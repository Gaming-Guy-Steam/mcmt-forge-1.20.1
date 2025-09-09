package mekanism.common.integration.projecte.mappers;

import java.util.List;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.common.integration.projecte.IngredientHelper;
import mekanism.common.integration.projecte.NSSInfuseType;
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
public class MetallurgicInfuserRecipeMapper implements IRecipeTypeMapper {
   public String getName() {
      return "MekMetallurgicInfuser";
   }

   public String getDescription() {
      return "Maps Mekanism metallurgic infuser recipes.";
   }

   public boolean canHandle(RecipeType<?> recipeType) {
      return recipeType == MekanismRecipeType.METALLURGIC_INFUSING.get();
   }

   public boolean handleRecipe(
      IMappingCollector<NormalizedSimpleStack, Long> mapper, Recipe<?> iRecipe, RegistryAccess registryAccess, INSSFakeGroupManager groupManager
   ) {
      if (!(iRecipe instanceof MetallurgicInfuserRecipe recipe)) {
         return false;
      } else {
         boolean handled = false;
         List infuseTypeRepresentations = recipe.getChemicalInput().getRepresentations();
         List itemRepresentations = recipe.getItemInput().getRepresentations();

         for (InfusionStack infuseTypeRepresentation : infuseTypeRepresentations) {
            NormalizedSimpleStack nssInfuseType = NSSInfuseType.createInfuseType(infuseTypeRepresentation);

            for (ItemStack itemRepresentation : itemRepresentations) {
               ItemStack output = recipe.getOutput(itemRepresentation, infuseTypeRepresentation);
               if (!output.m_41619_()) {
                  IngredientHelper ingredientHelper = new IngredientHelper(mapper);
                  ingredientHelper.put(nssInfuseType, infuseTypeRepresentation.getAmount());
                  ingredientHelper.put(itemRepresentation);
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
