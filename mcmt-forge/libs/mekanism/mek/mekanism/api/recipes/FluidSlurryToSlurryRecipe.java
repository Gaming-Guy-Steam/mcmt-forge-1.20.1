package mekanism.api.recipes;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.chemical.FluidChemicalToChemicalRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.resources.ResourceLocation;

@ParametersAreNotNullByDefault
public abstract class FluidSlurryToSlurryRecipe extends FluidChemicalToChemicalRecipe<Slurry, SlurryStack, ChemicalStackIngredient.SlurryStackIngredient> {
   public FluidSlurryToSlurryRecipe(
      ResourceLocation id, FluidStackIngredient fluidInput, ChemicalStackIngredient.SlurryStackIngredient slurryInput, SlurryStack output
   ) {
      super(id, fluidInput, slurryInput, output);
   }
}
