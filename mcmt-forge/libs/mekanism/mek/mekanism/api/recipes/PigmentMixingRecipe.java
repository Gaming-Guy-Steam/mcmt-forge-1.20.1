package mekanism.api.recipes;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.chemical.ChemicalChemicalToChemicalRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import net.minecraft.resources.ResourceLocation;

@ParametersAreNotNullByDefault
public abstract class PigmentMixingRecipe extends ChemicalChemicalToChemicalRecipe<Pigment, PigmentStack, ChemicalStackIngredient.PigmentStackIngredient> {
   public PigmentMixingRecipe(
      ResourceLocation id,
      ChemicalStackIngredient.PigmentStackIngredient leftInput,
      ChemicalStackIngredient.PigmentStackIngredient rightInput,
      PigmentStack output
   ) {
      super(id, leftInput, rightInput, output);
   }
}
