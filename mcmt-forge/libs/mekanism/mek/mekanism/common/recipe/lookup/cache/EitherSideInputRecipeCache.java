package mekanism.common.recipe.lookup.cache;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.InputIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.type.IInputCache;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public abstract class EitherSideInputRecipeCache<INPUT, INGREDIENT extends InputIngredient<INPUT>, RECIPE extends MekanismRecipe & BiPredicate<INPUT, INPUT>, CACHE extends IInputCache<INPUT, INGREDIENT, RECIPE>>
   extends AbstractInputRecipeCache<RECIPE> {
   private final Set<RECIPE> complexRecipes = new HashSet<>();
   private final Function<RECIPE, INGREDIENT> inputAExtractor;
   private final Function<RECIPE, INGREDIENT> inputBExtractor;
   private final CACHE cache;

   protected EitherSideInputRecipeCache(
      MekanismRecipeType<RECIPE, ?> recipeType, Function<RECIPE, INGREDIENT> inputAExtractor, Function<RECIPE, INGREDIENT> inputBExtractor, CACHE cache
   ) {
      super(recipeType);
      this.inputAExtractor = inputAExtractor;
      this.inputBExtractor = inputBExtractor;
      this.cache = cache;
   }

   @Override
   public void clear() {
      super.clear();
      this.cache.clear();
      this.complexRecipes.clear();
   }

   public boolean containsInput(@Nullable Level world, INPUT input) {
      if (this.cache.isEmpty(input)) {
         return false;
      } else {
         this.initCacheIfNeeded(world);
         return this.cache.contains(input)
            || this.complexRecipes
               .stream()
               .anyMatch(recipe -> this.inputAExtractor.apply((RECIPE)recipe).testType(input) || this.inputBExtractor.apply((RECIPE)recipe).testType(input));
      }
   }

   public boolean containsInput(@Nullable Level world, INPUT inputA, INPUT inputB) {
      if (this.cache.isEmpty(inputA)) {
         return this.containsInput(world, inputB);
      } else if (this.cache.isEmpty(inputB)) {
         return true;
      } else {
         this.initCacheIfNeeded(world);
         return this.cache.contains(inputA, recipe -> {
            INGREDIENT ingredientA = this.inputAExtractor.apply(recipe);
            INGREDIENT ingredientB = this.inputBExtractor.apply(recipe);
            return ingredientB.testType(inputB) && ingredientA.testType(inputA) || ingredientA.testType(inputB) && ingredientB.testType(inputA);
         }) ? true : this.complexRecipes.stream().anyMatch(recipe -> {
            INGREDIENT ingredientA = this.inputAExtractor.apply((RECIPE)recipe);
            INGREDIENT ingredientB = this.inputBExtractor.apply((RECIPE)recipe);
            return ingredientA.testType(inputA) && ingredientB.testType(inputB) || ingredientB.testType(inputA) && ingredientA.testType(inputB);
         });
      }
   }

   @Nullable
   public RECIPE findFirstRecipe(@Nullable Level world, INPUT inputA, INPUT inputB) {
      if (!this.cache.isEmpty(inputA) && !this.cache.isEmpty(inputB)) {
         this.initCacheIfNeeded(world);
         Predicate<RECIPE> matchPredicate = r -> r.test(inputA, inputB);
         RECIPE recipe = this.cache.findFirstRecipe(inputA, matchPredicate);
         return recipe == null ? this.findFirstRecipe(this.complexRecipes, matchPredicate) : recipe;
      } else {
         return null;
      }
   }

   @Override
   protected void initCache(List<RECIPE> recipes) {
      for (RECIPE recipe : recipes) {
         boolean complexA = this.cache.mapInputs(recipe, this.inputAExtractor.apply(recipe));
         boolean complexB = this.cache.mapInputs(recipe, this.inputBExtractor.apply(recipe));
         if (complexA || complexB) {
            this.complexRecipes.add(recipe);
         }
      }
   }
}
