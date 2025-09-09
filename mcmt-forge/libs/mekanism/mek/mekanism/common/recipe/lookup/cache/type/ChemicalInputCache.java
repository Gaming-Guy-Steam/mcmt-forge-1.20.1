package mekanism.common.recipe.lookup.cache.type;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.recipe.ingredient.chemical.MultiChemicalStackIngredient;
import mekanism.common.recipe.ingredient.chemical.SingleChemicalStackIngredient;
import mekanism.common.recipe.ingredient.chemical.TaggedChemicalStackIngredient;

public class ChemicalInputCache<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, RECIPE extends MekanismRecipe>
   extends BaseInputCache<CHEMICAL, STACK, ChemicalStackIngredient<CHEMICAL, STACK>, RECIPE> {
   public boolean mapInputs(RECIPE recipe, ChemicalStackIngredient<CHEMICAL, STACK> inputIngredient) {
      if (inputIngredient instanceof SingleChemicalStackIngredient<CHEMICAL, STACK> single) {
         this.addInputCache(single.getInputRaw(), recipe);
      } else {
         if (!(inputIngredient instanceof TaggedChemicalStackIngredient<CHEMICAL, STACK> tagged)) {
            if (inputIngredient instanceof MultiChemicalStackIngredient<CHEMICAL, STACK, ?> multi) {
               return this.mapMultiInputs(recipe, multi);
            }

            return true;
         }

         for (CHEMICAL input : tagged.getRawInput()) {
            this.addInputCache(input, recipe);
         }
      }

      return false;
   }

   protected CHEMICAL createKey(STACK stack) {
      return stack.getType();
   }

   public boolean isEmpty(STACK input) {
      return input.isEmpty();
   }
}
