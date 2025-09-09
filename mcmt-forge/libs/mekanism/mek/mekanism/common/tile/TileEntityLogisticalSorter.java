package mekanism.common.tile;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.text.EnumColor;
import mekanism.client.sound.SoundHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.filter.SortableFilterManager;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.SorterFilter;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.SyntheticComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.InternalInventorySlot;
import mekanism.common.lib.SidedBlockPos;
import mekanism.common.lib.inventory.Finder;
import mekanism.common.lib.inventory.TransitRequest;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ISustainedData;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporterBase;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityLogisticalSorter extends TileEntityMekanism implements ISustainedData, ITileFilterHolder<SorterFilter<?>> {
   private final SortableFilterManager<SorterFilter<?>> filterManager = new SortableFilterManager<>(SorterFilter.class, this::markForSave);
   private final Finder strictFinder = stack -> this.filterManager
      .getEnabledFilters()
      .stream()
      .noneMatch(filter -> !filter.allowDefault && filter.getFinder().modifies(stack));
   @SyntheticComputerMethod(
      getter = "getDefaultColor"
   )
   public EnumColor color;
   private boolean autoEject;
   private boolean roundRobin;
   private boolean singleItem;
   @Nullable
   public SidedBlockPos rrTarget;
   private int delayTicks;
   private long nextSound = 0L;

   public TileEntityLogisticalSorter(BlockPos pos, BlockState state) {
      super(MekanismBlocks.LOGISTICAL_SORTER, pos, state);
      this.delaySupplier = () -> 3;
      this.addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIG_CARD, this));
   }

   @NotNull
   @Override
   protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
      InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
      builder.addSlot(InternalInventorySlot.create(listener), RelativeSide.FRONT);
      return builder.build();
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      this.delayTicks = Math.max(0, this.delayTicks - 1);
      if (this.delayTicks == 6) {
         this.setActive(false);
      }

      if (MekanismUtils.canFunction(this) && this.delayTicks == 0) {
         Direction direction = this.getDirection();
         BlockEntity back = WorldUtils.getTileEntity(this.m_58904_(), this.f_58858_.m_121945_(direction.m_122424_()));
         BlockEntity front = WorldUtils.getTileEntity(this.m_58904_(), this.f_58858_.m_121945_(direction));
         if (InventoryUtils.isItemHandler(back, direction) && front != null) {
            boolean sentItems = false;

            for (SorterFilter<?> filter : this.filterManager.getEnabledFilters()) {
               TransitRequest request = filter.mapInventory(back, direction, this.singleItem);
               if (!request.isEmpty()) {
                  int min = this.singleItem ? 1 : (filter.sizeMode ? filter.min : 0);
                  TransitRequest.TransitResponse response = this.emitItemToTransporter(front, request, filter.color, min);
                  if (!response.isEmpty()) {
                     response.useAll();
                     WorldUtils.saveChunk(back);
                     this.setActive(true);
                     sentItems = true;
                     break;
                  }
               }
            }

            if (!sentItems && this.autoEject) {
               TransitRequest request = TransitRequest.definedItem(back, direction, this.singleItem ? 1 : 64, this.strictFinder);
               TransitRequest.TransitResponse response = this.emitItemToTransporter(front, request, this.color, 0);
               if (!response.isEmpty()) {
                  response.useAll();
                  WorldUtils.saveChunk(back);
                  this.setActive(true);
               }
            }
         }

         this.delayTicks = 10;
      }
   }

   private TransitRequest.TransitResponse emitItemToTransporter(BlockEntity front, TransitRequest request, EnumColor filterColor, int min) {
      if (front instanceof TileEntityLogisticalTransporterBase transporterBase) {
         LogisticalTransporterBase transporter = transporterBase.getTransmitter();
         return this.roundRobin ? transporter.insertRR(this, request, filterColor, true, min) : transporter.insert(this, request, filterColor, true, min);
      } else {
         return request.addToInventory(front, this.getDirection(), min, false);
      }
   }

   @Override
   public void m_183515_(@NotNull CompoundTag nbtTags) {
      super.m_183515_(nbtTags);
      if (this.rrTarget != null) {
         nbtTags.m_128365_("rrTarget", this.rrTarget.serialize());
      }
   }

   @Override
   public void m_142466_(@NotNull CompoundTag nbt) {
      super.m_142466_(nbt);
      if (nbt.m_128425_("rrTarget", 10)) {
         this.rrTarget = SidedBlockPos.deserialize(nbt.m_128469_("rrTarget"));
      }
   }

   @Override
   protected boolean canPlaySound() {
      return false;
   }

   @Override
   protected void onUpdateClient() {
      super.onUpdateClient();
      if (MekanismConfig.client.enableMachineSounds.get() && this.getActive() && this.f_58857_.m_46467_() >= this.nextSound) {
         if (!this.isFullyMuffled()) {
            SoundHandler.startTileSound(this.soundEvent, this.getSoundCategory(), this.getInitialVolume(), this.f_58857_.m_213780_(), this.getSoundPos(), false);
         }

         this.nextSound = this.f_58857_.m_46467_() + 20L * this.f_58857_.f_46441_.m_216339_(5, 15);
      }
   }

   @ComputerMethod(
      nameOverride = "getAutoMode"
   )
   public boolean getAutoEject() {
      return this.autoEject;
   }

   @ComputerMethod(
      nameOverride = "isRoundRobin"
   )
   public boolean getRoundRobin() {
      return this.roundRobin;
   }

   @ComputerMethod(
      nameOverride = "isSingle"
   )
   public boolean getSingleItem() {
      return this.singleItem;
   }

   public void toggleAutoEject() {
      this.autoEject = !this.autoEject;
      this.markForSave();
   }

   public void toggleRoundRobin() {
      this.roundRobin = !this.roundRobin;
      this.rrTarget = null;
      this.markForSave();
   }

   public void toggleSingleItem() {
      this.singleItem = !this.singleItem;
      this.markForSave();
   }

   public void changeColor(@Nullable EnumColor color) {
      if (this.color != color) {
         this.color = color;
         this.markForSave();
      }
   }

   public boolean canSendHome(ItemStack stack) {
      Direction oppositeDirection = this.getOppositeDirection();
      BlockEntity back = WorldUtils.getTileEntity(this.m_58904_(), this.f_58858_.m_121945_(oppositeDirection));
      return TransporterUtils.canInsert(back, null, stack, oppositeDirection, true);
   }

   public boolean hasConnectedInventory() {
      Direction oppositeDirection = this.getOppositeDirection();
      BlockEntity tile = WorldUtils.getTileEntity(this.m_58904_(), this.f_58858_.m_121945_(oppositeDirection));
      return TransporterUtils.isValidAcceptorOnSide(tile, oppositeDirection);
   }

   @NotNull
   public TransitRequest.TransitResponse sendHome(TransitRequest request) {
      Direction oppositeDirection = this.getOppositeDirection();
      BlockEntity back = WorldUtils.getTileEntity(this.m_58904_(), this.f_58858_.m_121945_(oppositeDirection));
      return request.addToInventory(back, oppositeDirection, 0, true);
   }

   @Override
   public boolean canPulse() {
      return true;
   }

   @Override
   public void writeSustainedData(CompoundTag dataMap) {
      dataMap.m_128405_("color", TransporterUtils.getColorIndex(this.color));
      dataMap.m_128379_("eject", this.autoEject);
      dataMap.m_128379_("roundRobin", this.roundRobin);
      dataMap.m_128379_("singleItem", this.singleItem);
      this.filterManager.writeToNBT(dataMap);
   }

   @Override
   public void readSustainedData(CompoundTag dataMap) {
      NBTUtils.setEnumIfPresent(dataMap, "color", TransporterUtils::readColor, color -> this.color = color);
      this.autoEject = dataMap.m_128471_("eject");
      this.roundRobin = dataMap.m_128471_("roundRobin");
      this.singleItem = dataMap.m_128471_("singleItem");
      this.filterManager.readFromNBT(dataMap);
   }

   @Override
   public Map<String, String> getTileDataRemap() {
      Map<String, String> remap = new Object2ObjectOpenHashMap();
      remap.put("color", "color");
      remap.put("eject", "eject");
      remap.put("roundRobin", "roundRobin");
      remap.put("singleItem", "singleItem");
      remap.put("filters", "filters");
      return remap;
   }

   @Override
   public int getRedstoneLevel() {
      return this.getActive() ? 15 : 0;
   }

   @Override
   protected boolean makesComparatorDirty(@Nullable SubstanceType type) {
      return false;
   }

   @Override
   public int getCurrentRedstoneLevel() {
      return this.getRedstoneLevel();
   }

   public SortableFilterManager<SorterFilter<?>> getFilterManager() {
      return this.filterManager;
   }

   @Override
   public void addContainerTrackers(MekanismContainer container) {
      super.addContainerTrackers(container);
      container.track(SyncableBoolean.create(this::getAutoEject, value -> this.autoEject = value));
      container.track(SyncableBoolean.create(this::getRoundRobin, value -> this.roundRobin = value));
      container.track(SyncableBoolean.create(this::getSingleItem, value -> this.singleItem = value));
      container.track(SyncableInt.create(() -> TransporterUtils.getColorIndex(this.color), value -> this.color = TransporterUtils.readColor(value)));
      this.filterManager.addContainerTrackers(container);
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   void setSingle(boolean value) throws ComputerException {
      this.validateSecurityIsPublic();
      if (this.singleItem != value) {
         this.toggleSingleItem();
      }
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   void setRoundRobin(boolean value) throws ComputerException {
      this.validateSecurityIsPublic();
      if (this.roundRobin != value) {
         this.toggleRoundRobin();
      }
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   void setAutoMode(boolean value) throws ComputerException {
      this.validateSecurityIsPublic();
      if (this.autoEject != value) {
         this.toggleAutoEject();
      }
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   void clearDefaultColor() throws ComputerException {
      this.validateSecurityIsPublic();
      this.changeColor(null);
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   void incrementDefaultColor() throws ComputerException {
      this.validateSecurityIsPublic();
      this.color = TransporterUtils.increment(this.color);
      this.markForSave();
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   void decrementDefaultColor() throws ComputerException {
      this.validateSecurityIsPublic();
      this.color = TransporterUtils.decrement(this.color);
      this.markForSave();
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   void setDefaultColor(EnumColor color) throws ComputerException {
      this.validateSecurityIsPublic();
      if (!TransporterUtils.colors.contains(color)) {
         throw new ComputerException("Color '%s' is not a supported transporter color.", color);
      } else {
         this.changeColor(color);
      }
   }

   @ComputerMethod
   List<SorterFilter<?>> getFilters() {
      return this.filterManager.getFilters();
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   boolean addFilter(SorterFilter<?> filter) throws ComputerException {
      this.validateSecurityIsPublic();
      return this.filterManager.addFilter(filter);
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   boolean removeFilter(SorterFilter<?> filter) throws ComputerException {
      this.validateSecurityIsPublic();
      return this.filterManager.removeFilter(filter);
   }
}
