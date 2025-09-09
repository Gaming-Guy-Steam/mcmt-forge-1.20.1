package mekanism.api.recipes;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.resources.ResourceLocation;

@ParametersAreNotNullByDefault
public abstract class ItemStackToGasRecipe extends ItemStackToChemicalRecipe<Gas, GasStack> {
   public ItemStackToGasRecipe(ResourceLocation id, ItemStackIngredient input, GasStack output) {
      super(id, input, output);
   }
}
