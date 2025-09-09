package mekanism.common.recipe.serializer;

import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.PaintingRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientDeserializer;

public class PaintingRecipeSerializer<RECIPE extends PaintingRecipe>
   extends ItemStackChemicalToItemStackRecipeSerializer<Pigment, PigmentStack, ChemicalStackIngredient.PigmentStackIngredient, RECIPE> {
   public PaintingRecipeSerializer(
      ItemStackChemicalToItemStackRecipeSerializer.IFactory<Pigment, PigmentStack, ChemicalStackIngredient.PigmentStackIngredient, RECIPE> factory
   ) {
      super(factory);
   }

   @Override
   protected ChemicalIngredientDeserializer<Pigment, PigmentStack, ChemicalStackIngredient.PigmentStackIngredient> getDeserializer() {
      return ChemicalIngredientDeserializer.PIGMENT;
   }
}
