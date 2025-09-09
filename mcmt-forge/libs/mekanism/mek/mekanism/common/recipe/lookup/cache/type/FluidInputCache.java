package mekanism.common.recipe.lookup.cache.type;

import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.common.recipe.ingredient.creator.FluidStackIngredientCreator;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class FluidInputCache<RECIPE extends MekanismRecipe> extends NBTSensitiveInputCache<Fluid, FluidStack, FluidStack, FluidStackIngredient, RECIPE> {
   public boolean mapInputs(RECIPE recipe, FluidStackIngredient inputIngredient) {
      if (inputIngredient instanceof FluidStackIngredientCreator.SingleFluidStackIngredient single) {
         this.addNbtInputCache(single.getInputRaw(), recipe);
      } else {
         if (!(inputIngredient instanceof FluidStackIngredientCreator.TaggedFluidStackIngredient tagged)) {
            if (inputIngredient instanceof FluidStackIngredientCreator.MultiFluidStackIngredient multi) {
               return this.mapMultiInputs(recipe, multi);
            }

            return true;
         }

         for (Fluid input : tagged.getRawInput()) {
            this.addInputCache(input, recipe);
         }
      }

      return false;
   }

   protected Fluid createKey(FluidStack stack) {
      return stack.getFluid();
   }

   protected FluidStack createNbtKey(FluidStack stack) {
      return stack;
   }

   public boolean isEmpty(FluidStack input) {
      return input.isEmpty();
   }
}
