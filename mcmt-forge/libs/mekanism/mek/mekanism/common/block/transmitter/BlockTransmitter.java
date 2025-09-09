package mekanism.common.block.transmitter;

import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMaps;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import mekanism.api.text.TextComponentUtil;
import mekanism.api.tier.BaseTier;
import mekanism.common.block.BlockMekanism;
import mekanism.common.block.states.IStateFluidLoggable;
import mekanism.common.content.network.transmitter.Transmitter;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MultipartUtils;
import mekanism.common.util.VoxelShapeUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BlockTransmitter extends BlockMekanism implements IStateFluidLoggable {
   private static final Short2ObjectMap<VoxelShape> cachedShapes = Short2ObjectMaps.synchronize(new Short2ObjectOpenHashMap());

   protected BlockTransmitter(UnaryOperator<Properties> propertiesModifier) {
      super(propertiesModifier.apply(Properties.m_284310_().m_60913_(1.0F, 6.0F).m_278166_(PushReaction.BLOCK)));
   }

   @Nullable
   protected BaseTier getBaseTier() {
      return null;
   }

   @NotNull
   public MutableComponent m_49954_() {
      BaseTier baseTier = this.getBaseTier();
      return baseTier == null ? super.m_49954_() : TextComponentUtil.build(baseTier.getColor(), super.m_49954_());
   }

   @Deprecated
   @NotNull
   public InteractionResult m_6227_(
      @NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit
   ) {
      ItemStack stack = player.m_21120_(hand);
      if (MekanismUtils.canUseAsWrench(stack) && player.m_6144_()) {
         if (!world.f_46443_) {
            WorldUtils.dismantleBlock(state, world, pos);
         }

         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.PASS;
      }
   }

   @Override
   public void m_6402_(@NotNull Level world, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
      TileEntityTransmitter tile = WorldUtils.getTileEntity(TileEntityTransmitter.class, world, pos);
      if (tile != null) {
         tile.onAdded();
      }
   }

   @Deprecated
   public void m_6861_(
      @NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Block neighborBlock, @NotNull BlockPos neighborPos, boolean isMoving
   ) {
      TileEntityTransmitter tile = WorldUtils.getTileEntity(TileEntityTransmitter.class, world, pos);
      if (tile != null) {
         Direction side = Direction.m_122372_(
            neighborPos.m_123341_() - pos.m_123341_(), neighborPos.m_123342_() - pos.m_123342_(), neighborPos.m_123343_() - pos.m_123343_()
         );
         tile.onNeighborBlockChange(side);
      }
   }

   public void onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos neighbor) {
      TileEntityTransmitter tile = WorldUtils.getTileEntity(TileEntityTransmitter.class, world, pos);
      if (tile != null) {
         Direction side = Direction.m_122372_(
            neighbor.m_123341_() - pos.m_123341_(), neighbor.m_123342_() - pos.m_123342_(), neighbor.m_123343_() - pos.m_123343_()
         );
         tile.onNeighborTileChange(side);
      }
   }

   @Deprecated
   public boolean m_7357_(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull PathComputationType type) {
      return false;
   }

   @Deprecated
   @NotNull
   public VoxelShape m_5940_(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, CollisionContext context) {
      if (!context.m_7142_(MekanismItems.CONFIGURATOR.m_5456_())) {
         return this.getRealShape(world, pos);
      } else if (context instanceof EntityCollisionContext entityContext && entityContext.m_193113_() != null) {
         TileEntityTransmitter tile = WorldUtils.getTileEntity(TileEntityTransmitter.class, world, pos);
         if (tile == null) {
            return this.getCenter();
         } else {
            MultipartUtils.AdvancedRayTraceResult result = MultipartUtils.collisionRayTrace(entityContext.m_193113_(), pos, tile.getCollisionBoxes());
            return result != null && result.valid() ? result.bounds : this.getCenter();
         }
      } else {
         return this.getRealShape(world, pos);
      }
   }

   @Deprecated
   @NotNull
   public VoxelShape m_7952_(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos) {
      return this.getRealShape(world, pos);
   }

   @Deprecated
   @NotNull
   public VoxelShape m_5939_(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
      return this.getRealShape(world, pos);
   }

   protected abstract VoxelShape getCenter();

   protected abstract VoxelShape getSide(ConnectionType type, Direction side);

   private VoxelShape getRealShape(BlockGetter world, BlockPos pos) {
      TileEntityTransmitter tile = WorldUtils.getTileEntity(TileEntityTransmitter.class, world, pos);
      if (tile == null) {
         return this.getCenter();
      } else {
         Transmitter<?, ?, ?> transmitter = tile.getTransmitter();
         int packedKey = tile.getTransmitterType().getSize().ordinal() << 12;

         for (Direction side : EnumUtils.DIRECTIONS) {
            ConnectionType connectionType = transmitter.getConnectionType(side);
            packedKey |= connectionType.ordinal() << side.ordinal() * 2;
         }

         return (VoxelShape)cachedShapes.computeIfAbsent((short)packedKey, packed -> {
            List<VoxelShape> shapes = new ArrayList<>(EnumUtils.DIRECTIONS.length);

            for (Direction sidex : EnumUtils.DIRECTIONS) {
               int index = packed >> sidex.ordinal() * 2 & 3;
               ConnectionType connectionTypex = ConnectionType.byIndexStatic(index);
               if (connectionTypex != ConnectionType.NONE) {
                  shapes.add(this.getSide(connectionTypex, sidex));
               }
            }

            VoxelShape center = this.getCenter();
            return shapes.isEmpty() ? center : VoxelShapeUtils.batchCombine(center, BooleanOp.f_82695_, true, shapes);
         });
      }
   }
}
