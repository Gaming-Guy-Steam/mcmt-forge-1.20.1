package mekanism.api.recipes;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.chemical.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@ParametersAreNotNullByDefault
public abstract class ItemStackGasToItemStackRecipe extends ItemStackChemicalToItemStackRecipe<Gas, GasStack, ChemicalStackIngredient.GasStackIngredient> {
   public ItemStackGasToItemStackRecipe(
      ResourceLocation id, ItemStackIngredient itemInput, ChemicalStackIngredient.GasStackIngredient gasInput, ItemStack output
   ) {
      super(id, itemInput, gasInput, output);
   }
}
