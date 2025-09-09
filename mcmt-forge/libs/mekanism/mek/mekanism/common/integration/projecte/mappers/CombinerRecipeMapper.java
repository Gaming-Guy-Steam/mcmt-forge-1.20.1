package mekanism.common.integration.projecte.mappers;

import java.util.List;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.common.integration.projecte.IngredientHelper;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.INSSFakeGroupManager;
import moze_intel.projecte.api.mapper.recipe.IRecipeTypeMapper;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

@RecipeTypeMapper
public class CombinerRecipeMapper implements IRecipeTypeMapper {
   public String getName() {
      return "MekCombiner";
   }

   public String getDescription() {
      return "Maps Mekanism combiner recipes.";
   }

   public boolean canHandle(RecipeType<?> recipeType) {
      return recipeType == MekanismRecipeType.COMBINING.get();
   }

   public boolean handleRecipe(
      IMappingCollector<NormalizedSimpleStack, Long> mapper, Recipe<?> iRecipe, RegistryAccess registryAccess, INSSFakeGroupManager groupManager
   ) {
      if (!(iRecipe instanceof CombinerRecipe recipe)) {
         return false;
      } else {
         boolean handled = false;
         List mainRepresentations = recipe.getMainInput().getRepresentations();
         List extraRepresentations = recipe.getExtraInput().getRepresentations();

         for (ItemStack mainRepresentation : mainRepresentations) {
            NormalizedSimpleStack nssMain = NSSItem.createItem(mainRepresentation);

            for (ItemStack extraRepresentation : extraRepresentations) {
               ItemStack output = recipe.getOutput(mainRepresentation, extraRepresentation);
               if (!output.m_41619_()) {
                  IngredientHelper ingredientHelper = new IngredientHelper(mapper);
                  ingredientHelper.put(nssMain, mainRepresentation.m_41613_());
                  ingredientHelper.put(extraRepresentation);
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
