package mekanism.common.integration.projecte.mappers;

import mekanism.api.recipes.ItemStackToItemStackRecipe;
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
public class ItemStackToItemStackRecipeMapper implements IRecipeTypeMapper {
   public String getName() {
      return "MekItemStackToItemStack";
   }

   public String getDescription() {
      return "Maps Mekanism Machine recipes that go from item to item. (Crushing, Enriching, Smelting)";
   }

   public boolean canHandle(RecipeType<?> recipeType) {
      return recipeType == MekanismRecipeType.CRUSHING.get()
         || recipeType == MekanismRecipeType.ENRICHING.get()
         || recipeType == MekanismRecipeType.SMELTING.get();
   }

   public boolean handleRecipe(
      IMappingCollector<NormalizedSimpleStack, Long> mapper, Recipe<?> iRecipe, RegistryAccess registryAccess, INSSFakeGroupManager groupManager
   ) {
      if (iRecipe instanceof ItemStackToItemStackRecipe recipe) {
         boolean handled = false;

         for (ItemStack representation : recipe.getInput().getRepresentations()) {
            ItemStack output = recipe.getOutput(representation);
            if (!output.m_41619_()) {
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
