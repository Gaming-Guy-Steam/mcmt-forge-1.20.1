package mekanism.api.recipes.cache;

import mekanism.api.recipes.MekanismRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ICachedRecipeHolder<RECIPE extends MekanismRecipe> {
   @Nullable
   default CachedRecipe<RECIPE> getUpdatedCache(int cacheIndex) {
      boolean cacheInvalid = this.invalidateCache();
      CachedRecipe<RECIPE> currentCache = cacheInvalid ? null : this.getCachedRecipe(cacheIndex);
      if ((currentCache == null || !currentCache.isInputValid()) && (cacheInvalid || !this.hasNoRecipe(cacheIndex))) {
         RECIPE recipe = this.getRecipe(cacheIndex);
         if (recipe == null) {
            this.setHasNoRecipe(cacheIndex);
         } else {
            CachedRecipe<RECIPE> cached = this.createNewCachedRecipe(recipe, cacheIndex);
            if (currentCache == null || cached != null) {
               if (currentCache == null && cached != null) {
                  this.loadSavedData(cached, cacheIndex);
               }

               return cached;
            }
         }
      }

      return currentCache;
   }

   default void loadSavedData(@NotNull CachedRecipe<RECIPE> cached, int cacheIndex) {
      cached.loadSavedOperatingTicks(this.getSavedOperatingTicks(cacheIndex));
   }

   default int getSavedOperatingTicks(int cacheIndex) {
      return 0;
   }

   @Nullable
   CachedRecipe<RECIPE> getCachedRecipe(int var1);

   @Nullable
   RECIPE getRecipe(int var1);

   @Nullable
   CachedRecipe<RECIPE> createNewCachedRecipe(@NotNull RECIPE var1, int var2);

   default boolean invalidateCache() {
      return false;
   }

   default void setHasNoRecipe(int cacheIndex) {
   }

   default boolean hasNoRecipe(int cacheIndex) {
      return false;
   }
}
