package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
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
public class InjectingIRecipe extends ItemStackGasToItemStackRecipe {
   public InjectingIRecipe(ResourceLocation id, ItemStackIngredient itemInput, ChemicalStackIngredient.GasStackIngredient gasInput, ItemStack output) {
      super(id, itemInput, gasInput, output);
   }

   public RecipeType<ItemStackGasToItemStackRecipe> m_6671_() {
      return (RecipeType<ItemStackGasToItemStackRecipe>)MekanismRecipeType.INJECTING.get();
   }

   public RecipeSerializer<ItemStackGasToItemStackRecipe> m_7707_() {
      return (RecipeSerializer<ItemStackGasToItemStackRecipe>)MekanismRecipeSerializers.INJECTING.get();
   }

   public String m_6076_() {
      return MekanismBlocks.CHEMICAL_INJECTION_CHAMBER.getName();
   }

   public ItemStack m_8042_() {
      return MekanismBlocks.CHEMICAL_INJECTION_CHAMBER.getItemStack();
   }
}
