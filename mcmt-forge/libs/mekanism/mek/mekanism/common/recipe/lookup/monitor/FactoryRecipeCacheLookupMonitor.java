package mekanism.common.recipe.lookup.monitor;

import mekanism.api.recipes.MekanismRecipe;
import mekanism.common.recipe.lookup.IRecipeLookupHandler;
import org.jetbrains.annotations.NotNull;

public class FactoryRecipeCacheLookupMonitor<RECIPE extends MekanismRecipe> extends RecipeCacheLookupMonitor<RECIPE> {
   private final Runnable setSortingNeeded;

   public FactoryRecipeCacheLookupMonitor(IRecipeLookupHandler<RECIPE> handler, int cacheIndex, Runnable setSortingNeeded) {
      super(handler, cacheIndex);
      this.setSortingNeeded = setSortingNeeded;
   }

   @Override
   public void onChange() {
      super.onChange();
      this.setSortingNeeded.run();
   }

   public void updateCachedRecipe(@NotNull RECIPE recipe) {
      this.cachedRecipe = this.createNewCachedRecipe(recipe, this.cacheIndex);
      this.hasNoRecipe = false;
   }
}
