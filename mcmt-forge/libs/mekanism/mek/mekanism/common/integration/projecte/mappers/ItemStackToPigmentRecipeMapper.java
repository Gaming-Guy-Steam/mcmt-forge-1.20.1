package mekanism.common.integration.projecte.mappers;

import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.ItemStackToPigmentRecipe;
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
public class ItemStackToPigmentRecipeMapper implements IRecipeTypeMapper {
   public String getName() {
      return "MekItemStackToPigment";
   }

   public String getDescription() {
      return "Maps Mekanism item stack to pigment recipes. (Pigment Extracting)";
   }

   public boolean canHandle(RecipeType<?> recipeType) {
      return recipeType == MekanismRecipeType.PIGMENT_EXTRACTING.get();
   }

   public boolean handleRecipe(
      IMappingCollector<NormalizedSimpleStack, Long> mapper, Recipe<?> iRecipe, RegistryAccess registryAccess, INSSFakeGroupManager groupManager
   ) {
      if (iRecipe instanceof ItemStackToPigmentRecipe recipe) {
         boolean handled = false;

         for (ItemStack representation : recipe.getInput().getRepresentations()) {
            PigmentStack output = recipe.getOutput(representation);
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
