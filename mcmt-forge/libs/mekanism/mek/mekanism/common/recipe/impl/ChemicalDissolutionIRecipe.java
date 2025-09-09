package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

@NothingNullByDefault
public class ChemicalDissolutionIRecipe extends ChemicalDissolutionRecipe {
   public ChemicalDissolutionIRecipe(
      ResourceLocation id, ItemStackIngredient itemInput, ChemicalStackIngredient.GasStackIngredient gasInput, ChemicalStack<?> output
   ) {
      super(id, itemInput, gasInput, output);
   }

   public RecipeType<ChemicalDissolutionRecipe> m_6671_() {
      return (RecipeType<ChemicalDissolutionRecipe>)MekanismRecipeType.DISSOLUTION.get();
   }

   public RecipeSerializer<ChemicalDissolutionRecipe> m_7707_() {
      return (RecipeSerializer<ChemicalDissolutionRecipe>)MekanismRecipeSerializers.DISSOLUTION.get();
   }

   public String m_6076_() {
      return MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER.getName();
   }

   public ItemStack m_8042_() {
      return MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER.getItemStack();
   }
}
