package mekanism.common.recipe.lookup.cache;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.type.ChemicalInputCache;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.EnumUtils;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ChemicalCrystallizerInputRecipeCache extends AbstractInputRecipeCache<ChemicalCrystallizerRecipe> {
   private final Map<ChemicalType, ChemicalInputCache<?, ?, ChemicalCrystallizerRecipe>> typeBasedCache = new EnumMap<>(ChemicalType.class);
   private final Map<ChemicalType, Set<ChemicalCrystallizerRecipe>> typeBasedComplexRecipes = new EnumMap<>(ChemicalType.class);

   public ChemicalCrystallizerInputRecipeCache(MekanismRecipeType<ChemicalCrystallizerRecipe, ?> recipeType) {
      super(recipeType);

      for (ChemicalType chemicalType : EnumUtils.CHEMICAL_TYPES) {
         this.typeBasedCache.put(chemicalType, new ChemicalInputCache<>());
         this.typeBasedComplexRecipes.put(chemicalType, new HashSet<>());
      }
   }

   @Override
   public void clear() {
      super.clear();

      for (ChemicalType chemicalType : EnumUtils.CHEMICAL_TYPES) {
         this.typeBasedCache.get(chemicalType).clear();
         this.typeBasedComplexRecipes.get(chemicalType).clear();
      }
   }

   public boolean containsInput(@Nullable Level world, BoxedChemicalStack input) {
      if (input.isEmpty()) {
         return false;
      } else {
         this.initCacheIfNeeded(world);
         ChemicalType type = input.getChemicalType();
         return this.containsInput(type, input.getChemicalStack())
            || this.typeBasedComplexRecipes.get(type).stream().anyMatch(recipe -> recipe.testType(input));
      }
   }

   public <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> boolean containsInput(@Nullable Level world, CHEMICAL input) {
      if (input.isEmptyType()) {
         return false;
      } else {
         this.initCacheIfNeeded(world);
         ChemicalType type = ChemicalType.getTypeFor(input);
         STACK stack = ChemicalUtil.withAmount(input, 1L);
         return this.containsInput(type, stack) || this.typeBasedComplexRecipes.get(type).stream().anyMatch(recipe -> recipe.testType(stack));
      }
   }

   private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> boolean containsInput(ChemicalType type, STACK stack) {
      return this.typeBasedCache.get(type).contains(stack);
   }

   @Nullable
   public ChemicalCrystallizerRecipe findFirstRecipe(@Nullable Level world, BoxedChemicalStack input) {
      if (input.isEmpty()) {
         return null;
      } else {
         this.initCacheIfNeeded(world);
         return this.findFirstRecipe(input.getChemicalType(), input.getChemicalStack());
      }
   }

   @Nullable
   private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> ChemicalCrystallizerRecipe findFirstRecipe(
      ChemicalType type, STACK stack
   ) {
      Predicate<ChemicalCrystallizerRecipe> matchPredicate = recipex -> recipex.getInput().test(stack);
      ChemicalInputCache<CHEMICAL, STACK, ChemicalCrystallizerRecipe> cache = (ChemicalInputCache<CHEMICAL, STACK, ChemicalCrystallizerRecipe>)this.typeBasedCache
         .get(type);
      ChemicalCrystallizerRecipe recipe = cache.findFirstRecipe(stack, matchPredicate);
      return recipe == null ? this.findFirstRecipe(this.typeBasedComplexRecipes.get(type), matchPredicate) : recipe;
   }

   @Override
   protected void initCache(List<ChemicalCrystallizerRecipe> recipes) {
      for (ChemicalCrystallizerRecipe recipe : recipes) {
         ChemicalStackIngredient<?, ?> ingredient = recipe.getInput();
         ChemicalType type = ChemicalType.getTypeFor(ingredient);
         if (this.mapInputs(recipe, type, ingredient)) {
            this.typeBasedComplexRecipes.get(type).add(recipe);
         }
      }
   }

   private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>> boolean mapInputs(
      ChemicalCrystallizerRecipe recipe, ChemicalType type, INGREDIENT ingredient
   ) {
      return this.typeBasedCache.get(type).mapInputs(recipe, ingredient);
   }
}
