package mekanism.common.tile;

import mekanism.api.Action;
import mekanism.api.IConfigurable;
import mekanism.api.IContentsListener;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.slot.BinInventorySlot;
import mekanism.common.lib.inventory.TileTransitRequest;
import mekanism.common.lib.inventory.TransitRequest;
import mekanism.common.tier.BinTier;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporterBase;
import mekanism.common.upgrade.BinUpgradeData;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TileEntityBin extends TileEntityMekanism implements IConfigurable {
   public int addTicks = 0;
   public int removeTicks = 0;
   private int delayTicks;
   private BinTier tier;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getStored"},
      docPlaceholder = "bin"
   )
   BinInventorySlot binSlot;

   public TileEntityBin(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
      super(blockProvider, pos, state);
      this.addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIGURABLE, this));
   }

   @Override
   protected void presetVariables() {
      super.presetVariables();
      this.tier = Attribute.getTier(this.getBlockType(), BinTier.class);
   }

   @NotNull
   @Override
   protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
      InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
      builder.addSlot(this.binSlot = BinInventorySlot.create(listener, this.tier));
      return builder.build();
   }

   public BinTier getTier() {
      return this.tier;
   }

   public int getItemCount() {
      return this.binSlot.getCount();
   }

   public BinInventorySlot getBinSlot() {
      return this.binSlot;
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      this.addTicks = Math.max(0, this.addTicks - 1);
      this.removeTicks = Math.max(0, this.removeTicks - 1);
      this.delayTicks = Math.max(0, this.delayTicks - 1);
      if (this.delayTicks == 0) {
         if (this.getActive()) {
            BlockEntity tile = WorldUtils.getTileEntity(this.m_58904_(), this.m_58899_().m_7495_());
            TileTransitRequest request = new TileTransitRequest(this, Direction.DOWN);
            request.addItem(this.binSlot.getBottomStack(), 0);
            TransitRequest.TransitResponse response;
            if (tile instanceof TileEntityLogisticalTransporterBase transporter) {
               response = transporter.getTransmitter().insert(this, request, transporter.getTransmitter().getColor(), true, 0);
            } else {
               response = request.addToInventory(tile, Direction.DOWN, 0, false);
            }

            if (!response.isEmpty() && this.tier != BinTier.CREATIVE) {
               int sendingAmount = response.getSendingAmount();
               MekanismUtils.logMismatchedStackSize(this.binSlot.shrinkStack(sendingAmount, Action.EXECUTE), sendingAmount);
            }

            this.delayTicks = 10;
         }
      } else {
         this.delayTicks--;
      }
   }

   @Override
   public InteractionResult onSneakRightClick(Player player) {
      this.setActive(!this.getActive());
      Level world = this.m_58904_();
      if (world != null) {
         world.m_6263_(
            null,
            this.m_58899_().m_123341_(),
            this.m_58899_().m_123342_(),
            this.m_58899_().m_123343_(),
            (SoundEvent)SoundEvents.f_12490_.get(),
            SoundSource.BLOCKS,
            0.3F,
            1.0F
         );
      }

      return InteractionResult.SUCCESS;
   }

   @Override
   public InteractionResult onRightClick(Player player) {
      return InteractionResult.PASS;
   }

   public boolean toggleLock() {
      return this.setLocked(!this.binSlot.isLocked());
   }

   public boolean setLocked(boolean isLocked) {
      if (this.binSlot.setLocked(isLocked)) {
         if (this.m_58904_() != null && !this.isRemote()) {
            this.sendUpdatePacket();
            this.markForSave();
            this.m_58904_()
               .m_6263_(
                  null,
                  this.m_58899_().m_123341_(),
                  this.m_58899_().m_123342_(),
                  this.m_58899_().m_123343_(),
                  (SoundEvent)SoundEvents.f_12490_.get(),
                  SoundSource.BLOCKS,
                  0.3F,
                  1.0F
               );
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public void parseUpgradeData(@NotNull IUpgradeData upgradeData) {
      if (upgradeData instanceof BinUpgradeData data) {
         this.redstone = data.redstone();
         BinInventorySlot previous = data.binSlot();
         this.binSlot.setStack(previous.getStack());
         this.binSlot.setLockStack(previous.getLockStack());
      } else {
         super.parseUpgradeData(upgradeData);
      }
   }

   @NotNull
   public BinUpgradeData getUpgradeData() {
      return new BinUpgradeData(this.redstone, this.getBinSlot());
   }

   @Override
   public void onContentsChanged() {
      super.onContentsChanged();
      if (this.f_58857_ != null && !this.isRemote()) {
         this.sendUpdatePacket();
      }
   }

   @NotNull
   @Override
   public CompoundTag getReducedUpdateTag() {
      CompoundTag updateTag = super.getReducedUpdateTag();
      updateTag.m_128365_("Item", this.binSlot.serializeNBT());
      return updateTag;
   }

   @Override
   public void handleUpdateTag(@NotNull CompoundTag tag) {
      super.handleUpdateTag(tag);
      NBTUtils.setCompoundIfPresent(tag, "Item", nbt -> this.binSlot.deserializeNBT(nbt));
   }

   @ComputerMethod(
      methodDescription = "Get the maximum number of items the bin can contain."
   )
   int getCapacity() {
      return this.binSlot.getLimit(this.binSlot.getStack());
   }

   @ComputerMethod(
      methodDescription = "If true, the Bin is locked to a particular item type."
   )
   boolean isLocked() {
      return this.binSlot.isLocked();
   }

   @ComputerMethod(
      methodDescription = "Get the type of item the Bin is locked to (or Air if not locked)"
   )
   ItemStack getLock() {
      return this.binSlot.getLockStack();
   }

   @ComputerMethod(
      methodDescription = "Lock the Bin to the currently stored item type. The Bin must not be creative, empty, or already locked"
   )
   void lock() throws ComputerException {
      if (this.getTier() == BinTier.CREATIVE) {
         throw new ComputerException("Creative bins cannot be locked!");
      } else if (this.binSlot.isEmpty()) {
         throw new ComputerException("Empty bins cannot be locked!");
      } else if (!this.setLocked(true)) {
         throw new ComputerException("This bin is already locked!");
      }
   }

   @ComputerMethod(
      methodDescription = "Unlock the Bin's fixed item type. The Bin must not be creative, or already unlocked"
   )
   void unlock() throws ComputerException {
      if (this.getTier() == BinTier.CREATIVE) {
         throw new ComputerException("Creative bins cannot be unlocked!");
      } else if (!this.setLocked(true)) {
         throw new ComputerException("This bin is not locked!");
      }
   }
}
