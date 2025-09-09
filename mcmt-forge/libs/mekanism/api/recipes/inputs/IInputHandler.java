package mekanism.api.recipes.inputs;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.ingredients.InputIngredient;

@NothingNullByDefault
public interface IInputHandler<INPUT> {
   INPUT getInput();

   INPUT getRecipeInput(InputIngredient<INPUT> var1);

   void use(INPUT var1, int var2);

   default void calculateOperationsCanSupport(CachedRecipe.OperationTracker tracker, INPUT recipeInput) {
      this.calculateOperationsCanSupport(tracker, recipeInput, 1);
   }

   void calculateOperationsCanSupport(CachedRecipe.OperationTracker var1, INPUT var2, int var3);
}
