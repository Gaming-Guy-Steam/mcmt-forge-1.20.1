package mekanism.common.tile.interfaces;

import java.util.Map;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public interface ITileNeighborCache extends ITileWrapper {
   default void createNeighborCache() {
      for (Direction side : EnumUtils.DIRECTIONS) {
         this.updateNeighborCache(this.getTilePos().m_121945_(side));
      }
   }

   default void updateNeighborCache(BlockPos neighborPos) {
      BlockState state = WorldUtils.getBlockState(this.getTileWorld(), neighborPos).orElseGet(Blocks.f_50016_::m_49966_);
      this.getNeighborCache().put(neighborPos, state);
   }

   Map<BlockPos, BlockState> getNeighborCache();
}
