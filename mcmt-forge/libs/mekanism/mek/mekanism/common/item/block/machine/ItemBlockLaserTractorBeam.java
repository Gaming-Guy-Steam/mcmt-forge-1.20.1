package mekanism.common.item.block.machine;

import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.capabilities.energy.BasicEnergyContainer;

public class ItemBlockLaserTractorBeam extends ItemBlockMachine {
   public ItemBlockLaserTractorBeam(BlockTile<?, ?> block) {
      super(block);
   }

   @Override
   protected Predicate<AutomationType> getEnergyCapInsertPredicate() {
      return BasicEnergyContainer.manualOnly;
   }
}
