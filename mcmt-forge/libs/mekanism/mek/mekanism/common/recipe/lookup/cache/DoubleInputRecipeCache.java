package mekanism.common.recipe.lookup.cache;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.InputIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.type.IInputCache;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public abstract class DoubleInputRecipeCache<INPUT_A, INGREDIENT_A extends InputIngredient<INPUT_A>, INPUT_B, INGREDIENT_B extends InputIngredient<INPUT_B>, RECIPE extends MekanismRecipe & BiPredicate<INPUT_A, INPUT_B>, CACHE_A extends IInputCache<INPUT_A, INGREDIENT_A, RECIPE>, CACHE_B extends IInputCache<INPUT_B, INGREDIENT_B, RECIPE>>
   extends AbstractInputRecipeCache<RECIPE> {
   private final Set<RECIPE> complexIngredientA = new HashSet<>();
   private final Set<RECIPE> complexIngredientB = new HashSet<>();
   private final Set<RECIPE> complexRecipes = new HashSet<>();
   private final Function<RECIPE, INGREDIENT_A> inputAExtractor;
   private final Function<RECIPE, INGREDIENT_B> inputBExtractor;
   private final CACHE_A cacheA;
   private final CACHE_B cacheB;

   protected DoubleInputRecipeCache(
      MekanismRecipeType<RECIPE, ?> recipeType,
      Function<RECIPE, INGREDIENT_A> inputAExtractor,
      CACHE_A cacheA,
      Function<RECIPE, INGREDIENT_B> inputBExtractor,
      CACHE_B cacheB
   ) {
      super(recipeType);
      this.inputAExtractor = inputAExtractor;
      this.inputBExtractor = inputBExtractor;
      this.cacheA = cacheA;
      this.cacheB = cacheB;
   }

   @Override
   public void clear() {
      super.clear();
      this.cacheA.clear();
      this.cacheB.clear();
      this.complexIngredientA.clear();
      this.complexIngredientB.clear();
      this.complexRecipes.clear();
   }

   public boolean containsInputA(@Nullable Level world, INPUT_A input) {
      return this.containsInput(world, input, this.inputAExtractor, this.cacheA, this.complexIngredientA);
   }

   public boolean containsInputB(@Nullable Level world, INPUT_B input) {
      return this.containsInput(world, input, this.inputBExtractor, this.cacheB, this.complexIngredientB);
   }

   public boolean containsInputAB(@Nullable Level world, INPUT_A inputA, INPUT_B inputB) {
      return this.containsPairing(
         world, inputA, this.inputAExtractor, this.cacheA, this.complexIngredientA, inputB, this.inputBExtractor, this.cacheB, this.complexIngredientB
      );
   }

   public boolean containsInputBA(@Nullable Level world, INPUT_A inputA, INPUT_B inputB) {
      return this.containsPairing(
         world, inputB, this.inputBExtractor, this.cacheB, this.complexIngredientB, inputA, this.inputAExtractor, this.cacheA, this.complexIngredientA
      );
   }

   @Nullable
   public RECIPE findFirstRecipe(@Nullable Level world, INPUT_A inputA, INPUT_B inputB) {
      return this.findFirstRecipe(world, inputA, inputB, true);
   }

   @Nullable
   public RECIPE findFirstRecipe(@Nullable Level world, INPUT_A inputA, INPUT_B inputB, boolean useCacheA) {
      if (!this.cacheA.isEmpty(inputA) && !this.cacheB.isEmpty(inputB)) {
         this.initCacheIfNeeded(world);
         Predicate<RECIPE> matchPredicate = r -> r.test(inputA, inputB);
         RECIPE recipe;
         if (useCacheA) {
            recipe = this.cacheA.findFirstRecipe(inputA, matchPredicate);
         } else {
            recipe = this.cacheB.findFirstRecipe(inputB, matchPredicate);
         }

         return recipe == null ? this.findFirstRecipe(this.complexRecipes, matchPredicate) : recipe;
      } else {
         return null;
      }
   }

   @Nullable
   public RECIPE findTypeBasedRecipe(@Nullable Level world, INPUT_A inputA, INPUT_B inputB, Predicate<RECIPE> matchCriteria) {
      if (this.cacheA.isEmpty(inputA)) {
         return null;
      } else {
         this.initCacheIfNeeded(world);
         Predicate<RECIPE> matchPredicate;
         if (this.cacheB.isEmpty(inputB)) {
            matchPredicate = matchCriteria;
         } else {
            matchPredicate = recipex -> this.inputBExtractor.apply((RECIPE)recipex).testType(inputB) && matchCriteria.test((RECIPE)recipex);
         }

         RECIPE recipe = this.cacheA.findFirstRecipe(inputA, matchPredicate);
         return recipe == null
            ? this.findFirstRecipe(this.complexRecipes, r -> this.inputAExtractor.apply(r).testType(inputA) && matchPredicate.test(r))
            : recipe;
      }
   }

   @Override
   protected void initCache(List<RECIPE> recipes) {
      for (RECIPE recipe : recipes) {
         boolean complexA = this.cacheA.mapInputs(recipe, this.inputAExtractor.apply(recipe));
         boolean complexB = this.cacheB.mapInputs(recipe, this.inputBExtractor.apply(recipe));
         if (complexA) {
            this.complexIngredientA.add(recipe);
         }

         if (complexB) {
            this.complexIngredientB.add(recipe);
         }

         if (complexA || complexB) {
            this.complexRecipes.add(recipe);
         }
      }
   }

   public abstract static class DoubleSameInputRecipeCache<INPUT, INGREDIENT extends InputIngredient<INPUT>, RECIPE extends MekanismRecipe & BiPredicate<INPUT, INPUT>, CACHE extends IInputCache<INPUT, INGREDIENT, RECIPE>>
      extends DoubleInputRecipeCache<INPUT, INGREDIENT, INPUT, INGREDIENT, RECIPE, CACHE, CACHE> {
      protected DoubleSameInputRecipeCache(
         MekanismRecipeType<RECIPE, ?> recipeType,
         Function<RECIPE, INGREDIENT> inputAExtractor,
         Function<RECIPE, INGREDIENT> inputBExtractor,
         Supplier<CACHE> cacheSupplier
      ) {
         super(recipeType, inputAExtractor, cacheSupplier.get(), inputBExtractor, cacheSupplier.get());
      }
   }
}
