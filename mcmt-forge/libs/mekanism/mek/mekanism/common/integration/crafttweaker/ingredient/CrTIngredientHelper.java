package mekanism.common.integration.crafttweaker.ingredient;

import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import mekanism.api.chemical.Chemical;
import mekanism.api.recipes.ingredients.InputIngredient;
import mekanism.api.recipes.ingredients.creator.IIngredientCreator;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import net.minecraft.tags.TagKey;

public class CrTIngredientHelper {
   static void assertValidAmount(String ingredientType, long amount) {
      if (amount <= 0L) {
         throw new IllegalArgumentException(ingredientType + " can only be created with a size of at least one. Received size was: " + amount);
      }
   }

   static <TYPE> TagKey<TYPE> assertValidAndGet(KnownTag<TYPE> crtTag, long amount, String ingredientType) {
      assertValidAmount(ingredientType, amount);
      return CrTUtils.validateTagAndGet(crtTag);
   }

   static void assertValid(Chemical<?> instance, long amount, String ingredientType, String chemicalType) {
      assertValidAmount(ingredientType, amount);
      if (instance.isEmptyType()) {
         throw new IllegalArgumentException(ingredientType + " cannot be created from an empty " + chemicalType + ".");
      }
   }

   static void assertValid(ICrTChemicalStack<?, ?, ?> instance, String ingredientType) {
      if (instance.isEmpty()) {
         throw new IllegalArgumentException(ingredientType + " cannot be created from an empty stack.");
      }
   }

   @SafeVarargs
   static <INGREDIENT extends InputIngredient<?>> INGREDIENT createMulti(
      String ingredientType, IIngredientCreator<?, ?, INGREDIENT> creator, INGREDIENT... ingredients
   ) {
      if (ingredients.length == 0) {
         throw new IllegalArgumentException("Multi " + ingredientType + " ingredients cannot be made out of no ingredients!");
      } else {
         return ingredients.length == 1 ? ingredients[0] : creator.createMulti(ingredients);
      }
   }
}
