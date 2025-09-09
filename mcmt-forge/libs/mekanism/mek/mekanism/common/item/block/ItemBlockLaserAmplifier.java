package mekanism.common.item.block;

import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.tile.laser.TileEntityLaserAmplifier;

public class ItemBlockLaserAmplifier extends ItemBlockTooltip<BlockTile.BlockTileModel<TileEntityLaserAmplifier, BlockTypeTile<TileEntityLaserAmplifier>>> {
   public ItemBlockLaserAmplifier(BlockTile.BlockTileModel<TileEntityLaserAmplifier, BlockTypeTile<TileEntityLaserAmplifier>> block) {
      super(block);
   }

   @Override
   protected Predicate<AutomationType> getEnergyCapInsertPredicate() {
      return BasicEnergyContainer.manualOnly;
   }
}
