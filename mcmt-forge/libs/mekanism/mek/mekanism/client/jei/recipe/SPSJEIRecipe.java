package mekanism.client.jei.recipe;

import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;

public record SPSJEIRecipe(ChemicalStackIngredient.GasStackIngredient input, GasStack output) {
}
