package mekanism.common.integration.projecte.mappers;

import java.util.List;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.common.integration.projecte.IngredientHelper;
import mekanism.common.integration.projecte.NSSGas;
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
public class ItemStackGasToItemStackRecipeMapper implements IRecipeTypeMapper {
   public String getName() {
      return "MekItemStackGasToItemStack";
   }

   public String getDescription() {
      return "Maps Mekanism Machine recipes that go from item, gas to item. (Compressing, Purifying, Injecting)";
   }

   public boolean canHandle(RecipeType<?> recipeType) {
      return recipeType == MekanismRecipeType.COMPRESSING.get()
         || recipeType == MekanismRecipeType.PURIFYING.get()
         || recipeType == MekanismRecipeType.INJECTING.get();
   }

   public boolean handleRecipe(
      IMappingCollector<NormalizedSimpleStack, Long> mapper, Recipe<?> iRecipe, RegistryAccess registryAccess, INSSFakeGroupManager groupManager
   ) {
      if (!(iRecipe instanceof ItemStackGasToItemStackRecipe recipe)) {
         return false;
      } else {
         boolean handled = false;
         List itemRepresentations = recipe.getItemInput().getRepresentations();

         for (GasStack gasRepresentation : recipe.getChemicalInput().getRepresentations()) {
            NSSGas nssGas = NSSGas.createGas(gasRepresentation);
            long gasAmount = gasRepresentation.getAmount() * 200L;

            for (ItemStack itemRepresentation : itemRepresentations) {
               ItemStack output = recipe.getOutput(itemRepresentation, gasRepresentation);
               if (!output.m_41619_()) {
                  IngredientHelper ingredientHelper = new IngredientHelper(mapper);
                  ingredientHelper.put(itemRepresentation);
                  ingredientHelper.put(nssGas, gasAmount);
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
