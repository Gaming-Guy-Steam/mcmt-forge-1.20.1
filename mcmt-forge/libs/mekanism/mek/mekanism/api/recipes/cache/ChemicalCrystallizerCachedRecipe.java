package mekanism.api.recipes.cache;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.inputs.BoxedChemicalInputHandler;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.minecraft.world.item.ItemStack;

@NothingNullByDefault
public class ChemicalCrystallizerCachedRecipe extends CachedRecipe<ChemicalCrystallizerRecipe> {
   private final IOutputHandler<ItemStack> outputHandler;
   private final BoxedChemicalInputHandler inputHandler;
   private BoxedChemicalStack recipeInput = BoxedChemicalStack.EMPTY;
   private ItemStack output = ItemStack.f_41583_;

   public ChemicalCrystallizerCachedRecipe(
      ChemicalCrystallizerRecipe recipe, BooleanSupplier recheckAllErrors, BoxedChemicalInputHandler inputHandler, IOutputHandler<ItemStack> outputHandler
   ) {
      super(recipe, recheckAllErrors);
      this.inputHandler = Objects.requireNonNull(inputHandler, "Input handler cannot be null.");
      this.outputHandler = Objects.requireNonNull(outputHandler, "Output handler cannot be null.");
   }

   @Override
   protected void calculateOperationsThisTick(CachedRecipe.OperationTracker tracker) {
      super.calculateOperationsThisTick(tracker);
      if (tracker.shouldContinueChecking()) {
         this.recipeInput = this.inputHandler.getRecipeInput(this.recipe.getInput());
         if (this.recipeInput.isEmpty()) {
            tracker.mismatchedRecipe();
         } else {
            this.inputHandler.calculateOperationsCanSupport(tracker, this.recipeInput);
            if (tracker.shouldContinueChecking()) {
               this.output = this.recipe.getOutput(this.recipeInput);
               this.outputHandler.calculateOperationsCanSupport(tracker, this.output);
            }
         }
      }
   }

   @Override
   public boolean isInputValid() {
      BoxedChemicalStack input = this.inputHandler.getInput();
      return !input.isEmpty() && this.recipe.test(input);
   }

   @Override
   protected void finishProcessing(int operations) {
      if (!this.recipeInput.isEmpty() && !this.output.m_41619_()) {
         this.inputHandler.use(this.recipeInput, operations);
         this.outputHandler.handleOutput(this.output, operations);
      }
   }
}
