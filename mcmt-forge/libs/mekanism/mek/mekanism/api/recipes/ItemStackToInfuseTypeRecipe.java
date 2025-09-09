package mekanism.api.recipes;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.resources.ResourceLocation;

@ParametersAreNotNullByDefault
public abstract class ItemStackToInfuseTypeRecipe extends ItemStackToChemicalRecipe<InfuseType, InfusionStack> {
   public ItemStackToInfuseTypeRecipe(ResourceLocation id, ItemStackIngredient input, InfusionStack output) {
      super(id, input, output);
   }
}
