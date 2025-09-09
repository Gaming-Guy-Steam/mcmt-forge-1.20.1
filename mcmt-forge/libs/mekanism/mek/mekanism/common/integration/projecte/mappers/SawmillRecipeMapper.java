package mekanism.common.integration.projecte.mappers;

import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
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
import org.apache.commons.lang3.math.Fraction;

@RecipeTypeMapper
public class SawmillRecipeMapper implements IRecipeTypeMapper {
   public String getName() {
      return "MekSawmill";
   }

   public String getDescription() {
      return "Maps Mekanism sawmill recipes. (Disabled by default, due to causing various EMC values to be removed pertaining to charcoal/wood)";
   }

   public boolean isAvailable() {
      return false;
   }

   public boolean canHandle(RecipeType<?> recipeType) {
      return recipeType == MekanismRecipeType.SAWING.get();
   }

   public boolean handleRecipe(
      IMappingCollector<NormalizedSimpleStack, Long> mapper, Recipe<?> iRecipe, RegistryAccess registryAccess, INSSFakeGroupManager groupManager
   ) {
      if (iRecipe instanceof SawmillRecipe recipe) {
         ItemStackIngredient input = recipe.getInput();
         int primaryMultiplier = 1;
         int secondaryMultiplier = 1;
         if (recipe.getSecondaryChance() > 0.0 && recipe.getSecondaryChance() < 1.0) {
            Fraction multiplier;
            try {
               multiplier = Fraction.getFraction(recipe.getSecondaryChance()).invert();
            } catch (ArithmeticException var19) {
               return false;
            }

            primaryMultiplier = multiplier.getNumerator();
            secondaryMultiplier = multiplier.getDenominator();
         }

         boolean handled = false;

         for (ItemStack representation : input.getRepresentations()) {
            SawmillRecipe.ChanceOutput output = recipe.getOutput(representation);
            ItemStack mainOutput = output.getMainOutput();
            ItemStack secondaryOutput = output.getMaxSecondaryOutput();
            NormalizedSimpleStack nssInput = NSSItem.createItem(representation);
            IngredientHelper ingredientHelper = new IngredientHelper(mapper);
            if (secondaryOutput.m_41619_()) {
               if (!mainOutput.m_41619_()) {
                  ingredientHelper.put(nssInput, representation.m_41613_());
                  if (ingredientHelper.addAsConversion(mainOutput)) {
                     handled = true;
                  }
               }
            } else if (mainOutput.m_41619_()) {
               ingredientHelper.put(nssInput, representation.m_41613_() * primaryMultiplier);
               if (ingredientHelper.addAsConversion(NSSItem.createItem(secondaryOutput), secondaryOutput.m_41613_() * secondaryMultiplier)) {
                  handled = true;
               }
            } else {
               NormalizedSimpleStack nssMainOutput = NSSItem.createItem(mainOutput);
               NormalizedSimpleStack nssSecondaryOutput = NSSItem.createItem(secondaryOutput);
               ingredientHelper.put(nssInput, representation.m_41613_() * primaryMultiplier);
               ingredientHelper.put(nssSecondaryOutput, -secondaryOutput.m_41613_() * secondaryMultiplier);
               if (ingredientHelper.addAsConversion(nssMainOutput, mainOutput.m_41613_() * primaryMultiplier)) {
                  handled = true;
               }

               ingredientHelper.resetHelper();
               ingredientHelper.put(nssInput, representation.m_41613_() * primaryMultiplier);
               ingredientHelper.put(nssMainOutput, -mainOutput.m_41613_() * primaryMultiplier);
               if (ingredientHelper.addAsConversion(nssSecondaryOutput, secondaryOutput.m_41613_() * secondaryMultiplier)) {
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
