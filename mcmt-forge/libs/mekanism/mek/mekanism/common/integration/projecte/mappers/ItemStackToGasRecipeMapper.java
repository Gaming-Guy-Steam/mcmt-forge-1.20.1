package mekanism.common.integration.projecte.mappers;

import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.common.integration.projecte.IngredientHelper;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.INSSFakeGroupManager;
import moze_intel.projecte.api.mapper.recipe.IRecipeTypeMapper;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

@RecipeTypeMapper
public class ItemStackToGasRecipeMapper implements IRecipeTypeMapper {
   public String getName() {
      return "MekItemStackToGas";
   }

   public String getDescription() {
      return "Maps Mekanism item stack to gas recipes. (Gas conversion, Oxidizing)";
   }

   public boolean canHandle(RecipeType<?> recipeType) {
      return recipeType == MekanismRecipeType.GAS_CONVERSION.get() || recipeType == MekanismRecipeType.OXIDIZING.get();
   }

   public boolean handleRecipe(
      IMappingCollector<NormalizedSimpleStack, Long> mapper, Recipe<?> iRecipe, RegistryAccess registryAccess, INSSFakeGroupManager groupManager
   ) {
      if (iRecipe instanceof ItemStackToGasRecipe recipe) {
         boolean handled = false;

         for (ItemStack representation : recipe.getInput().getRepresentations()) {
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
