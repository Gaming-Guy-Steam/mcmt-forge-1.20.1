package mekanism.common.recipe.serializer;

import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientDeserializer;

public class ItemStackGasToItemStackRecipeSerializer<RECIPE extends ItemStackGasToItemStackRecipe>
   extends ItemStackChemicalToItemStackRecipeSerializer<Gas, GasStack, ChemicalStackIngredient.GasStackIngredient, RECIPE> {
   public ItemStackGasToItemStackRecipeSerializer(
      ItemStackChemicalToItemStackRecipeSerializer.IFactory<Gas, GasStack, ChemicalStackIngredient.GasStackIngredient, RECIPE> factory
   ) {
      super(factory);
   }

   @Override
   protected ChemicalIngredientDeserializer<Gas, GasStack, ChemicalStackIngredient.GasStackIngredient> getDeserializer() {
      return ChemicalIngredientDeserializer.GAS;
   }
}
