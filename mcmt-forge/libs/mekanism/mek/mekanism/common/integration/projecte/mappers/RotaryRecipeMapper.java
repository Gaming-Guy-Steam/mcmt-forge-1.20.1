package mekanism.common.integration.projecte.mappers;

import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.common.integration.projecte.IngredientHelper;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.INSSFakeGroupManager;
import moze_intel.projecte.api.mapper.recipe.IRecipeTypeMapper;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fluids.FluidStack;

@RecipeTypeMapper
public class RotaryRecipeMapper implements IRecipeTypeMapper {
   public String getName() {
      return "MekRotary";
   }

   public String getDescription() {
      return "Maps Mekanism rotary condensentrator recipes.";
   }

   public boolean canHandle(RecipeType<?> recipeType) {
      return recipeType == MekanismRecipeType.ROTARY.get();
   }

   public boolean handleRecipe(
      IMappingCollector<NormalizedSimpleStack, Long> mapper, Recipe<?> iRecipe, RegistryAccess registryAccess, INSSFakeGroupManager groupManager
   ) {
      if (!(iRecipe instanceof RotaryRecipe recipe)) {
         return false;
      } else {
         boolean handled = false;
         if (recipe.hasFluidToGas()) {
            for (FluidStack representation : recipe.getFluidInput().getRepresentations()) {
               GasStack output = recipe.getGasOutput(representation);
               if (!output.isEmpty()) {
                  IngredientHelper ingredientHelper = new IngredientHelper(mapper);
                  ingredientHelper.put(representation);
                  if (ingredientHelper.addAsConversion(output)) {
                     handled = true;
                  }
               }
            }
         }

         if (recipe.hasGasToFluid()) {
            for (GasStack representationx : recipe.getGasInput().getRepresentations()) {
               FluidStack output = recipe.getFluidOutput(representationx);
               if (!output.isEmpty()) {
                  IngredientHelper ingredientHelper = new IngredientHelper(mapper);
                  ingredientHelper.put(representationx);
                  if (ingredientHelper.addAsConversion(output)) {
                     handled = true;
                  }
               }
            }
         }

         return handled;
      }
   }
}
