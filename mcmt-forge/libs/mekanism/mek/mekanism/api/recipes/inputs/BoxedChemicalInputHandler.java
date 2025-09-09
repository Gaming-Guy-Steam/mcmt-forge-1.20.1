package mekanism.api.recipes.inputs;

import java.util.Objects;
import mekanism.api.Action;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.chemical.merged.MergedChemicalTank;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.math.MathUtils;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;

@NothingNullByDefault
public class BoxedChemicalInputHandler {
   private final MergedChemicalTank chemicalTank;
   private final CachedRecipe.OperationTracker.RecipeError notEnoughError;

   public BoxedChemicalInputHandler(MergedChemicalTank chemicalTank, CachedRecipe.OperationTracker.RecipeError notEnoughError) {
      this.chemicalTank = Objects.requireNonNull(chemicalTank, "Tank cannot be null.");
      this.notEnoughError = Objects.requireNonNull(notEnoughError, "Not enough input error cannot be null.");
   }

   public BoxedChemicalStack getInput() {
      MergedChemicalTank.Current current = this.chemicalTank.getCurrent();
      return current == MergedChemicalTank.Current.EMPTY
         ? BoxedChemicalStack.EMPTY
         : BoxedChemicalStack.box(this.chemicalTank.getTankFromCurrent(current).getStack());
   }

   public BoxedChemicalStack getRecipeInput(ChemicalStackIngredient<?, ?> recipeIngredient) {
      BoxedChemicalStack input = this.getInput();
      if (input.isEmpty()) {
         return BoxedChemicalStack.EMPTY;
      } else {
         if (recipeIngredient instanceof ChemicalStackIngredient.GasStackIngredient ingredient) {
            if (input.getChemicalType() == ChemicalType.GAS) {
               return BoxedChemicalStack.box(ingredient.getMatchingInstance((GasStack)input.getChemicalStack()));
            }
         } else if (recipeIngredient instanceof ChemicalStackIngredient.InfusionStackIngredient ingredientx) {
            if (input.getChemicalType() == ChemicalType.INFUSION) {
               return BoxedChemicalStack.box(ingredientx.getMatchingInstance((InfusionStack)input.getChemicalStack()));
            }
         } else if (recipeIngredient instanceof ChemicalStackIngredient.PigmentStackIngredient ingredientxx) {
            if (input.getChemicalType() == ChemicalType.PIGMENT) {
               return BoxedChemicalStack.box(ingredientxx.getMatchingInstance((PigmentStack)input.getChemicalStack()));
            }
         } else {
            if (!(recipeIngredient instanceof ChemicalStackIngredient.SlurryStackIngredient ingredientxxx)) {
               throw new IllegalStateException("Unknown Chemical Type");
            }

            if (input.getChemicalType() == ChemicalType.SLURRY) {
               return BoxedChemicalStack.box(ingredientxxx.getMatchingInstance((SlurryStack)input.getChemicalStack()));
            }
         }

         return BoxedChemicalStack.EMPTY;
      }
   }

   public void use(BoxedChemicalStack recipeInput, long operations) {
      if (operations != 0L && !recipeInput.isEmpty()) {
         BoxedChemicalStack inputGas = this.getInput();
         if (!inputGas.isEmpty()) {
            long amount = recipeInput.getChemicalStack().getAmount() * operations;
            logMismatchedStackSize(this.chemicalTank.getTankForType(inputGas.getChemicalType()).shrinkStack(amount, Action.EXECUTE), amount);
         }
      }
   }

   public void calculateOperationsCanSupport(CachedRecipe.OperationTracker tracker, BoxedChemicalStack recipeInput) {
      this.calculateOperationsCanSupport(tracker, recipeInput, 1L);
   }

   public void calculateOperationsCanSupport(CachedRecipe.OperationTracker tracker, BoxedChemicalStack recipeInput, long usageMultiplier) {
      if (usageMultiplier > 0L) {
         if (!recipeInput.isEmpty()) {
            int operations = MathUtils.clampToInt(
               this.getInput().getChemicalStack().getAmount() / (recipeInput.getChemicalStack().getAmount() * usageMultiplier)
            );
            if (operations > 0) {
               tracker.updateOperations(operations);
               return;
            }
         }

         tracker.resetProgress(this.notEnoughError);
      }
   }

   private static void logMismatchedStackSize(long actual, long expected) {
      if (expected != actual) {
         MekanismAPI.logger.error("Stack size changed by a different amount ({}) than requested ({}).", new Object[]{actual, expected, new Exception()});
      }
   }
}
