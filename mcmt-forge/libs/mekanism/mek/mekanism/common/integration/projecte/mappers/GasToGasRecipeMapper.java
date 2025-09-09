package mekanism.common.integration.projecte.mappers;

import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.GasToGasRecipe;
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

@RecipeTypeMapper
public class GasToGasRecipeMapper implements IRecipeTypeMapper {
   public String getName() {
      return "MekGasToGas";
   }

   public String getDescription() {
      return "Maps Mekanism activating and centrifuging recipes.";
   }

   public boolean canHandle(RecipeType<?> recipeType) {
      return recipeType == MekanismRecipeType.ACTIVATING.get() || recipeType == MekanismRecipeType.CENTRIFUGING.get();
   }

   public boolean handleRecipe(
      IMappingCollector<NormalizedSimpleStack, Long> mapper, Recipe<?> iRecipe, RegistryAccess registryAccess, INSSFakeGroupManager groupManager
   ) {
      if (iRecipe instanceof GasToGasRecipe recipe) {
         boolean handled = false;

         for (GasStack representation : recipe.getInput().getRepresentations()) {
            GasStack output = recipe.getOutput(representation);
            if (!output.isEmpty()) {
               IngredientHelper ingredientHelper = new IngredientHelper(mapper);
               ingredientHelper.put(representation);
               if (ingredientHelper.addAsConversion(output)) {
                  handled = true;
               }
            }
         }

         return handled;
      } else {
         return false;
      }
   }
}
