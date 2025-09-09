package mekanism.common.recipe.lookup.cache;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.InputIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.type.IInputCache;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public abstract class SingleInputRecipeCache<INPUT, INGREDIENT extends InputIngredient<INPUT>, RECIPE extends MekanismRecipe & Predicate<INPUT>, CACHE extends IInputCache<INPUT, INGREDIENT, RECIPE>>
   extends AbstractInputRecipeCache<RECIPE> {
   private final Set<RECIPE> complexRecipes = new HashSet<>();
   private final Function<RECIPE, INGREDIENT> inputExtractor;
   private final CACHE cache;

   protected SingleInputRecipeCache(MekanismRecipeType<RECIPE, ?> recipeType, Function<RECIPE, INGREDIENT> inputExtractor, CACHE cache) {
      super(recipeType);
      this.inputExtractor = inputExtractor;
      this.cache = cache;
   }

   @Override
   public void clear() {
      super.clear();
      this.cache.clear();
      this.complexRecipes.clear();
   }

   public boolean containsInput(@Nullable Level world, INPUT input) {
      return this.containsInput(world, input, this.inputExtractor, this.cache, this.complexRecipes);
   }

   @Nullable
   public RECIPE findFirstRecipe(@Nullable Level world, INPUT input) {
      if (this.cache.isEmpty(input)) {
         return null;
      } else {
         this.initCacheIfNeeded(world);
         Predicate<RECIPE> matchPredicate = recipex -> ((Predicate)recipex).test(input);
         RECIPE recipe = this.cache.findFirstRecipe(input, matchPredicate);
         return recipe == null ? this.findFirstRecipe(this.complexRecipes, matchPredicate) : recipe;
      }
   }

   @Nullable
   public RECIPE findTypeBasedRecipe(@Nullable Level world, INPUT input) {
      return this.findTypeBasedRecipe(world, input, ConstantPredicates.alwaysTrue());
   }

   @Nullable
   public RECIPE findTypeBasedRecipe(@Nullable Level world, INPUT input, Predicate<RECIPE> matchCriteria) {
      if (this.cache.isEmpty(input)) {
         return null;
      } else {
         this.initCacheIfNeeded(world);
         RECIPE recipe = this.cache.findFirstRecipe(input, matchCriteria);
         return recipe == null ? this.findFirstRecipe(this.complexRecipes, r -> this.inputExtractor.apply(r).testType(input) && matchCriteria.test(r)) : recipe;
      }
   }

   @Override
   protected void initCache(List<RECIPE> recipes) {
      for (RECIPE recipe : recipes) {
         if (this.cache.mapInputs(recipe, this.inputExtractor.apply(recipe))) {
            this.complexRecipes.add(recipe);
         }
      }
   }
}
