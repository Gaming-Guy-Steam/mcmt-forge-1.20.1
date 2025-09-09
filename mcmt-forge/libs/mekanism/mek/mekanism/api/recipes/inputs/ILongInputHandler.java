package mekanism.api.recipes.inputs;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.cache.CachedRecipe;

@NothingNullByDefault
public interface ILongInputHandler<INPUT> extends IInputHandler<INPUT> {
   @Override
   default void use(INPUT recipeInput, int operations) {
      this.use(recipeInput, (long)operations);
   }

   void use(INPUT var1, long var2);

   @Override
   default void calculateOperationsCanSupport(CachedRecipe.OperationTracker tracker, INPUT recipeInput, int usageMultiplier) {
      this.calculateOperationsCanSupport(tracker, recipeInput, (long)usageMultiplier);
   }

   void calculateOperationsCanSupport(CachedRecipe.OperationTracker var1, INPUT var2, long var3);
}
