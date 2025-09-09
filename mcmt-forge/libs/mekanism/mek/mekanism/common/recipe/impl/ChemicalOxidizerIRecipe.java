package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

@NothingNullByDefault
public class ChemicalOxidizerIRecipe extends ItemStackToGasRecipe {
   public ChemicalOxidizerIRecipe(ResourceLocation id, ItemStackIngredient input, GasStack output) {
      super(id, input, output);
   }

   public RecipeType<ItemStackToGasRecipe> m_6671_() {
      return (RecipeType<ItemStackToGasRecipe>)MekanismRecipeType.OXIDIZING.get();
   }

   public RecipeSerializer<ItemStackToGasRecipe> m_7707_() {
      return (RecipeSerializer<ItemStackToGasRecipe>)MekanismRecipeSerializers.OXIDIZING.get();
   }

   public String m_6076_() {
      return MekanismBlocks.CHEMICAL_OXIDIZER.getName();
   }

   public ItemStack m_8042_() {
      return MekanismBlocks.CHEMICAL_OXIDIZER.getItemStack();
   }
}
