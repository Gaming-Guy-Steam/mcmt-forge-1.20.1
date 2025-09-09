package mekanism.common.recipe.lookup;

import mekanism.api.IContentsListener;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.lookup.cache.IInputRecipeCache;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IRecipeLookupHandler<RECIPE extends MekanismRecipe> extends IContentsListener {
   @Nullable
   default Level getHandlerWorld() {
      if (this instanceof BlockEntity tile) {
         return tile.m_58904_();
      } else {
         return this instanceof Entity entity ? entity.m_9236_() : null;
      }
   }

   @NotNull
   IMekanismRecipeTypeProvider<RECIPE, ?> getRecipeType();

   default int getSavedOperatingTicks(int cacheIndex) {
      return 0;
   }

   @Nullable
   RECIPE getRecipe(int cacheIndex);

   @NotNull
   CachedRecipe<RECIPE> createNewCachedRecipe(@NotNull RECIPE recipe, int cacheIndex);

   default void onCachedRecipeChanged(@Nullable CachedRecipe<RECIPE> cachedRecipe, int cacheIndex) {
      this.clearRecipeErrors(cacheIndex);
   }

   default void clearRecipeErrors(int cacheIndex) {
   }

   public interface ConstantUsageRecipeLookupHandler {
      default long getSavedUsedSoFar(int cacheIndex) {
         return 0L;
      }
   }

   public interface IRecipeTypedLookupHandler<RECIPE extends MekanismRecipe, INPUT_CACHE extends IInputRecipeCache> extends IRecipeLookupHandler<RECIPE> {
      @NotNull
      @Override
      IMekanismRecipeTypeProvider<RECIPE, INPUT_CACHE> getRecipeType();
   }
}
