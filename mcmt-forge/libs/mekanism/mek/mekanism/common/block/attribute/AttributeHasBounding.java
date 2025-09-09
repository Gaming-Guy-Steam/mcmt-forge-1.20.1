package mekanism.common.block.attribute;

import java.util.stream.Stream;
import java.util.stream.Stream.Builder;
import mekanism.api.functions.TriConsumer;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockBounding;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.util.RegistryUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class AttributeHasBounding implements Attribute {
   private final TriConsumer<BlockPos, BlockState, Builder<BlockPos>> boundingPositions;

   public AttributeHasBounding(TriConsumer<BlockPos, BlockState, Builder<BlockPos>> boundingPositions) {
      this.boundingPositions = boundingPositions;
   }

   public void removeBoundingBlocks(Level level, BlockPos pos, BlockState state) {
      this.getPositions(pos, state)
         .forEach(
            p -> {
               BlockState boundingState = level.m_8055_(p);
               if (!boundingState.m_60795_()) {
                  if (boundingState.m_60713_(MekanismBlocks.BOUNDING_BLOCK.getBlock())) {
                     level.m_7471_(p, false);
                  } else {
                     Mekanism.logger
                        .warn(
                           "Skipping removing block, expected bounding block but the block at {} in {} was {}",
                           new Object[]{p, level.m_46472_().m_135782_(), RegistryUtils.getName(boundingState.m_60734_())}
                        );
                  }
               }
            }
         );
   }

   public void placeBoundingBlocks(Level level, BlockPos orig, BlockState state) {
      this.getPositions(orig, state).forEach(boundingLocation -> {
         BlockBounding boundingBlock = MekanismBlocks.BOUNDING_BLOCK.getBlock();
         BlockState newState = BlockStateHelper.getStateForPlacement(boundingBlock, boundingBlock.m_49966_(), level, boundingLocation, null, Direction.NORTH);
         level.m_7731_(boundingLocation, newState, 3);
         if (!level.m_5776_()) {
            TileEntityBoundingBlock tile = WorldUtils.getTileEntity(TileEntityBoundingBlock.class, level, boundingLocation);
            if (tile != null) {
               tile.setMainLocation(orig);
            } else {
               Mekanism.logger.warn("Unable to find Bounding Block Tile at: {}", boundingLocation);
            }
         }
      });
   }

   public Stream<BlockPos> getPositions(BlockPos pos, BlockState state) {
      Builder<BlockPos> builder = Stream.builder();
      this.boundingPositions.accept(pos, state, builder);
      return builder.build();
   }
}
