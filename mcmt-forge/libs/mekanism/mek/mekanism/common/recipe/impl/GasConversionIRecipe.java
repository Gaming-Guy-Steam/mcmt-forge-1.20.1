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
public class GasConversionIRecipe extends ItemStackToGasRecipe {
   public GasConversionIRecipe(ResourceLocation id, ItemStackIngredient input, GasStack output) {
      super(id, input, output);
   }

   public RecipeType<ItemStackToGasRecipe> m_6671_() {
      return (RecipeType<ItemStackToGasRecipe>)MekanismRecipeType.GAS_CONVERSION.get();
   }

   public RecipeSerializer<ItemStackToGasRecipe> m_7707_() {
      return (RecipeSerializer<ItemStackToGasRecipe>)MekanismRecipeSerializers.GAS_CONVERSION.get();
   }

   public String m_6076_() {
      return "gas_conversion";
   }

   public ItemStack m_8042_() {
      return MekanismBlocks.CREATIVE_CHEMICAL_TANK.getItemStack();
   }
}
