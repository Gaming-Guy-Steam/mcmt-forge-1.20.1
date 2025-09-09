package mekanism.api.recipes;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.chemical.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@ParametersAreNotNullByDefault
public abstract class PaintingRecipe extends ItemStackChemicalToItemStackRecipe<Pigment, PigmentStack, ChemicalStackIngredient.PigmentStackIngredient> {
   public PaintingRecipe(ResourceLocation id, ItemStackIngredient itemInput, ChemicalStackIngredient.PigmentStackIngredient pigmentInput, ItemStack output) {
      super(id, itemInput, pigmentInput, output);
   }
}
