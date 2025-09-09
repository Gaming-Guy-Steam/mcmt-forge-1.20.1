package mekanism.common.recipe;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.common.recipe.lookup.cache.IInputRecipeCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IMekanismRecipeTypeProvider<RECIPE extends MekanismRecipe, INPUT_CACHE extends IInputRecipeCache> {
   default ResourceLocation getRegistryName() {
      return this.getRecipeType().getRegistryName();
   }

   MekanismRecipeType<RECIPE, INPUT_CACHE> getRecipeType();

   default INPUT_CACHE getInputCache() {
      return this.getRecipeType().getInputCache();
   }

   @NotNull
   default List<RECIPE> getRecipes(@Nullable Level world) {
      return this.getRecipeType().getRecipes(world);
   }

   default Stream<RECIPE> stream(@Nullable Level world) {
      return this.getRecipes(world).stream();
   }

   @Nullable
   default RECIPE findFirst(@Nullable Level world, Predicate<RECIPE> matchCriteria) {
      return this.stream(world).filter(matchCriteria).findFirst().orElse(null);
   }

   default boolean contains(@Nullable Level world, Predicate<RECIPE> matchCriteria) {
      return this.stream(world).anyMatch(matchCriteria);
   }
}
