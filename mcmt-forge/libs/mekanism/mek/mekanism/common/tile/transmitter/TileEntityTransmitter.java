package mekanism.common.tile.transmitter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import mekanism.api.IAlloyInteraction;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.text.EnumColor;
import mekanism.api.tier.AlloyTier;
import mekanism.api.tier.BaseTier;
import mekanism.client.model.data.TransmitterModelData;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.block.transmitter.BlockLargeTransmitter;
import mekanism.common.block.transmitter.BlockSmallTransmitter;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.DynamicHandler;
import mekanism.common.capabilities.proxy.ProxyConfigurable;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.capabilities.resolver.BasicSidedCapabilityResolver;
import mekanism.common.content.network.transmitter.BufferedTransmitter;
import mekanism.common.content.network.transmitter.IUpgradeableTransmitter;
import mekanism.common.content.network.transmitter.Transmitter;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.lib.transmitter.DynamicBufferedNetwork;
import mekanism.common.lib.transmitter.DynamicNetwork;
import mekanism.common.lib.transmitter.TransmitterNetworkRegistry;
import mekanism.common.tile.base.CapabilityTileEntity;
import mekanism.common.upgrade.transmitter.TransmitterUpgradeData;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MultipartUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TileEntityTransmitter extends CapabilityTileEntity implements ProxyConfigurable.ISidedConfigurable, IAlloyInteraction {
   public static final ModelProperty<TransmitterModelData> TRANSMITTER_PROPERTY = new ModelProperty();
   private final Transmitter<?, ?, ?> transmitter;
   private boolean forceUpdate = true;
   private boolean loaded = false;

   public TileEntityTransmitter(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
      super(((IHasTileEntity)blockProvider.getBlock()).getTileType(), pos, state);
      this.transmitter = this.createTransmitter(blockProvider);
      this.cacheCoord();
      this.addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.ALLOY_INTERACTION, this));
      this.addCapabilityResolver(new BasicSidedCapabilityResolver<>(this, Capabilities.CONFIGURABLE, ProxyConfigurable::new));
   }

   protected abstract Transmitter<?, ?, ?> createTransmitter(IBlockProvider blockProvider);

   public Transmitter<?, ?, ?> getTransmitter() {
      return this.transmitter;
   }

   public void setForceUpdate() {
      this.forceUpdate = true;
   }

   public abstract TransmitterType getTransmitterType();

   protected void onUpdateServer() {
      if (this.forceUpdate) {
         this.getTransmitter().refreshConnections();
         this.forceUpdate = false;
      }
   }

   public static void tickServer(Level level, BlockPos pos, BlockState state, TileEntityTransmitter transmitter) {
      transmitter.onUpdateServer();
   }

   @NotNull
   @Override
   public CompoundTag getReducedUpdateTag() {
      return this.getTransmitter().getReducedUpdateTag(super.getReducedUpdateTag());
   }

   @Override
   public void handleUpdateTag(@NotNull CompoundTag tag) {
      super.handleUpdateTag(tag);
      this.getTransmitter().handleUpdateTag(tag);
   }

   @Override
   public void handleUpdatePacket(@NotNull CompoundTag tag) {
      super.handleUpdatePacket(tag);
      this.updateModelData();
   }

   @Override
   public void m_142466_(@NotNull CompoundTag nbt) {
      super.m_142466_(nbt);
      this.getTransmitter().read(nbt);
   }

   public void m_183515_(@NotNull CompoundTag nbtTags) {
      super.m_183515_(nbtTags);
      this.getTransmitter().write(nbtTags);
   }

   public void onNeighborTileChange(Direction side) {
      this.getTransmitter().onNeighborTileChange(side);
   }

   public void onNeighborBlockChange(Direction side) {
      this.getTransmitter().onNeighborBlockChange(side);
   }

   public void m_6339_() {
      super.m_6339_();
      this.onWorldJoin(false);
   }

   public void onChunkUnloaded() {
      if (!this.isRemote()) {
         this.getTransmitter().takeShare();
      }

      super.onChunkUnloaded();
   }

   public void m_7651_() {
      super.m_7651_();
      this.onWorldSeparate(false);
      this.getTransmitter().remove();
   }

   public void onAdded() {
      this.onWorldJoin(false);
      this.getTransmitter().refreshConnections();
   }

   private void onWorldJoin(boolean wasPresent) {
      if (!this.isRemote() && !wasPresent) {
         TransmitterNetworkRegistry.trackTransmitter(this.getTransmitter());
      }

      if (!this.loaded) {
         this.loaded = true;
         if (!this.isRemote()) {
            TransmitterNetworkRegistry.registerOrphanTransmitter(this.getTransmitter());
         }
      }
   }

   private void onWorldSeparate(boolean stillPresent) {
      if (!this.isRemote() && !stillPresent) {
         TransmitterNetworkRegistry.untrackTransmitter(this.getTransmitter());
      }

      if (this.loaded) {
         this.loaded = false;
         if (this.isRemote()) {
            this.getTransmitter().setTransmitterNetwork(null);
         } else {
            TransmitterNetworkRegistry.invalidateTransmitter(this.getTransmitter());
         }
      }
   }

   public void chunkAccessibilityChange(boolean loaded) {
      if (loaded) {
         this.onWorldJoin(true);
      } else {
         this.getTransmitter().validateAndTakeShare();
         this.onWorldSeparate(true);
      }
   }

   public boolean isLoaded() {
      return this.loaded;
   }

   public Direction getSideLookingAt(Player player, Direction fallback) {
      Direction side = this.getSideLookingAt(player);
      return side == null ? fallback : side;
   }

   @Nullable
   public Direction getSideLookingAt(Player player) {
      MultipartUtils.AdvancedRayTraceResult result = MultipartUtils.collisionRayTrace(player, this.m_58899_(), this.getCollisionBoxes());
      if (result != null && result.valid()) {
         List<Direction> list = new ArrayList<>(EnumUtils.DIRECTIONS.length);
         byte connections = this.getTransmitter().getAllCurrentConnections();

         for (Direction dir : EnumUtils.DIRECTIONS) {
            if (Transmitter.connectionMapContainsSide(connections, dir)) {
               list.add(dir);
            }
         }

         int boxIndex = result.subHit + 1;
         if (boxIndex < list.size()) {
            return list.get(boxIndex);
         }
      }

      return null;
   }

   @NotNull
   @Override
   public InteractionResult onSneakRightClick(@NotNull Player player, @NotNull Direction side) {
      if (!this.isRemote()) {
         Direction hitSide = this.getSideLookingAt(player);
         if (hitSide == null) {
            if (this.transmitter.getConnectionTypeRaw(side) != ConnectionType.NONE) {
               InteractionResult result = this.onConfigure(player, side);
               if (result.m_19077_()) {
                  this.getTransmitter().refreshConnections();
                  this.getTransmitter().notifyTileChange();
                  return result;
               }
            }

            hitSide = side;
         }

         this.transmitter.setConnectionTypeRaw(hitSide, this.transmitter.getConnectionTypeRaw(hitSide).getNext());
         this.getTransmitter().onModeChange(Direction.m_122376_(hitSide.ordinal()));
         this.getTransmitter().refreshConnections();
         this.getTransmitter().notifyTileChange();
         player.m_5661_(MekanismLang.CONNECTION_TYPE.translateColored(EnumColor.GRAY, new Object[]{this.transmitter.getConnectionTypeRaw(hitSide)}), true);
         this.sendUpdatePacket();
      }

      return InteractionResult.SUCCESS;
   }

   protected InteractionResult onConfigure(Player player, Direction side) {
      return this.getTransmitter().onConfigure(player, side);
   }

   @NotNull
   @Override
   public InteractionResult onRightClick(@NotNull Player player, @NotNull Direction side) {
      return this.getTransmitter().onRightClick(player, side);
   }

   public List<VoxelShape> getCollisionBoxes() {
      List<VoxelShape> list = new ArrayList<>();
      boolean isSmall = this.getTransmitterType().getSize() == TransmitterType.Size.SMALL;

      for (Direction side : EnumUtils.DIRECTIONS) {
         ConnectionType connectionType = this.getTransmitter().getConnectionType(side);
         if (connectionType != ConnectionType.NONE) {
            if (isSmall) {
               list.add(BlockSmallTransmitter.getSideForType(connectionType, side));
            } else {
               list.add(BlockLargeTransmitter.getSideForType(connectionType, side));
            }
         }
      }

      list.add(isSmall ? BlockSmallTransmitter.CENTER : BlockLargeTransmitter.CENTER);
      return list;
   }

   @NotNull
   public AABB getRenderBoundingBox() {
      return new AABB(this.f_58858_, this.f_58858_.m_7918_(1, 1, 1));
   }

   @NotNull
   public ModelData getModelData() {
      TransmitterModelData data = this.initModelData();
      this.updateModelData(data);
      return ModelData.builder().with(TRANSMITTER_PROPERTY, data).build();
   }

   protected void updateModelData(TransmitterModelData modelData) {
      for (Direction side : EnumUtils.DIRECTIONS) {
         modelData.setConnectionData(side, this.getTransmitter().getConnectionType(side));
      }
   }

   @NotNull
   protected TransmitterModelData initModelData() {
      return new TransmitterModelData();
   }

   @Override
   public void onAlloyInteraction(Player player, ItemStack stack, @NotNull AlloyTier tier) {
      if (this.m_58904_() != null && this.getTransmitter().hasTransmitterNetwork()) {
         DynamicNetwork<?, ?, ?> transmitterNetwork = this.getTransmitter().getTransmitterNetwork();
         List<Transmitter<?, ?, ?>> list = new ArrayList<>((Collection<? extends Transmitter<?, ?, ?>>)transmitterNetwork.getTransmitters());
         list.sort(
            (o1, o2) -> o1 != null && o2 != null ? Double.compare(o1.getTilePos().m_123331_(this.f_58858_), o2.getTilePos().m_123331_(this.f_58858_)) : 0
         );
         boolean sharesSet = false;
         int upgraded = 0;

         for (Transmitter<?, ?, ?> transmitter : list) {
            if (transmitter instanceof IUpgradeableTransmitter<?> upgradeableTransmitter && upgradeableTransmitter.canUpgrade(tier)) {
               TileEntityTransmitter transmitterTile = transmitter.getTransmitterTile();
               BlockState state = transmitterTile.m_58900_();
               BlockState upgradeState = transmitterTile.upgradeResult(state, tier.getBaseTier());
               if (state != upgradeState) {
                  if (!sharesSet) {
                     if (transmitterNetwork instanceof DynamicBufferedNetwork dynamicNetwork) {
                        dynamicNetwork.validateSaveShares((BufferedTransmitter)transmitter);
                     }

                     sharesSet = true;
                  }

                  transmitter.startUpgrading();
                  TransmitterUpgradeData upgradeData = upgradeableTransmitter.getUpgradeData();
                  BlockPos transmitterPos = transmitter.getTilePos();
                  Level transmitterWorld = transmitter.getTileWorld();
                  if (upgradeData == null) {
                     Mekanism.logger
                        .warn(
                           "Got no upgrade data for transmitter at position: {} in {} but it said it would be able to provide some.",
                           transmitterPos,
                           transmitterWorld
                        );
                  } else {
                     transmitterWorld.m_46597_(transmitterPos, upgradeState);
                     TileEntityTransmitter upgradedTile = WorldUtils.getTileEntity(TileEntityTransmitter.class, transmitterWorld, transmitterPos);
                     if (upgradedTile == null) {
                        Mekanism.logger.warn("Error upgrading transmitter at position: {} in {}.", transmitterPos, transmitterWorld);
                     } else {
                        Transmitter<?, ?, ?> upgradedTransmitter = upgradedTile.getTransmitter();
                        if (upgradedTransmitter instanceof IUpgradeableTransmitter) {
                           this.transferUpgradeData((IUpgradeableTransmitter)upgradedTransmitter, upgradeData);
                        } else {
                           Mekanism.logger.warn("Unhandled upgrade data.", new IllegalStateException());
                        }

                        if (++upgraded == 8) {
                           break;
                        }
                     }
                  }
               }
            }
         }

         if (upgraded > 0) {
            transmitterNetwork.invalidate(null);
            if (!player.m_7500_()) {
               stack.m_41774_(1);
            }

            if (player instanceof ServerPlayer serverPlayer) {
               MekanismCriteriaTriggers.ALLOY_UPGRADE.trigger(serverPlayer);
            }
         }
      }
   }

   private <DATA extends TransmitterUpgradeData> void transferUpgradeData(IUpgradeableTransmitter<DATA> upgradeableTransmitter, TransmitterUpgradeData data) {
      if (upgradeableTransmitter.dataTypeMatches(data)) {
         upgradeableTransmitter.parseUpgradeData((DATA)data);
      } else {
         Mekanism.logger.warn("Unhandled upgrade data.", new IllegalStateException());
      }
   }

   @NotNull
   protected BlockState upgradeResult(@NotNull BlockState current, @NotNull BaseTier tier) {
      return current;
   }

   public void sideChanged(@NotNull Direction side, @NotNull ConnectionType old, @NotNull ConnectionType type) {
   }

   public void redstoneChanged(boolean powered) {
   }

   protected DynamicHandler.InteractPredicate getExtractPredicate() {
      return (tank, side) -> {
         if (side == null) {
            return true;
         } else {
            ConnectionType connectionType = this.getTransmitter().getConnectionType(side);
            return connectionType == ConnectionType.NORMAL || connectionType == ConnectionType.PUSH;
         }
      };
   }

   protected DynamicHandler.InteractPredicate getInsertPredicate() {
      return (tank, side) -> {
         if (side == null) {
            return true;
         } else {
            ConnectionType connectionType = this.getTransmitter().getConnectionType(side);
            return connectionType == ConnectionType.NORMAL || connectionType == ConnectionType.PULL;
         }
      };
   }
}
