package mekanism.common.tile.prefab;

import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.UUID;
import mekanism.api.IConfigurable;
import mekanism.api.IContentsListener;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.text.EnumColor;
import mekanism.client.SparkleAnimation;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.BoundMethodHolder;
import mekanism.common.integration.computer.FactoryRegistry;
import mekanism.common.integration.computer.MethodRestriction;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.dynamic.SyncMapper;
import mekanism.common.lib.multiblock.FormationProtocol;
import mekanism.common.lib.multiblock.IMultiblock;
import mekanism.common.lib.multiblock.IStructuralMultiblock;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.lib.multiblock.Structure;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TileEntityMultiblock<T extends MultiblockData> extends TileEntityMekanism implements IMultiblock<T>, IConfigurable {
   private Structure structure = Structure.INVALID;
   private final T defaultMultiblock = this.createMultiblock();
   private boolean prevStructure;
   private boolean isMaster;
   @Nullable
   private UUID cachedID = null;
   private long unformedTicks = 100L;

   public TileEntityMultiblock(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
      super(blockProvider, pos, state);
      this.cacheCoord();
      this.addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIGURABLE, this));
   }

   @Override
   public void setStructure(Structure structure) {
      this.structure = structure;
   }

   @Override
   public Structure getStructure() {
      return this.structure;
   }

   @Override
   public T getDefaultData() {
      return this.defaultMultiblock;
   }

   @Override
   protected void onUpdateClient() {
      super.onUpdateClient();
      if (!this.getMultiblock().isFormed()) {
         this.unformedTicks++;
         if (!this.playersUsing.isEmpty()) {
            ObjectIterator var1 = new ObjectOpenHashSet(this.playersUsing).iterator();

            while (var1.hasNext()) {
               Player player = (Player)var1.next();
               player.m_6915_();
            }
         }
      } else {
         this.unformedTicks = 0L;
      }
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      boolean needsPacket = false;
      if (this.ticker >= 3) {
         this.structure.tick(this, this.ticker % 10 == 0);
      }

      T multiblock = this.getMultiblock();
      if (this.isMaster() && multiblock.isFormed() && multiblock.recheckStructure) {
         multiblock.recheckStructure = false;
         this.getStructure().doImmediateUpdate(this, this.ticker % 10 == 0);
         multiblock = this.getMultiblock();
      }

      if (multiblock.isFormed()) {
         if (!this.prevStructure) {
            this.structureChanged(multiblock);
            this.prevStructure = true;
            needsPacket = true;
         }

         if (multiblock.inventoryID != null) {
            UUID oldCachedID = this.cachedID;
            this.cachedID = multiblock.inventoryID;
            if (oldCachedID != this.cachedID) {
               this.markForSave();
            }

            if (this.isMaster()) {
               if (multiblock.tick(this.f_58857_)) {
                  needsPacket = true;
               }

               this.getManager().handleDirtyMultiblock(multiblock);
            }
         }
      } else {
         this.playersUsing.forEach(Player::m_6915_);
         if (this.prevStructure) {
            this.structureChanged(multiblock);
            this.prevStructure = false;
            needsPacket = true;
         }

         this.isMaster = false;
      }

      needsPacket |= this.onUpdateServer(multiblock);
      if (needsPacket) {
         this.sendUpdatePacket();
      }
   }

   protected boolean onUpdateServer(T multiblock) {
      return false;
   }

   @Override
   public void resetForFormed() {
      this.isMaster = false;
      this.prevStructure = false;
   }

   protected void structureChanged(T multiblock) {
      this.invalidateCachedCapabilities();
      if (multiblock.isFormed() && !multiblock.hasMaster && this.canBeMaster()) {
         multiblock.hasMaster = true;
         this.isMaster = true;
         multiblock.forceUpdateComparatorLevel();
         multiblock.notifyAllUpdateComparator(this.f_58857_);
      }

      for (Direction side : EnumUtils.DIRECTIONS) {
         BlockPos pos = this.m_58899_().m_121945_(side);
         if (!multiblock.isFormed() || !multiblock.isKnownLocation(pos)) {
            BlockEntity tile = WorldUtils.getTileEntity(this.f_58857_, pos);
            if (!this.f_58857_.m_46859_(pos)
               && (tile == null || tile.getClass() != this.getClass())
               && !(tile instanceof IStructuralMultiblock)
               && !(tile instanceof IMultiblock)) {
               WorldUtils.notifyNeighborOfChange(this.f_58857_, pos, this.m_58899_());
            }
         }
      }

      if (!multiblock.isFormed()) {
         this.markDirtyComparator();
      }
   }

   @Override
   protected boolean makesComparatorDirty(@Nullable SubstanceType type) {
      return false;
   }

   @Override
   public boolean canBeMaster() {
      return true;
   }

   @Override
   public InteractionResult onActivate(Player player, InteractionHand hand, ItemStack stack) {
      return !player.m_6144_() && this.getMultiblock().isFormed() ? this.openGui(player) : InteractionResult.PASS;
   }

   @Override
   public void m_7651_() {
      super.m_7651_();
      if (!this.isRemote()) {
         this.structure.invalidate(this.f_58857_);
      }
   }

   @Override
   public boolean shouldDumpRadiation() {
      return false;
   }

   @Override
   public void resetCache() {
      this.cachedID = null;
   }

   @Nullable
   @Override
   public UUID getCacheID() {
      return this.cachedID;
   }

   @Override
   public boolean isMaster() {
      return this.isMaster;
   }

   @NotNull
   @Override
   public CompoundTag getReducedUpdateTag() {
      CompoundTag updateTag = super.getReducedUpdateTag();
      updateTag.m_128379_("rendering", this.isMaster());
      T multiblock = this.getMultiblock();
      updateTag.m_128379_("hasStructure", multiblock.isFormed());
      if (multiblock.isFormed() && this.isMaster()) {
         multiblock.writeUpdateTag(updateTag);
      }

      return updateTag;
   }

   @Override
   public void handleUpdateTag(@NotNull CompoundTag tag) {
      super.handleUpdateTag(tag);
      NBTUtils.setBooleanIfPresent(tag, "rendering", value -> this.isMaster = value);
      T multiblock = this.getMultiblock();
      NBTUtils.setBooleanIfPresent(tag, "hasStructure", multiblock::setFormedForce);
      if (this.isMaster()) {
         if (multiblock.isFormed()) {
            multiblock.readUpdateTag(tag);
            this.doMultiblockSparkle(multiblock);
         } else {
            this.isMaster = false;
         }
      }

      this.prevStructure = multiblock.isFormed();
   }

   private void doMultiblockSparkle(T multiblock) {
      if (this.isRemote() && multiblock.renderLocation != null && !this.prevStructure && this.unformedTicks >= 5L) {
         LocalPlayer player = Minecraft.m_91087_().f_91074_;
         if (player != null && this.f_58858_.m_123331_(player.m_20183_()) <= 1600.0) {
            if (MekanismConfig.client.enableMultiblockFormationParticles.get()) {
               new SparkleAnimation(this, multiblock.renderLocation, multiblock.length() - 1, multiblock.width() - 1, multiblock.height() - 1).run();
            } else {
               player.m_5661_(MekanismLang.MULTIBLOCK_FORMED_CHAT.translateColored(EnumColor.INDIGO, new Object[0]), true);
            }
         }
      }
   }

   @Override
   public void m_142466_(@NotNull CompoundTag nbt) {
      super.m_142466_(nbt);
      if (!this.getMultiblock().isFormed()) {
         NBTUtils.setUUIDIfPresent(nbt, "inventoryID", id -> this.cachedID = id);
      }
   }

   @Override
   public void m_183515_(@NotNull CompoundTag nbtTags) {
      super.m_183515_(nbtTags);
      if (this.cachedID != null) {
         nbtTags.m_128362_("inventoryID", this.cachedID);
      }
   }

   @Override
   public void addContainerTrackers(MekanismContainer container) {
      super.addContainerTrackers(container);
      SyncMapper.INSTANCE.setup(container, this.getMultiblock().getClass(), this::getMultiblock);
   }

   @NotNull
   public AABB getRenderBoundingBox() {
      if (this.isMaster()) {
         T multiblock = this.getMultiblock();
         if (multiblock.isFormed() && multiblock.getBounds() != null) {
            return new AABB(multiblock.getMinPos(), multiblock.getMaxPos().m_7918_(1, 1, 1));
         }
      }

      return super.getRenderBoundingBox();
   }

   @Override
   public boolean persistInventory() {
      return false;
   }

   @NotNull
   @Override
   protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
      return side -> this.getMultiblock().getInventorySlots(side);
   }

   @Override
   public void onNeighborChange(Block block, BlockPos neighborPos) {
      super.onNeighborChange(block, neighborPos);
      if (!this.isRemote()) {
         T multiblock = this.getMultiblock();
         if (multiblock.isPositionInsideBounds(this.getStructure(), neighborPos)
            && (this.f_58857_.m_46859_(neighborPos) || !multiblock.internalLocations.contains(neighborPos))) {
            this.getStructure().markForUpdate(this.f_58857_, true);
         }
      }
   }

   @Override
   public InteractionResult onRightClick(Player player) {
      if (!this.isRemote() && !this.getMultiblock().isFormed()) {
         FormationProtocol.FormationResult result = this.getStructure().runUpdate(this);
         if (!result.isFormed() && result.getResultText() != null) {
            player.m_213846_(result.getResultText());
            return InteractionResult.m_19078_(this.isRemote());
         }
      }

      return InteractionResult.PASS;
   }

   @Override
   public InteractionResult onSneakRightClick(Player player) {
      return InteractionResult.PASS;
   }

   public boolean exposesMultiblockToComputer() {
      return true;
   }

   @Override
   public boolean isComputerCapabilityPersistent() {
      return !this.exposesMultiblockToComputer() && super.isComputerCapabilityPersistent();
   }

   @Override
   public void getComputerMethods(BoundMethodHolder holder) {
      super.getComputerMethods(holder);
      if (this.exposesMultiblockToComputer()) {
         T multiblock = this.getMultiblock();
         if (multiblock.isFormed()) {
            FactoryRegistry.bindTo(holder, multiblock);
         }
      }
   }

   @ComputerMethod(
      restriction = MethodRestriction.MULTIBLOCK
   )
   boolean isFormed() {
      return this.getMultiblock().isFormed();
   }
}
