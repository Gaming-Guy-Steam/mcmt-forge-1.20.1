package mekanism.common.util;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import mekanism.common.Mekanism;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WorldUtils {
   @Contract("null, _ -> false")
   public static boolean isChunkLoaded(@Nullable LevelReader world, @NotNull BlockPos pos) {
      return isChunkLoaded(world, SectionPos.m_123171_(pos.m_123341_()), SectionPos.m_123171_(pos.m_123343_()));
   }

   @Contract("null, _ -> false")
   public static boolean isChunkLoaded(@Nullable LevelReader world, ChunkPos chunkPos) {
      return isChunkLoaded(world, chunkPos.f_45578_, chunkPos.f_45579_);
   }

   @Contract("null, _, _ -> false")
   public static boolean isChunkLoaded(@Nullable LevelReader world, int chunkX, int chunkZ) {
      if (world == null) {
         return false;
      } else {
         return !(world instanceof LevelAccessor accessor && (!(accessor instanceof Level level) || !level.f_46443_))
            ? world.m_6522_(chunkX, chunkZ, ChunkStatus.f_62326_, false) != null
            : accessor.m_7232_(chunkX, chunkZ);
      }
   }

   @Contract("null, _ -> false")
   public static boolean isBlockLoaded(@Nullable BlockGetter world, @NotNull BlockPos pos) {
      if (world == null) {
         return false;
      } else if (world instanceof LevelReader reader) {
         return reader instanceof Level level && !level.m_46739_(pos) ? false : isChunkLoaded(reader, pos);
      } else {
         return true;
      }
   }

   @Contract("null, _ -> false")
   public static boolean isBlockInBounds(@Nullable BlockGetter world, @NotNull BlockPos pos) {
      if (world == null) {
         return false;
      } else {
         return !(world instanceof LevelReader reader) ? true : !(reader instanceof Level level && !level.m_46739_(pos));
      }
   }

   @Nullable
   @Contract("null, _, _ -> null")
   private static ChunkAccess getChunkForPos(@Nullable LevelAccessor world, @NotNull Long2ObjectMap<ChunkAccess> chunkMap, @NotNull BlockPos pos) {
      if (!isBlockInBounds(world, pos)) {
         return null;
      } else {
         int chunkX = SectionPos.m_123171_(pos.m_123341_());
         int chunkZ = SectionPos.m_123171_(pos.m_123343_());
         long combinedChunk = ChunkPos.m_45589_(chunkX, chunkZ);
         ChunkAccess chunk = (ChunkAccess)chunkMap.get(combinedChunk);
         if (chunk == null) {
            chunk = world.m_6522_(chunkX, chunkZ, ChunkStatus.f_62326_, false);
            if (chunk != null) {
               chunkMap.put(combinedChunk, chunk);
            }
         }

         return chunk;
      }
   }

   @NotNull
   public static Optional<BlockState> getBlockState(@Nullable LevelAccessor world, @NotNull Long2ObjectMap<ChunkAccess> chunkMap, @NotNull BlockPos pos) {
      return getBlockState(getChunkForPos(world, chunkMap, pos), pos);
   }

   @NotNull
   public static Optional<BlockState> getBlockState(@Nullable BlockGetter world, @NotNull BlockPos pos) {
      return !isBlockLoaded(world, pos) ? Optional.empty() : Optional.of(world.m_8055_(pos));
   }

   @NotNull
   public static Optional<FluidState> getFluidState(@Nullable LevelAccessor world, @NotNull Long2ObjectMap<ChunkAccess> chunkMap, @NotNull BlockPos pos) {
      return getFluidState(getChunkForPos(world, chunkMap, pos), pos);
   }

   @NotNull
   public static Optional<FluidState> getFluidState(@Nullable BlockGetter world, @NotNull BlockPos pos) {
      return !isBlockLoaded(world, pos) ? Optional.empty() : Optional.of(world.m_6425_(pos));
   }

   @Nullable
   @Contract("null, _, _ -> null")
   public static BlockEntity getTileEntity(@Nullable LevelAccessor world, @NotNull Long2ObjectMap<ChunkAccess> chunkMap, @NotNull BlockPos pos) {
      return getTileEntity(getChunkForPos(world, chunkMap, pos), pos);
   }

   @Nullable
   @Contract("_, null, _, _ -> null")
   public static <T extends BlockEntity> T getTileEntity(
      @NotNull Class<T> clazz, @Nullable LevelAccessor world, @NotNull Long2ObjectMap<ChunkAccess> chunkMap, @NotNull BlockPos pos
   ) {
      return getTileEntity(clazz, world, chunkMap, pos, false);
   }

   @Nullable
   @Contract("_, null, _, _, _ -> null")
   public static <T extends BlockEntity> T getTileEntity(
      @NotNull Class<T> clazz, @Nullable LevelAccessor world, @NotNull Long2ObjectMap<ChunkAccess> chunkMap, @NotNull BlockPos pos, boolean logWrongType
   ) {
      return getTileEntity(clazz, getChunkForPos(world, chunkMap, pos), pos, logWrongType);
   }

   @Nullable
   @Contract("null, _ -> null")
   public static BlockEntity getTileEntity(@Nullable BlockGetter world, @NotNull BlockPos pos) {
      return !isBlockLoaded(world, pos) ? null : world.m_7702_(pos);
   }

   @Nullable
   @Contract("_, null, _ -> null")
   public static <T extends BlockEntity> T getTileEntity(@NotNull Class<T> clazz, @Nullable BlockGetter world, @NotNull BlockPos pos) {
      return getTileEntity(clazz, world, pos, false);
   }

   @Nullable
   @Contract("_, null, _, _ -> null")
   public static <T extends BlockEntity> T getTileEntity(@NotNull Class<T> clazz, @Nullable BlockGetter world, @NotNull BlockPos pos, boolean logWrongType) {
      BlockEntity tile = getTileEntity(world, pos);
      if (tile == null) {
         return null;
      } else if (clazz.isInstance(tile)) {
         return clazz.cast(tile);
      } else {
         if (logWrongType) {
            Mekanism.logger.warn("Unexpected BlockEntity class at {}, expected {}, but found: {}", new Object[]{pos, clazz, tile.getClass()});
         }

         return null;
      }
   }

   public static void saveChunk(BlockEntity tile) {
      if (tile != null && !tile.m_58901_() && tile.m_58904_() != null) {
         markChunkDirty(tile.m_58904_(), tile.m_58899_());
      }
   }

   public static void markChunkDirty(Level world, BlockPos pos) {
      if (isBlockLoaded(world, pos)) {
         world.m_46745_(pos).m_8092_(true);
      }
   }

   public static void dismantleBlock(BlockState state, Level world, BlockPos pos) {
      dismantleBlock(state, world, pos, getTileEntity(world, pos));
   }

   public static void dismantleBlock(BlockState state, Level world, BlockPos pos, @Nullable BlockEntity tile) {
      if (world instanceof ServerLevel level) {
         Block.m_49869_(state, level, pos, tile).forEach(stack -> Block.m_49840_(world, pos, stack));
         state.m_222967_(level, pos, ItemStack.f_41583_, false);
      }

      world.m_7471_(pos, false);
   }

   public static double distanceBetween(BlockPos start, BlockPos end) {
      return Math.sqrt(start.m_123331_(end));
   }

   public static Direction sideDifference(BlockPos pos, BlockPos other) {
      BlockPos diff = pos.m_121996_(other);

      for (Direction side : EnumUtils.DIRECTIONS) {
         if (side.m_122429_() == diff.m_123341_() && side.m_122430_() == diff.m_123342_() && side.m_122431_() == diff.m_123343_()) {
            return side;
         }
      }

      return null;
   }

   public static boolean isChunkVibrated(ChunkPos chunk, Level world) {
      return Mekanism.activeVibrators
         .stream()
         .anyMatch(
            coord -> coord.dimension == world.m_46472_()
               && SectionPos.m_123171_(coord.getX()) == chunk.f_45578_
               && SectionPos.m_123171_(coord.getZ()) == chunk.f_45579_
         );
   }

   public static boolean tryPlaceContainedLiquid(@Nullable Player player, Level world, BlockPos pos, @NotNull FluidStack fluidStack, @Nullable Direction side) {
      Fluid fluid = fluidStack.getFluid();
      FluidType fluidType = fluid.getFluidType();
      if (!fluidType.canBePlacedInLevel(world, pos, fluidStack)) {
         return false;
      } else {
         BlockState state = world.m_8055_(pos);
         boolean isReplaceable = state.m_60722_(fluid);
         boolean canContainFluid = state.m_60734_() instanceof LiquidBlockContainer liquidBlockContainer
            && liquidBlockContainer.m_6044_(world, pos, state, fluid);
         if (!state.m_60795_() && !isReplaceable && !canContainFluid) {
            return side != null && tryPlaceContainedLiquid(player, world, pos.m_121945_(side), fluidStack, null);
         } else {
            if (world.m_6042_().f_63857_() && fluidType.isVaporizedOnPlacement(world, pos, fluidStack)) {
               fluidType.onVaporize(player, world, pos, fluidStack);
            } else if (canContainFluid) {
               if (!((LiquidBlockContainer)state.m_60734_()).m_7361_(world, pos, state, fluidType.getStateForPlacement(world, pos, fluidStack))) {
                  return false;
               }

               playEmptySound(player, world, pos, fluidType, fluidStack);
            } else {
               if (!world.m_5776_() && isReplaceable && !state.m_278721_()) {
                  world.m_46961_(pos, true);
               }

               playEmptySound(player, world, pos, fluidType, fluidStack);
               world.m_7731_(pos, fluid.m_76145_().m_76188_(), 11);
            }

            return true;
         }
      }
   }

   private static void playEmptySound(@Nullable Player player, LevelAccessor world, BlockPos pos, FluidType fluidType, @NotNull FluidStack fluidStack) {
      SoundEvent soundevent = fluidType.getSound(player, world, pos, SoundActions.BUCKET_EMPTY);
      if (soundevent == null) {
         soundevent = MekanismTags.Fluids.LAVA_LOOKUP.contains(fluidStack.getFluid()) ? SoundEvents.f_11780_ : SoundEvents.f_11778_;
      }

      world.m_5594_(player, pos, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
   }

   public static void playFillSound(@Nullable Player player, LevelAccessor world, BlockPos pos, @NotNull FluidStack fluidStack, @Nullable SoundEvent soundEvent) {
      if (soundEvent == null) {
         Fluid fluid = fluidStack.getFluid();
         soundEvent = fluid.m_142520_().orElseGet(() -> fluid.getFluidType().getSound(player, world, pos, SoundActions.BUCKET_FILL));
      }

      if (soundEvent != null) {
         world.m_5594_(player, pos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
      }
   }

   public static boolean isGettingPowered(Level world, BlockPos pos) {
      if (isBlockLoaded(world, pos)) {
         for (Direction side : EnumUtils.DIRECTIONS) {
            BlockPos offset = pos.m_121945_(side);
            if (isBlockLoaded(world, offset)) {
               BlockState blockState = world.m_8055_(offset);
               boolean weakPower = blockState.m_60734_().shouldCheckWeakPower(blockState, world, pos, side);
               if (weakPower && isDirectlyGettingPowered(world, offset) || !weakPower && blockState.m_60746_(world, offset, side) > 0) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   public static boolean isDirectlyGettingPowered(Level world, BlockPos pos) {
      for (Direction side : EnumUtils.DIRECTIONS) {
         BlockPos offset = pos.m_121945_(side);
         if (isBlockLoaded(world, offset) && world.m_277185_(pos, side) > 0) {
            return true;
         }
      }

      return false;
   }

   public static boolean areBlocksValidAndReplaceable(@NotNull BlockGetter world, @NotNull BlockPos... positions) {
      return areBlocksValidAndReplaceable(world, Arrays.stream(positions));
   }

   public static boolean areBlocksValidAndReplaceable(@NotNull BlockGetter world, @NotNull Collection<BlockPos> positions) {
      return areBlocksValidAndReplaceable(world, positions.stream());
   }

   public static boolean areBlocksValidAndReplaceable(@NotNull BlockGetter world, @NotNull Stream<BlockPos> positions) {
      return positions.allMatch(pos -> isValidReplaceableBlock(world, pos));
   }

   public static boolean isValidReplaceableBlock(@NotNull BlockGetter world, @NotNull BlockPos pos) {
      return isBlockInBounds(world, pos) && world.m_8055_(pos).m_247087_();
   }

   public static void notifyLoadedNeighborsOfTileChange(Level world, BlockPos pos) {
      BlockState state = world.m_8055_(pos);

      for (Direction dir : EnumUtils.DIRECTIONS) {
         BlockPos offset = pos.m_121945_(dir);
         if (isBlockLoaded(world, offset)) {
            notifyNeighborOfChange(world, offset, pos);
            if (world.m_8055_(offset).m_60796_(world, offset)) {
               offset = offset.m_121945_(dir);
               if (isBlockLoaded(world, offset)) {
                  Block block1 = world.m_8055_(offset).m_60734_();
                  if (block1.getWeakChanges(state, world, offset)) {
                     block1.onNeighborChange(state, world, offset, pos);
                  }
               }
            }
         }
      }
   }

   public static void notifyNeighborsOfChange(@Nullable Level world, BlockPos fromPos, Set<Direction> neighbors) {
      if (!neighbors.isEmpty()) {
         getBlockState(world, fromPos).ifPresent(sourceState -> {
            for (Direction neighbor : neighbors) {
               BlockPos pos = fromPos.m_121945_(neighbor);
               getBlockState(world, pos).ifPresent(state -> {
                  state.onNeighborChange(world, pos, fromPos);
                  state.m_60690_(world, pos, sourceState.m_60734_(), fromPos, false);
               });
            }
         });
      }
   }

   public static void notifyNeighborOfChange(@Nullable Level world, BlockPos pos, BlockPos fromPos) {
      getBlockState(world, pos).ifPresent(state -> {
         state.onNeighborChange(world, pos, fromPos);
         state.m_60690_(world, pos, world.m_8055_(fromPos).m_60734_(), fromPos, false);
      });
   }

   public static void notifyNeighborOfChange(@Nullable Level world, Direction neighborSide, BlockPos fromPos) {
      notifyNeighborOfChange(world, fromPos.m_121945_(neighborSide), fromPos);
   }

   public static void updateBlock(@Nullable Level world, @NotNull BlockPos pos, BlockState state) {
      if (isBlockLoaded(world, pos)) {
         world.m_7260_(pos, state, state, 3);
      }
   }

   public static void recheckLighting(@Nullable BlockAndTintGetter world, @NotNull BlockPos pos) {
      if (isBlockLoaded(world, pos)) {
         world.m_5518_().m_7174_(pos);
      }
   }

   public static float getSunBrightness(Level world, float partialTicks) {
      float f = world.m_46942_(partialTicks);
      float f1 = 1.0F - (Mth.m_14089_(f * (float) (Math.PI * 2)) * 2.0F + 0.2F);
      f1 = Mth.m_14036_(f1, 0.0F, 1.0F);
      f1 = 1.0F - f1;
      f1 = (float)(f1 * (1.0 - world.m_46722_(partialTicks) * 5.0F / 16.0));
      f1 = (float)(f1 * (1.0 - world.m_46661_(partialTicks) * 5.0F / 16.0));
      return f1 * 0.8F + 0.2F;
   }

   @Contract("null, _ -> false")
   public static boolean canSeeSun(@Nullable Level world, BlockPos pos) {
      return world != null && world.m_6042_().f_223549_() && world.m_7445_() < 4 && world.m_45527_(pos);
   }

   public static BlockPos getBlockPosFromChunkPos(long chunkPos) {
      return new BlockPos((int)chunkPos, 0, (int)(chunkPos >> 32));
   }
}
