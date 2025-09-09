package mekanism.common.integration.projecte.mappers;

import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.common.integration.projecte.IngredientHelper;
import mekanism.common.integration.projecte.NSSGas;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.INSSFakeGroupManager;
import moze_intel.projecte.api.mapper.recipe.IRecipeTypeMapper;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NSSFluid;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fluids.FluidStack;

@RecipeTypeMapper
public class ElectrolysisRecipeMapper implements IRecipeTypeMapper {
   public String getName() {
      return "MekElectrolysis";
   }

   public String getDescription() {
      return "Maps Mekanism electrolytic separator recipes.";
   }

   public boolean canHandle(RecipeType<?> recipeType) {
      return recipeType == MekanismRecipeType.SEPARATING.get();
   }

   public boolean handleRecipe(
      IMappingCollector<NormalizedSimpleStack, Long> mapper, Recipe<?> iRecipe, RegistryAccess registryAccess, INSSFakeGroupManager groupManager
   ) {
      if (iRecipe instanceof ElectrolysisRecipe recipe) {
         boolean handled = false;
         FluidStackIngredient input = recipe.getInput();

         for (FluidStack representation : input.getRepresentations()) {
            ElectrolysisRecipe.ElectrolysisRecipeOutput output = recipe.getOutput(representation);
            GasStack leftOutput = output.left();
            GasStack rightOutput = output.right();
            if (!leftOutput.isEmpty() && !rightOutput.isEmpty()) {
               NormalizedSimpleStack nssInput = NSSFluid.createFluid(representation);
               NormalizedSimpleStack nssLeftOutput = NSSGas.createGas(leftOutput);
               NormalizedSimpleStack nssRightOutput = NSSGas.createGas(rightOutput);
               IngredientHelper ingredientHelper = new IngredientHelper(mapper);
               ingredientHelper.put(nssInput, representation.getAmount());
               ingredientHelper.put(nssRightOutput, -rightOutput.getAmount());
               if (ingredientHelper.addAsConversion(nssLeftOutput, leftOutput.getAmount())) {
                  handled = true;
               }

               ingredientHelper.resetHelper();
               ingredientHelper.put(nssInput, representation.getAmount());
               ingredientHelper.put(nssLeftOutput, -leftOutput.getAmount());
               if (ingredientHelper.addAsConversion(nssRightOutput, rightOutput.getAmount())) {
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
