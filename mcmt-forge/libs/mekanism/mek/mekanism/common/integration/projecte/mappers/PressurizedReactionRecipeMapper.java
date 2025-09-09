package mekanism.common.integration.projecte.mappers;

import java.util.List;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.common.integration.projecte.IngredientHelper;
import mekanism.common.integration.projecte.NSSGas;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.INSSFakeGroupManager;
import moze_intel.projecte.api.mapper.recipe.IRecipeTypeMapper;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NSSFluid;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fluids.FluidStack;

@RecipeTypeMapper
public class PressurizedReactionRecipeMapper implements IRecipeTypeMapper {
   public String getName() {
      return "MekPressurizedReaction";
   }

   public String getDescription() {
      return "Maps Mekanism pressurized reaction recipes.";
   }

   public boolean isAvailable() {
      return false;
   }

   public boolean canHandle(RecipeType<?> recipeType) {
      return recipeType == MekanismRecipeType.REACTION.get();
   }

   public boolean handleRecipe(
      IMappingCollector<NormalizedSimpleStack, Long> mapper, Recipe<?> iRecipe, RegistryAccess registryAccess, INSSFakeGroupManager groupManager
   ) {
      if (!(iRecipe instanceof PressurizedReactionRecipe recipe)) {
         return false;
      } else {
         boolean handled = false;
         List itemRepresentations = recipe.getInputSolid().getRepresentations();
         List fluidRepresentations = recipe.getInputFluid().getRepresentations();
         List gasRepresentations = recipe.getInputGas().getRepresentations();

         for (ItemStack itemRepresentation : itemRepresentations) {
            NormalizedSimpleStack nssItem = NSSItem.createItem(itemRepresentation);

            for (FluidStack fluidRepresentation : fluidRepresentations) {
               NormalizedSimpleStack nssFluid = NSSFluid.createFluid(fluidRepresentation);

               for (GasStack gasRepresentation : gasRepresentations) {
                  NormalizedSimpleStack nssGas = NSSGas.createGas(gasRepresentation);
                  PressurizedReactionRecipe.PressurizedReactionRecipeOutput output = recipe.getOutput(
                     itemRepresentation, fluidRepresentation, gasRepresentation
                  );
                  ItemStack itemOutput = output.item();
                  GasStack gasOutput = output.gas();
                  IngredientHelper ingredientHelper = new IngredientHelper(mapper);
                  ingredientHelper.put(nssItem, itemRepresentation.m_41613_());
                  ingredientHelper.put(nssFluid, fluidRepresentation.getAmount());
                  ingredientHelper.put(nssGas, gasRepresentation.getAmount());
                  if (itemOutput.m_41619_()) {
                     if (!gasOutput.isEmpty() && ingredientHelper.addAsConversion(gasOutput)) {
                        handled = true;
                     }
                  } else if (gasOutput.isEmpty()) {
                     if (ingredientHelper.addAsConversion(itemOutput)) {
                        handled = true;
                     }
                  } else {
                     NormalizedSimpleStack nssItemOutput = NSSItem.createItem(itemOutput);
                     NormalizedSimpleStack nssGasOutput = NSSGas.createGas(gasOutput);
                     ingredientHelper.put(nssGasOutput, -gasOutput.getAmount());
                     if (ingredientHelper.addAsConversion(nssItemOutput, itemOutput.m_41613_())) {
                        handled = true;
                     }

                     ingredientHelper.resetHelper();
                     ingredientHelper.put(nssItem, itemRepresentation.m_41613_());
                     ingredientHelper.put(nssFluid, fluidRepresentation.getAmount());
                     ingredientHelper.put(nssGas, gasRepresentation.getAmount());
                     ingredientHelper.put(nssItemOutput, -itemOutput.m_41613_());
                     if (ingredientHelper.addAsConversion(nssGasOutput, gasOutput.getAmount())) {
                        handled = true;
                     }
                  }
               }
            }
         }

         return handled;
      }
   }
}
