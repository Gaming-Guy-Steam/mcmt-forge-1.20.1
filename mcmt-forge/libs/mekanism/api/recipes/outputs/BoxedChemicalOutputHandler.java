package mekanism.api.recipes.outputs;

import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.chemical.merged.MergedChemicalTank;
import mekanism.api.recipes.cache.CachedRecipe;

@NothingNullByDefault
public class BoxedChemicalOutputHandler {
   private final CachedRecipe.OperationTracker.RecipeError notEnoughSpaceError;
   private final MergedChemicalTank chemicalTank;

   public BoxedChemicalOutputHandler(MergedChemicalTank chemicalTank, CachedRecipe.OperationTracker.RecipeError notEnoughSpaceError) {
      this.chemicalTank = Objects.requireNonNull(chemicalTank, "Chemical tank cannot be null.");
      this.notEnoughSpaceError = Objects.requireNonNull(notEnoughSpaceError, "Not enough space error cannot be null.");
   }

   public void handleOutput(BoxedChemicalStack toOutput, int operations) {
      this.handleOutput(this.chemicalTank.getTankForType(toOutput.getChemicalType()), toOutput.getChemicalStack(), operations);
   }

   private <STACK extends ChemicalStack<?>> void handleOutput(IChemicalTank<?, ?> tank, STACK stack, int operations) {
      OutputHelper.handleOutput(tank, stack, operations);
   }

   public void calculateOperationsRoomFor(CachedRecipe.OperationTracker tracker, BoxedChemicalStack toOutput) {
      this.calculateOperationsRoomFor(tracker, this.chemicalTank.getTankForType(toOutput.getChemicalType()), toOutput.getChemicalStack());
   }

   private <STACK extends ChemicalStack<?>> void calculateOperationsRoomFor(CachedRecipe.OperationTracker tracker, IChemicalTank<?, ?> tank, STACK stack) {
      OutputHelper.calculateOperationsCanSupport(tracker, this.notEnoughSpaceError, tank, stack);
   }
}
