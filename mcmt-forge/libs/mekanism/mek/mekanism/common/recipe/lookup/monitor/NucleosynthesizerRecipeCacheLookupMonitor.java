package mekanism.common.recipe.lookup.monitor;

import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.recipe.lookup.IRecipeLookupHandler;

public class NucleosynthesizerRecipeCacheLookupMonitor extends RecipeCacheLookupMonitor<NucleosynthesizingRecipe> {
   public NucleosynthesizerRecipeCacheLookupMonitor(IRecipeLookupHandler<NucleosynthesizingRecipe> handler) {
      super(handler);
   }

   @Override
   public FloatingLong updateAndProcess(IEnergyContainer energyContainer) {
      if (!(energyContainer instanceof MachineEnergyContainer<?> machineEnergyContainer)) {
         return FloatingLong.ZERO;
      } else {
         FloatingLong prev = energyContainer.getEnergy().copy();
         if (!this.updateAndProcess()) {
            return FloatingLong.ZERO;
         } else {
            int toProcess = (int)Math.sqrt(prev.divide(machineEnergyContainer.getEnergyPerTick()).doubleValue());

            for (int i = 0; i < toProcess - 1; i++) {
               this.cachedRecipe.process();
            }

            return prev.minusEqual(energyContainer.getEnergy());
         }
      }
   }
}
