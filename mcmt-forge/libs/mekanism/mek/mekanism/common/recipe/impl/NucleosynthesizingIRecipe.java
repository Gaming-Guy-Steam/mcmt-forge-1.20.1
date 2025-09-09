package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.NucleosynthesizingRecipe;
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
public class NucleosynthesizingIRecipe extends NucleosynthesizingRecipe {
   public NucleosynthesizingIRecipe(
      ResourceLocation id, ItemStackIngredient itemInput, ChemicalStackIngredient.GasStackIngredient gasInput, ItemStack output, int duration
   ) {
      super(id, itemInput, gasInput, output, duration);
   }

   public RecipeType<NucleosynthesizingRecipe> m_6671_() {
      return (RecipeType<NucleosynthesizingRecipe>)MekanismRecipeType.NUCLEOSYNTHESIZING.get();
   }

   public RecipeSerializer<NucleosynthesizingRecipe> m_7707_() {
      return (RecipeSerializer<NucleosynthesizingRecipe>)MekanismRecipeSerializers.NUCLEOSYNTHESIZING.get();
   }

   public String m_6076_() {
      return MekanismBlocks.ANTIPROTONIC_NUCLEOSYNTHESIZER.getName();
   }

   public ItemStack m_8042_() {
      return MekanismBlocks.ANTIPROTONIC_NUCLEOSYNTHESIZER.getItemStack();
   }
}
