package mekanism.api.recipes.cache;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.chemical.ChemicalChemicalToChemicalRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.outputs.IOutputHandler;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ChemicalChemicalToChemicalCachedRecipe<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>, RECIPE extends ChemicalChemicalToChemicalRecipe<CHEMICAL, STACK, INGREDIENT>>
   extends CachedRecipe<RECIPE> {
   private final IOutputHandler<STACK> outputHandler;
   private final IInputHandler<STACK> leftInputHandler;
   private final IInputHandler<STACK> rightInputHandler;
   @Nullable
   private STACK leftRecipeInput;
   @Nullable
   private STACK rightRecipeInput;
   @Nullable
   private STACK output;

   public ChemicalChemicalToChemicalCachedRecipe(
      RECIPE recipe,
      BooleanSupplier recheckAllErrors,
      IInputHandler<STACK> leftInputHandler,
      IInputHandler<STACK> rightInputHandler,
      IOutputHandler<STACK> outputHandler
   ) {
      super(recipe, recheckAllErrors);
      this.leftInputHandler = Objects.requireNonNull(leftInputHandler, "Left input handler cannot be null.");
      this.rightInputHandler = Objects.requireNonNull(rightInputHandler, "Right input handler cannot be null.");
      this.outputHandler = Objects.requireNonNull(outputHandler, "Output handler cannot be null.");
   }

   @Override
   protected void calculateOperationsThisTick(CachedRecipe.OperationTracker tracker) {
      super.calculateOperationsThisTick(tracker);
      if (tracker.shouldContinueChecking()) {
         STACK leftInputChemical = this.leftInputHandler.getInput();
         if (leftInputChemical.isEmpty()) {
            tracker.mismatchedRecipe();
         } else {
            STACK rightInputChemical = this.rightInputHandler.getInput();
            if (rightInputChemical.isEmpty()) {
               tracker.mismatchedRecipe();
            } else {
               INGREDIENT leftInput = this.recipe.getLeftInput();
               INGREDIENT rightInput = this.recipe.getRightInput();
               Supplier<INGREDIENT> leftIngredient;
               Supplier<INGREDIENT> rightIngredient;
               if (leftInput.test(leftInputChemical) && rightInput.test(rightInputChemical)) {
                  leftIngredient = () -> leftInput;
                  rightIngredient = () -> rightInput;
               } else {
                  leftIngredient = () -> rightInput;
                  rightIngredient = () -> leftInput;
               }

               CachedRecipeHelper.twoInputCalculateOperationsThisTick(
                  tracker, this.leftInputHandler, leftIngredient, this.rightInputHandler, rightIngredient, (left, right) -> {
                     this.leftRecipeInput = left;
                     this.rightRecipeInput = right;
                  }, this.outputHandler, this.recipe::getOutput, output -> this.output = output, ChemicalStack::isEmpty, ChemicalStack::isEmpty
               );
            }
         }
      }
   }

   @Override
   public boolean isInputValid() {
      STACK leftInput = this.leftInputHandler.getInput();
      if (leftInput.isEmpty()) {
         return false;
      } else {
         STACK rightInput = this.rightInputHandler.getInput();
         return !rightInput.isEmpty() && this.recipe.test(leftInput, rightInput);
      }
   }

   @Override
   protected void finishProcessing(int operations) {
      if (this.leftRecipeInput != null
         && this.rightRecipeInput != null
         && this.output != null
         && !this.leftRecipeInput.isEmpty()
         && !this.rightRecipeInput.isEmpty()
         && !this.output.isEmpty()) {
         this.leftInputHandler.use(this.leftRecipeInput, operations);
         this.rightInputHandler.use(this.rightRecipeInput, operations);
         this.outputHandler.handleOutput(this.output, operations);
      }
   }
}
