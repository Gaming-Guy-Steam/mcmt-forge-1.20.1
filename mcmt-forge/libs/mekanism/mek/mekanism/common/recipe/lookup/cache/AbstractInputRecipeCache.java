package mekanism.common.recipe.lookup.cache;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.InputIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.type.IInputCache;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractInputRecipeCache<RECIPE extends MekanismRecipe> implements IInputRecipeCache {
   protected final MekanismRecipeType<RECIPE, ?> recipeType;
   protected boolean initialized;

   protected AbstractInputRecipeCache(MekanismRecipeType<RECIPE, ?> recipeType) {
      this.recipeType = recipeType;
   }

   @Override
   public void clear() {
      this.initialized = false;
   }

   protected void initCacheIfNeeded(@Nullable Level world) {
      if (!this.initialized) {
         this.initialized = true;
         this.initCache(this.recipeType.getRecipes(world));
      }
   }

   protected abstract void initCache(List<RECIPE> recipes);

   @Nullable
   protected RECIPE findFirstRecipe(@Nullable Collection<RECIPE> recipes, Predicate<RECIPE> matchCriteria) {
      return recipes == null ? null : recipes.stream().filter(matchCriteria).findFirst().orElse(null);
   }

   protected <INPUT, INGREDIENT extends InputIngredient<INPUT>, CACHE extends IInputCache<INPUT, INGREDIENT, RECIPE>> boolean containsInput(
      @Nullable Level world, INPUT input, Function<RECIPE, INGREDIENT> inputExtractor, CACHE cache, Set<RECIPE> complexRecipes
   ) {
      if (cache.isEmpty(input)) {
         return false;
      } else {
         this.initCacheIfNeeded(world);
         return cache.contains(input) || complexRecipes.stream().anyMatch(recipe -> inputExtractor.apply((RECIPE)recipe).testType(input));
      }
   }

   protected <INPUT_1, INGREDIENT_1 extends InputIngredient<INPUT_1>, CACHE_1 extends IInputCache<INPUT_1, INGREDIENT_1, RECIPE>, INPUT_2, INGREDIENT_2 extends InputIngredient<INPUT_2>, CACHE_2 extends IInputCache<INPUT_2, INGREDIENT_2, RECIPE>> boolean containsPairing(
      @Nullable Level world,
      INPUT_1 input1,
      Function<RECIPE, INGREDIENT_1> input1Extractor,
      CACHE_1 cache1,
      Set<RECIPE> complexIngredients1,
      INPUT_2 input2,
      Function<RECIPE, INGREDIENT_2> input2Extractor,
      CACHE_2 cache2,
      Set<RECIPE> complexIngredients2
   ) {
      if (cache1.isEmpty(input1)) {
         return this.containsInput(world, input2, input2Extractor, cache2, complexIngredients2);
      } else if (cache2.isEmpty(input2)) {
         return true;
      } else {
         this.initCacheIfNeeded(world);
         return cache1.contains(input1, recipe -> input2Extractor.apply(recipe).testType(input2))
            ? true
            : complexIngredients1.stream()
               .anyMatch(recipe -> input1Extractor.apply((RECIPE)recipe).testType(input1) && input2Extractor.apply((RECIPE)recipe).testType(input2));
      }
   }
}
