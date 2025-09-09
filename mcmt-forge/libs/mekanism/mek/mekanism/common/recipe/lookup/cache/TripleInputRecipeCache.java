package mekanism.common.recipe.lookup.cache;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.InputIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.type.IInputCache;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.TriPredicate;
import org.jetbrains.annotations.Nullable;

public abstract class TripleInputRecipeCache<INPUT_A, INGREDIENT_A extends InputIngredient<INPUT_A>, INPUT_B, INGREDIENT_B extends InputIngredient<INPUT_B>, INPUT_C, INGREDIENT_C extends InputIngredient<INPUT_C>, RECIPE extends MekanismRecipe & TriPredicate<INPUT_A, INPUT_B, INPUT_C>, CACHE_A extends IInputCache<INPUT_A, INGREDIENT_A, RECIPE>, CACHE_B extends IInputCache<INPUT_B, INGREDIENT_B, RECIPE>, CACHE_C extends IInputCache<INPUT_C, INGREDIENT_C, RECIPE>>
   extends AbstractInputRecipeCache<RECIPE> {
   private final Set<RECIPE> complexIngredientA = new HashSet<>();
   private final Set<RECIPE> complexIngredientB = new HashSet<>();
   private final Set<RECIPE> complexIngredientC = new HashSet<>();
   private final Set<RECIPE> complexRecipes = new HashSet<>();
   private final Function<RECIPE, INGREDIENT_A> inputAExtractor;
   private final Function<RECIPE, INGREDIENT_B> inputBExtractor;
   private final Function<RECIPE, INGREDIENT_C> inputCExtractor;
   private final CACHE_A cacheA;
   private final CACHE_B cacheB;
   private final CACHE_C cacheC;

   protected TripleInputRecipeCache(
      MekanismRecipeType<RECIPE, ?> recipeType,
      Function<RECIPE, INGREDIENT_A> inputAExtractor,
      CACHE_A cacheA,
      Function<RECIPE, INGREDIENT_B> inputBExtractor,
      CACHE_B cacheB,
      Function<RECIPE, INGREDIENT_C> inputCExtractor,
      CACHE_C cacheC
   ) {
      super(recipeType);
      this.inputAExtractor = inputAExtractor;
      this.inputBExtractor = inputBExtractor;
      this.inputCExtractor = inputCExtractor;
      this.cacheA = cacheA;
      this.cacheB = cacheB;
      this.cacheC = cacheC;
   }

   @Override
   public void clear() {
      super.clear();
      this.cacheA.clear();
      this.cacheB.clear();
      this.cacheC.clear();
      this.complexIngredientA.clear();
      this.complexIngredientB.clear();
      this.complexIngredientC.clear();
      this.complexRecipes.clear();
   }

   public boolean containsInputA(@Nullable Level world, INPUT_A input) {
      return this.containsInput(world, input, this.inputAExtractor, this.cacheA, this.complexIngredientA);
   }

   public boolean containsInputB(@Nullable Level world, INPUT_B input) {
      return this.containsInput(world, input, this.inputBExtractor, this.cacheB, this.complexIngredientB);
   }

   public boolean containsInputC(@Nullable Level world, INPUT_C input) {
      return this.containsInput(world, input, this.inputCExtractor, this.cacheC, this.complexIngredientC);
   }

   public boolean containsInputABC(@Nullable Level world, INPUT_A inputA, INPUT_B inputB, INPUT_C inputC) {
      return this.containsGrouping(
         world,
         inputA,
         this.inputAExtractor,
         this.cacheA,
         this.complexIngredientA,
         inputB,
         this.inputBExtractor,
         this.cacheB,
         this.complexIngredientB,
         inputC,
         this.inputCExtractor,
         this.cacheC,
         this.complexIngredientC
      );
   }

   public boolean containsInputBAC(@Nullable Level world, INPUT_A inputA, INPUT_B inputB, INPUT_C inputC) {
      return this.containsGrouping(
         world,
         inputB,
         this.inputBExtractor,
         this.cacheB,
         this.complexIngredientB,
         inputA,
         this.inputAExtractor,
         this.cacheA,
         this.complexIngredientA,
         inputC,
         this.inputCExtractor,
         this.cacheC,
         this.complexIngredientC
      );
   }

   public boolean containsInputCAB(@Nullable Level world, INPUT_A inputA, INPUT_B inputB, INPUT_C inputC) {
      return this.containsGrouping(
         world,
         inputC,
         this.inputCExtractor,
         this.cacheC,
         this.complexIngredientC,
         inputA,
         this.inputAExtractor,
         this.cacheA,
         this.complexIngredientA,
         inputB,
         this.inputBExtractor,
         this.cacheB,
         this.complexIngredientB
      );
   }

   private <INPUT_1, INGREDIENT_1 extends InputIngredient<INPUT_1>, CACHE_1 extends IInputCache<INPUT_1, INGREDIENT_1, RECIPE>, INPUT_2, INGREDIENT_2 extends InputIngredient<INPUT_2>, CACHE_2 extends IInputCache<INPUT_2, INGREDIENT_2, RECIPE>, INPUT_3, INGREDIENT_3 extends InputIngredient<INPUT_3>, CACHE_3 extends IInputCache<INPUT_3, INGREDIENT_3, RECIPE>> boolean containsGrouping(
      @Nullable Level world,
      INPUT_1 input1,
      Function<RECIPE, INGREDIENT_1> input1Extractor,
      CACHE_1 cache1,
      Set<RECIPE> complexIngredients1,
      INPUT_2 input2,
      Function<RECIPE, INGREDIENT_2> input2Extractor,
      CACHE_2 cache2,
      Set<RECIPE> complexIngredients2,
      INPUT_3 input3,
      Function<RECIPE, INGREDIENT_3> input3Extractor,
      CACHE_3 cache3,
      Set<RECIPE> complexIngredients3
   ) {
      if (cache1.isEmpty(input1)) {
         return cache3.isEmpty(input3)
            ? this.containsInput(world, input2, input2Extractor, cache2, complexIngredients2)
            : this.containsPairing(
               world, input2, input2Extractor, (CACHE_1)cache2, complexIngredients2, input3, input3Extractor, (CACHE_2)cache3, complexIngredients3
            );
      } else if (cache2.isEmpty(input2)) {
         return this.containsPairing(world, input1, input1Extractor, cache1, complexIngredients1, input3, input3Extractor, (CACHE_2)cache3, complexIngredients3);
      } else if (cache3.isEmpty(input3)) {
         return this.containsPairing(world, input1, input1Extractor, cache1, complexIngredients1, input2, input2Extractor, cache2, complexIngredients2);
      } else {
         this.initCacheIfNeeded(world);
         return cache1.contains(input1, recipe -> input2Extractor.apply(recipe).testType(input2) && input3Extractor.apply(recipe).testType(input3))
            ? true
            : complexIngredients1.stream()
               .anyMatch(
                  recipe -> input1Extractor.apply((RECIPE)recipe).testType(input1)
                     && input2Extractor.apply((RECIPE)recipe).testType(input2)
                     && input3Extractor.apply((RECIPE)recipe).testType(input3)
               );
      }
   }

   @Nullable
   public RECIPE findFirstRecipe(@Nullable Level world, INPUT_A inputA, INPUT_B inputB, INPUT_C inputC) {
      if (!this.cacheA.isEmpty(inputA) && !this.cacheB.isEmpty(inputB)) {
         this.initCacheIfNeeded(world);
         Predicate<RECIPE> matchPredicate = r -> r.test(inputA, inputB, inputC);
         RECIPE recipe = this.cacheA.findFirstRecipe(inputA, matchPredicate);
         return recipe == null ? this.findFirstRecipe(this.complexRecipes, matchPredicate) : recipe;
      } else {
         return null;
      }
   }

   @Override
   protected void initCache(List<RECIPE> recipes) {
      for (RECIPE recipe : recipes) {
         boolean complexA = this.cacheA.mapInputs(recipe, this.inputAExtractor.apply(recipe));
         boolean complexB = this.cacheB.mapInputs(recipe, this.inputBExtractor.apply(recipe));
         boolean complexC = this.cacheC.mapInputs(recipe, this.inputCExtractor.apply(recipe));
         if (complexA) {
            this.complexIngredientA.add(recipe);
         }

         if (complexB) {
            this.complexIngredientB.add(recipe);
         }

         if (complexC) {
            this.complexIngredientC.add(recipe);
         }

         if (complexA || complexB || complexC) {
            this.complexRecipes.add(recipe);
         }
      }
   }
}
