package mekanism.api.recipes;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.chemical.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@ParametersAreNotNullByDefault
public abstract class MetallurgicInfuserRecipe
   extends ItemStackChemicalToItemStackRecipe<InfuseType, InfusionStack, ChemicalStackIngredient.InfusionStackIngredient> {
   public MetallurgicInfuserRecipe(
      ResourceLocation id, ItemStackIngredient itemInput, ChemicalStackIngredient.InfusionStackIngredient infusionInput, ItemStack output
   ) {
      super(id, itemInput, infusionInput, output);
   }
}
