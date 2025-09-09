package mekanism.common.block;

import java.util.function.Consumer;
import mekanism.client.render.RenderPropertiesProvider;
import mekanism.common.Mekanism;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.IStateFluidLoggable;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.resource.BlockResourceInfo;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.util.WorldUtils;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockBounding extends Block implements IHasTileEntity<TileEntityBoundingBlock>, IStateFluidLoggable {
   @Nullable
   public static BlockPos getMainBlockPos(BlockGetter world, BlockPos thisPos) {
      TileEntityBoundingBlock te = WorldUtils.getTileEntity(TileEntityBoundingBlock.class, world, thisPos);
      return te != null && te.hasReceivedCoords() && !thisPos.equals(te.getMainPos()) ? te.getMainPos() : null;
   }

   public BlockBounding() {
      super(
         BlockStateHelper.applyLightLevelAdjustments(
            Properties.m_284310_()
               .m_284180_(BlockResourceInfo.STEEL.getMapColor())
               .m_60913_(3.5F, 4.8F)
               .m_60999_()
               .m_60988_()
               .m_60955_()
               .m_60971_(BlockStateHelper.NEVER_PREDICATE)
               .m_278166_(PushReaction.BLOCK)
         )
      );
      this.m_49959_(BlockStateHelper.getDefaultState((BlockState)this.f_49792_.m_61090_()));
   }

   public void initializeClient(Consumer<IClientBlockExtensions> consumer) {
      consumer.accept(RenderPropertiesProvider.boundingParticles());
   }

   protected void m_7926_(@NotNull Builder<Block, BlockState> builder) {
      super.m_7926_(builder);
      BlockStateHelper.fillBlockStateContainer(this, builder);
   }

   @Nullable
   public BlockState m_5573_(@NotNull BlockPlaceContext context) {
      return BlockStateHelper.getStateForPlacement(this, super.m_5573_(context), context);
   }

   @Deprecated
   @NotNull
   public InteractionResult m_6227_(
      @NotNull BlockState state,
      @NotNull Level world,
      @NotNull BlockPos pos,
      @NotNull Player player,
      @NotNull InteractionHand hand,
      @NotNull BlockHitResult hit
   ) {
      BlockPos mainPos = getMainBlockPos(world, pos);
      if (mainPos == null) {
         return InteractionResult.FAIL;
      } else {
         BlockState mainState = world.m_8055_(mainPos);
         return mainState.m_60734_().m_6227_(mainState, world, mainPos, player, hand, hit);
      }
   }

   @Deprecated
   public void m_6810_(BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
      if (!state.m_60713_(newState.m_60734_())) {
         BlockPos mainPos = getMainBlockPos(world, pos);
         if (mainPos != null) {
            BlockState mainState = world.m_8055_(mainPos);
            if (!mainState.m_60795_()) {
               world.m_7471_(mainPos, false);
            }
         }

         super.m_6810_(state, world, pos, newState, isMoving);
      }
   }

   @NotNull
   public ItemStack getCloneItemStack(@NotNull BlockState state, HitResult target, @NotNull BlockGetter world, @NotNull BlockPos pos, Player player) {
      BlockPos mainPos = getMainBlockPos(world, pos);
      if (mainPos == null) {
         return ItemStack.f_41583_;
      } else {
         BlockState mainState = world.m_8055_(mainPos);
         return mainState.m_60734_().getCloneItemStack(mainState, target, world, mainPos, player);
      }
   }

   public boolean onDestroyedByPlayer(
      @NotNull BlockState state, Level world, @NotNull BlockPos pos, @NotNull Player player, boolean willHarvest, FluidState fluidState
   ) {
      if (willHarvest) {
         return true;
      } else {
         BlockPos mainPos = getMainBlockPos(world, pos);
         if (mainPos != null) {
            BlockState mainState = world.m_8055_(mainPos);
            if (!mainState.m_60795_()) {
               mainState.onDestroyedByPlayer(world, mainPos, player, false, mainState.m_60819_());
            }
         }

         return super.onDestroyedByPlayer(state, world, pos, player, false, fluidState);
      }
   }

   public void onBlockExploded(BlockState state, Level world, BlockPos pos, Explosion explosion) {
      BlockPos mainPos = getMainBlockPos(world, pos);
      if (mainPos != null) {
         BlockState mainState = world.m_8055_(mainPos);
         if (!mainState.m_60795_()) {
            if (world instanceof ServerLevel serverLevel) {
               net.minecraft.world.level.storage.loot.LootParams.Builder lootContextBuilder = new net.minecraft.world.level.storage.loot.LootParams.Builder(
                     serverLevel
                  )
                  .m_287286_(LootContextParams.f_81460_, Vec3.m_82512_(mainPos))
                  .m_287286_(LootContextParams.f_81463_, ItemStack.f_41583_)
                  .m_287289_(LootContextParams.f_81462_, mainState.m_155947_() ? WorldUtils.getTileEntity(serverLevel, mainPos) : null)
                  .m_287289_(LootContextParams.f_81455_, explosion.getExploder());
               if (explosion.f_46010_ == BlockInteraction.DESTROY_WITH_DECAY) {
                  lootContextBuilder.m_287286_(LootContextParams.f_81464_, explosion.f_46017_);
               }

               mainState.m_287290_(lootContextBuilder).forEach(stack -> Block.m_49840_(serverLevel, mainPos, stack));
            }

            mainState.onBlockExploded(world, mainPos, explosion);
         }
      }

      super.onBlockExploded(state, world, pos, explosion);
   }

   @Deprecated
   public void m_213646_(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull ItemStack stack, boolean dropExperience) {
      BlockPos mainPos = getMainBlockPos(level, pos);
      if (mainPos != null) {
         BlockState mainState = level.m_8055_(mainPos);
         if (!mainState.m_60795_()) {
            mainState.m_222967_(level, mainPos, stack, dropExperience);
         }
      }

      super.m_213646_(state, level, pos, stack, dropExperience);
   }

   public void m_6240_(@NotNull Level world, @NotNull Player player, @NotNull BlockPos pos, @NotNull BlockState state, BlockEntity te, @NotNull ItemStack stack) {
      BlockPos mainPos = getMainBlockPos(world, pos);
      if (mainPos != null) {
         BlockState mainState = world.m_8055_(mainPos);
         mainState.m_60734_().m_6240_(world, player, mainPos, mainState, WorldUtils.getTileEntity(world, mainPos), stack);
      } else {
         super.m_6240_(world, player, pos, state, te, stack);
      }

      world.m_7471_(pos, false);
   }

   @Deprecated
   public void m_6861_(
      @NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Block neighborBlock, @NotNull BlockPos neighborPos, boolean isMoving
   ) {
      if (!world.f_46443_) {
         TileEntityBoundingBlock tile = WorldUtils.getTileEntity(TileEntityBoundingBlock.class, world, pos);
         if (tile != null) {
            tile.onNeighborChange(neighborBlock, neighborPos);
         }
      }

      BlockPos mainPos = getMainBlockPos(world, pos);
      if (mainPos != null) {
         world.m_8055_(mainPos).m_60690_(world, mainPos, neighborBlock, neighborPos, isMoving);
      }
   }

   @Deprecated
   public boolean m_7278_(@NotNull BlockState blockState) {
      return true;
   }

   @Deprecated
   public int m_6782_(@NotNull BlockState blockState, @NotNull Level world, @NotNull BlockPos pos) {
      if (!world.f_46443_) {
         TileEntityBoundingBlock tile = WorldUtils.getTileEntity(TileEntityBoundingBlock.class, world, pos);
         if (tile != null) {
            return tile.getComparatorSignal();
         }
      }

      return 0;
   }

   @Deprecated
   public float m_5880_(@NotNull BlockState state, @NotNull Player player, @NotNull BlockGetter world, @NotNull BlockPos pos) {
      BlockPos mainPos = getMainBlockPos(world, pos);
      return mainPos == null ? super.m_5880_(state, player, world, pos) : world.m_8055_(mainPos).m_60625_(player, world, mainPos);
   }

   public float getExplosionResistance(BlockState state, BlockGetter world, BlockPos pos, Explosion explosion) {
      BlockPos mainPos = getMainBlockPos(world, pos);
      return mainPos == null
         ? super.getExplosionResistance(state, world, pos, explosion)
         : world.m_8055_(mainPos).getExplosionResistance(world, mainPos, explosion);
   }

   @Deprecated
   @NotNull
   public RenderShape m_7514_(@NotNull BlockState state) {
      return RenderShape.INVISIBLE;
   }

   @Deprecated
   public boolean m_8133_(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, int id, int param) {
      super.m_8133_(state, level, pos, id, param);
      return this.triggerBlockEntityEvent(state, level, pos, id, param);
   }

   @Override
   public TileEntityTypeRegistryObject<TileEntityBoundingBlock> getTileType() {
      return MekanismTileEntityTypes.BOUNDING_BLOCK;
   }

   @Deprecated
   @NotNull
   public VoxelShape m_5940_(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
      return this.proxyShape(world, pos, context, BlockStateBase::m_60651_);
   }

   @Deprecated
   @NotNull
   public VoxelShape m_5939_(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
      return this.proxyShape(world, pos, context, BlockStateBase::m_60742_);
   }

   @Deprecated
   @NotNull
   public VoxelShape m_5909_(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
      return this.proxyShape(world, pos, context, BlockStateBase::m_60771_);
   }

   @Deprecated
   @NotNull
   public VoxelShape m_7952_(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos) {
      return this.proxyShape(world, pos, null, (s, level, p, ctx) -> s.m_60768_(level, p));
   }

   @Deprecated
   @NotNull
   public VoxelShape m_7947_(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos) {
      return this.proxyShape(world, pos, null, (s, level, p, ctx) -> s.m_60816_(level, p));
   }

   @Deprecated
   @NotNull
   public VoxelShape m_6079_(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos) {
      return this.proxyShape(world, pos, null, (s, level, p, ctx) -> s.m_60820_(level, p));
   }

   private VoxelShape proxyShape(BlockGetter world, BlockPos pos, @Nullable CollisionContext context, BlockBounding.ShapeProxy proxy) {
      BlockPos mainPos = getMainBlockPos(world, pos);
      if (mainPos == null) {
         return Shapes.m_83040_();
      } else {
         BlockState mainState;
         try {
            mainState = world.m_8055_(mainPos);
         } catch (ArrayIndexOutOfBoundsException var9) {
            if (!(world instanceof RenderChunkRegion region)) {
               Mekanism.logger
                  .error(
                     "Error getting bounding block shape, for position {}, with main position {}. World of type {}",
                     new Object[]{pos, mainPos, world.getClass().getName()}
                  );
               return Shapes.m_83040_();
            }

            world = region.f_112908_;
            mainState = world.m_8055_(mainPos);
         }

         VoxelShape shape = proxy.getShape(mainState, world, mainPos, context);
         BlockPos offset = pos.m_121996_(mainPos);
         return shape.m_83216_(-offset.m_123341_(), -offset.m_123342_(), -offset.m_123343_());
      }
   }

   @Deprecated
   @NotNull
   public FluidState m_5888_(@NotNull BlockState state) {
      return this.getFluid(state);
   }

   @Deprecated
   @NotNull
   public BlockState m_7417_(
      @NotNull BlockState state,
      @NotNull Direction facing,
      @NotNull BlockState facingState,
      @NotNull LevelAccessor world,
      @NotNull BlockPos currentPos,
      @NotNull BlockPos facingPos
   ) {
      this.updateFluids(state, world, currentPos);
      return super.m_7417_(state, facing, facingState, world, currentPos, facingPos);
   }

   @Deprecated
   public boolean m_7357_(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull PathComputationType type) {
      return false;
   }

   private interface ShapeProxy {
      VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context);
   }
}
