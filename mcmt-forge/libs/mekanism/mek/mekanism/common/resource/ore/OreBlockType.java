package mekanism.common.resource.ore;

import mekanism.common.block.BlockOre;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.registration.impl.BlockRegistryObject;

public record OreBlockType(BlockRegistryObject<BlockOre, ItemBlockTooltip<BlockOre>> stone, BlockRegistryObject<BlockOre, ItemBlockTooltip<BlockOre>> deepslate) {
   public BlockOre stoneBlock() {
      return this.stone.getBlock();
   }

   public BlockOre deepslateBlock() {
      return this.deepslate.getBlock();
   }
}
