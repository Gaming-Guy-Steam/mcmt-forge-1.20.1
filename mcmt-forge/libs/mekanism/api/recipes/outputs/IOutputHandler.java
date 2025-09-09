package mekanism.api.recipes.outputs;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.recipes.cache.CachedRecipe;

@ParametersAreNotNullByDefault
public interface IOutputHandler<OUTPUT> {
   void handleOutput(OUTPUT var1, int var2);

   void calculateOperationsCanSupport(CachedRecipe.OperationTracker var1, OUTPUT var2);
}
