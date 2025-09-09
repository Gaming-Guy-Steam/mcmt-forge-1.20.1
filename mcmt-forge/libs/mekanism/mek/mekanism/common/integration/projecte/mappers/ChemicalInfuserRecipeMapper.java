package mekanism.common.integration.projecte.mappers;

import java.util.List;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.common.integration.projecte.IngredientHelper;
import mekanism.common.integration.projecte.NSSGas;
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
public class ChemicalInfuserRecipeMapper implements IRecipeTypeMapper {
   public String getName() {
      return "MekChemicalInfuser";
   }

   public String getDescription() {
      return "Maps Mekanism chemical infuser recipes.";
   }

   public boolean canHandle(RecipeType<?> recipeType) {
      return recipeType == MekanismRecipeType.CHEMICAL_INFUSING.get();
   }

   public boolean handleRecipe(
      IMappingCollector<NormalizedSimpleStack, Long> mapper, Recipe<?> iRecipe, RegistryAccess registryAccess, INSSFakeGroupManager groupManager
   ) {
      if (!(iRecipe instanceof ChemicalInfuserRecipe recipe)) {
         return false;
      } else {
         boolean handled = false;
         List leftInputRepresentations = recipe.getLeftInput().getRepresentations();
         List rightInputRepresentations = recipe.getRightInput().getRepresentations();

         for (GasStack leftRepresentation : leftInputRepresentations) {
            NormalizedSimpleStack nssLeft = NSSGas.createGas(leftRepresentation);

            for (GasStack rightRepresentation : rightInputRepresentations) {
               GasStack output = recipe.getOutput(leftRepresentation, rightRepresentation);
               if (!output.isEmpty()) {
                  IngredientHelper ingredientHelper = new IngredientHelper(mapper);
                  ingredientHelper.put(nssLeft, leftRepresentation.getAmount());
                  ingredientHelper.put(rightRepresentation);
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
