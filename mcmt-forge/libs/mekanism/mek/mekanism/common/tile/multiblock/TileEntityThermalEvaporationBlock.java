package mekanism.common.tile.multiblock;

import mekanism.api.providers.IBlockProvider;
import mekanism.common.Mekanism;
import mekanism.common.content.evaporation.EvaporationMultiblockData;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityThermalEvaporationBlock extends TileEntityMultiblock<EvaporationMultiblockData> {
   public TileEntityThermalEvaporationBlock(BlockPos pos, BlockState state) {
      this(MekanismBlocks.THERMAL_EVAPORATION_BLOCK, pos, state);
   }

   public TileEntityThermalEvaporationBlock(IBlockProvider provider, BlockPos pos, BlockState state) {
      super(provider, pos, state);
   }

   @Override
   public void onNeighborChange(Block block, BlockPos neighborPos) {
      super.onNeighborChange(block, neighborPos);
      if (!this.isRemote() && WorldUtils.sideDifference(this.f_58858_, neighborPos) == Direction.DOWN) {
         EvaporationMultiblockData multiblock = this.getMultiblock();
         if (multiblock.isFormed()) {
            multiblock.updateSolarSpot(this.m_58904_(), neighborPos);
         }
      }
   }

   public EvaporationMultiblockData createMultiblock() {
      return new EvaporationMultiblockData(this);
   }

   @Override
   public MultiblockManager<EvaporationMultiblockData> getManager() {
      return Mekanism.evaporationManager;
   }

   @Override
   public boolean canBeMaster() {
      return false;
   }
}
