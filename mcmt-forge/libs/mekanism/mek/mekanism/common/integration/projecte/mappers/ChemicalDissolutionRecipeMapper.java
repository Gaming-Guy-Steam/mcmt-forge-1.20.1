package mekanism.common.integration.projecte.mappers;

import java.util.List;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
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
public class ChemicalDissolutionRecipeMapper implements IRecipeTypeMapper {
   public String getName() {
      return "MekDissolution";
   }

   public String getDescription() {
      return "Maps Mekanism dissolution recipes.";
   }

   public boolean canHandle(RecipeType<?> recipeType) {
      return recipeType == MekanismRecipeType.DISSOLUTION.get();
   }

   public boolean handleRecipe(
      IMappingCollector<NormalizedSimpleStack, Long> mapper, Recipe<?> iRecipe, RegistryAccess registryAccess, INSSFakeGroupManager groupManager
   ) {
      if (!(iRecipe instanceof ChemicalDissolutionRecipe recipe)) {
         return false;
      } else {
         boolean handled = false;
         List itemRepresentations = recipe.getItemInput().getRepresentations();

         for (GasStack gasRepresentation : recipe.getGasInput().getRepresentations()) {
            NSSGas nssGas = NSSGas.createGas(gasRepresentation);
            long gasAmount = gasRepresentation.getAmount() * 100L;

            for (ItemStack itemRepresentation : itemRepresentations) {
               BoxedChemicalStack output = recipe.getOutput(itemRepresentation, gasRepresentation);
               if (!output.isEmpty()) {
                  IngredientHelper ingredientHelper = new IngredientHelper(mapper);
                  ingredientHelper.put(itemRepresentation);
                  ingredientHelper.put(nssGas, gasAmount);
                  if (ingredientHelper.addAsConversion(output.getChemicalStack())) {
                     handled = true;
                  }
               }
            }
         }

         return handled;
      }
   }
}
