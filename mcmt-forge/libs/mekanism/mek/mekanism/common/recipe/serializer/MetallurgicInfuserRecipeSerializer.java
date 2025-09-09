package mekanism.common.recipe.serializer;

import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientDeserializer;

public class MetallurgicInfuserRecipeSerializer<RECIPE extends MetallurgicInfuserRecipe>
   extends ItemStackChemicalToItemStackRecipeSerializer<InfuseType, InfusionStack, ChemicalStackIngredient.InfusionStackIngredient, RECIPE> {
   public MetallurgicInfuserRecipeSerializer(
      ItemStackChemicalToItemStackRecipeSerializer.IFactory<InfuseType, InfusionStack, ChemicalStackIngredient.InfusionStackIngredient, RECIPE> factory
   ) {
      super(factory);
   }

   @Override
   protected ChemicalIngredientDeserializer<InfuseType, InfusionStack, ChemicalStackIngredient.InfusionStackIngredient> getDeserializer() {
      return ChemicalIngredientDeserializer.INFUSION;
   }
}
