package mekanism.api.recipes;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.chemical.ChemicalChemicalToChemicalRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import net.minecraft.resources.ResourceLocation;

@ParametersAreNotNullByDefault
public abstract class ChemicalInfuserRecipe extends ChemicalChemicalToChemicalRecipe<Gas, GasStack, ChemicalStackIngredient.GasStackIngredient> {
   public ChemicalInfuserRecipe(
      ResourceLocation id, ChemicalStackIngredient.GasStackIngredient leftInput, ChemicalStackIngredient.GasStackIngredient rightInput, GasStack output
   ) {
      super(id, leftInput, rightInput, output);
   }
}
