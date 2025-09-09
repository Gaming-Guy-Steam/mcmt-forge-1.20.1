package mekanism.common.content.matrix;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import java.util.ArrayList;
import java.util.List;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.lib.multiblock.CuboidStructureValidator;
import mekanism.common.lib.multiblock.FormationProtocol;
import mekanism.common.registries.MekanismBlockTypes;
import mekanism.common.tile.multiblock.TileEntityInductionCell;
import mekanism.common.tile.multiblock.TileEntityInductionProvider;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class MatrixValidator extends CuboidStructureValidator<MatrixMultiblockData> {
   private final List<TileEntityInductionCell> cells = new ArrayList<>();
   private final List<TileEntityInductionProvider> providers = new ArrayList<>();

   @Override
   protected FormationProtocol.CasingType getCasingType(BlockState state) {
      Block block = state.m_60734_();
      if (BlockType.is(block, MekanismBlockTypes.INDUCTION_CASING)) {
         return FormationProtocol.CasingType.FRAME;
      } else {
         return BlockType.is(block, MekanismBlockTypes.INDUCTION_PORT) ? FormationProtocol.CasingType.VALVE : FormationProtocol.CasingType.INVALID;
      }
   }

   @Override
   public boolean validateInner(BlockState state, Long2ObjectMap<ChunkAccess> chunkMap, BlockPos pos) {
      if (super.validateInner(state, chunkMap, pos)) {
         return true;
      } else {
         if (BlockType.is(
            state.m_60734_(),
            MekanismBlockTypes.BASIC_INDUCTION_CELL,
            MekanismBlockTypes.ADVANCED_INDUCTION_CELL,
            MekanismBlockTypes.ELITE_INDUCTION_CELL,
            MekanismBlockTypes.ULTIMATE_INDUCTION_CELL,
            MekanismBlockTypes.BASIC_INDUCTION_PROVIDER,
            MekanismBlockTypes.ADVANCED_INDUCTION_PROVIDER,
            MekanismBlockTypes.ELITE_INDUCTION_PROVIDER,
            MekanismBlockTypes.ULTIMATE_INDUCTION_PROVIDER
         )) {
            BlockEntity tile = WorldUtils.getTileEntity(this.world, chunkMap, pos);
            if (tile instanceof TileEntityInductionCell cell) {
               this.cells.add(cell);
               return true;
            }

            if (tile instanceof TileEntityInductionProvider provider) {
               this.providers.add(provider);
               return true;
            }
         }

         return false;
      }
   }

   public FormationProtocol.FormationResult postcheck(MatrixMultiblockData structure, Long2ObjectMap<ChunkAccess> chunkMap) {
      this.cells.forEach(structure::addCell);
      this.providers.forEach(structure::addProvider);
      return FormationProtocol.FormationResult.SUCCESS;
   }
}
