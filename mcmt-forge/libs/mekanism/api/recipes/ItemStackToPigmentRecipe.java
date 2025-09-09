package mekanism.api.recipes;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.resources.ResourceLocation;

@ParametersAreNotNullByDefault
public abstract class ItemStackToPigmentRecipe extends ItemStackToChemicalRecipe<Pigment, PigmentStack> {
   public ItemStackToPigmentRecipe(ResourceLocation id, ItemStackIngredient input, PigmentStack output) {
      super(id, input, output);
   }
}
