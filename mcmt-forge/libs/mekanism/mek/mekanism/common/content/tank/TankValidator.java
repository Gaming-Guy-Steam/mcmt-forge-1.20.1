package mekanism.common.content.tank;

import mekanism.common.content.blocktype.BlockType;
import mekanism.common.lib.multiblock.CuboidStructureValidator;
import mekanism.common.lib.multiblock.FormationProtocol;
import mekanism.common.registries.MekanismBlockTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class TankValidator extends CuboidStructureValidator<TankMultiblockData> {
   @Override
   protected FormationProtocol.CasingType getCasingType(BlockState state) {
      Block block = state.m_60734_();
      if (BlockType.is(block, MekanismBlockTypes.DYNAMIC_TANK)) {
         return FormationProtocol.CasingType.FRAME;
      } else {
         return BlockType.is(block, MekanismBlockTypes.DYNAMIC_VALVE) ? FormationProtocol.CasingType.VALVE : FormationProtocol.CasingType.INVALID;
      }
   }
}
