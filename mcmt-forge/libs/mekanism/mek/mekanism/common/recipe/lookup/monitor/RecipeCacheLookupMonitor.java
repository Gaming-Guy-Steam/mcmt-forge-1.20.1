package mekanism.common.recipe.lookup.monitor;

import mekanism.api.IContentsListener;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.ICachedRecipeHolder;
import mekanism.api.recipes.cache.ItemStackConstantChemicalToItemStackCachedRecipe;
import mekanism.common.CommonWorldTickHandler;
import mekanism.common.recipe.lookup.IRecipeLookupHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RecipeCacheLookupMonitor<RECIPE extends MekanismRecipe> implements ICachedRecipeHolder<RECIPE>, IContentsListener {
   private final IRecipeLookupHandler<RECIPE> handler;
   protected final int cacheIndex;
   protected CachedRecipe<RECIPE> cachedRecipe;
   protected boolean hasNoRecipe;

   public RecipeCacheLookupMonitor(IRecipeLookupHandler<RECIPE> handler) {
      this(handler, 0);
   }

   public RecipeCacheLookupMonitor(IRecipeLookupHandler<RECIPE> handler, int cacheIndex) {
      this.handler = handler;
      this.cacheIndex = cacheIndex;
   }

   protected boolean cachedIndexMatches(int cacheIndex) {
      return this.cacheIndex == cacheIndex;
   }

   @Override
   public final void onContentsChanged() {
      this.handler.onContentsChanged();
      this.onChange();
   }

   public void onChange() {
      this.hasNoRecipe = false;
   }

   public FloatingLong updateAndProcess(IEnergyContainer energyContainer) {
      FloatingLong prev = energyContainer.getEnergy().copy();
      return this.updateAndProcess() ? prev.minusEqual(energyContainer.getEnergy()) : FloatingLong.ZERO;
   }

   public boolean updateAndProcess() {
      CachedRecipe<RECIPE> oldCache = this.cachedRecipe;
      this.cachedRecipe = this.getUpdatedCache(this.cacheIndex);
      if (this.cachedRecipe != oldCache) {
         this.handler.onCachedRecipeChanged(this.cachedRecipe, this.cacheIndex);
      }

      if (this.cachedRecipe != null) {
         this.cachedRecipe.process();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void loadSavedData(@NotNull CachedRecipe<RECIPE> cached, int cacheIndex) {
      if (this.cachedIndexMatches(cacheIndex)) {
         ICachedRecipeHolder.super.loadSavedData(cached, cacheIndex);
         if (cached instanceof ItemStackConstantChemicalToItemStackCachedRecipe<?, ?, ?, ?> c
            && this.handler instanceof IRecipeLookupHandler.ConstantUsageRecipeLookupHandler handler) {
            c.loadSavedUsageSoFar(handler.getSavedUsedSoFar(cacheIndex));
         }
      }
   }

   @Override
   public int getSavedOperatingTicks(int cacheIndex) {
      return this.cachedIndexMatches(cacheIndex)
         ? this.handler.getSavedOperatingTicks(cacheIndex)
         : ICachedRecipeHolder.super.getSavedOperatingTicks(cacheIndex);
   }

   @Nullable
   @Override
   public CachedRecipe<RECIPE> getCachedRecipe(int cacheIndex) {
      return this.cachedIndexMatches(cacheIndex) ? this.cachedRecipe : null;
   }

   @Nullable
   @Override
   public RECIPE getRecipe(int cacheIndex) {
      return this.cachedIndexMatches(cacheIndex) ? this.handler.getRecipe(cacheIndex) : null;
   }

   @Nullable
   @Override
   public CachedRecipe<RECIPE> createNewCachedRecipe(@NotNull RECIPE recipe, int cacheIndex) {
      return this.cachedIndexMatches(cacheIndex) ? this.handler.createNewCachedRecipe(recipe, cacheIndex) : null;
   }

   @Override
   public boolean invalidateCache() {
      return CommonWorldTickHandler.flushTagAndRecipeCaches;
   }

   @Override
   public void setHasNoRecipe(int cacheIndex) {
      if (this.cachedIndexMatches(cacheIndex)) {
         this.hasNoRecipe = true;
      }
   }

   @Override
   public boolean hasNoRecipe(int cacheIndex) {
      return this.cachedIndexMatches(cacheIndex) ? this.hasNoRecipe : ICachedRecipeHolder.super.hasNoRecipe(cacheIndex);
   }
}
