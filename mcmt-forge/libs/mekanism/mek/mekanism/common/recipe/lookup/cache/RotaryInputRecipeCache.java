package mekanism.common.recipe.lookup.cache;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.type.ChemicalInputCache;
import mekanism.common.recipe.lookup.cache.type.FluidInputCache;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public class RotaryInputRecipeCache extends AbstractInputRecipeCache<RotaryRecipe> {
   private final ChemicalInputCache<Gas, GasStack, RotaryRecipe> gasInputCache = new ChemicalInputCache<>();
   private final FluidInputCache<RotaryRecipe> fluidInputCache = new FluidInputCache<>();
   private final Set<RotaryRecipe> complexGasInputRecipes = new HashSet<>();
   private final Set<RotaryRecipe> complexFluidInputRecipes = new HashSet<>();

   public RotaryInputRecipeCache(MekanismRecipeType<RotaryRecipe, ?> recipeType) {
      super(recipeType);
   }

   @Override
   public void clear() {
      super.clear();
      this.gasInputCache.clear();
      this.fluidInputCache.clear();
      this.complexGasInputRecipes.clear();
      this.complexFluidInputRecipes.clear();
   }

   public boolean containsInput(@Nullable Level world, FluidStack input) {
      return this.containsInput(world, input, RotaryRecipe::getFluidInput, this.fluidInputCache, this.complexFluidInputRecipes);
   }

   public boolean containsInput(@Nullable Level world, GasStack input) {
      return this.containsInput(world, input, RotaryRecipe::getGasInput, this.gasInputCache, this.complexGasInputRecipes);
   }

   @Nullable
   public RotaryRecipe findFirstRecipe(@Nullable Level world, FluidStack input) {
      if (this.fluidInputCache.isEmpty(input)) {
         return null;
      } else {
         this.initCacheIfNeeded(world);
         Predicate<RotaryRecipe> matchPredicate = recipex -> recipex.test(input);
         RotaryRecipe recipe = this.fluidInputCache.findFirstRecipe(input, matchPredicate);
         return recipe == null ? this.findFirstRecipe(this.complexFluidInputRecipes, matchPredicate) : recipe;
      }
   }

   @Nullable
   public RotaryRecipe findFirstRecipe(@Nullable Level world, GasStack input) {
      if (this.gasInputCache.isEmpty(input)) {
         return null;
      } else {
         this.initCacheIfNeeded(world);
         Predicate<RotaryRecipe> matchPredicate = recipex -> recipex.test(input);
         RotaryRecipe recipe = this.gasInputCache.findFirstRecipe(input, matchPredicate);
         return recipe == null ? this.findFirstRecipe(this.complexGasInputRecipes, matchPredicate) : recipe;
      }
   }

   @Override
   protected void initCache(List<RotaryRecipe> recipes) {
      for (RotaryRecipe recipe : recipes) {
         if (recipe.hasFluidToGas() && this.fluidInputCache.mapInputs(recipe, recipe.getFluidInput())) {
            this.complexFluidInputRecipes.add(recipe);
         }

         if (recipe.hasGasToFluid() && this.gasInputCache.mapInputs(recipe, recipe.getGasInput())) {
            this.complexGasInputRecipes.add(recipe);
         }
      }
   }
}
