package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

@NothingNullByDefault
public class CrushingIRecipe extends ItemStackToItemStackRecipe {
   public CrushingIRecipe(ResourceLocation id, ItemStackIngredient input, ItemStack output) {
      super(id, input, output);
   }

   public RecipeType<ItemStackToItemStackRecipe> m_6671_() {
      return (RecipeType<ItemStackToItemStackRecipe>)MekanismRecipeType.CRUSHING.get();
   }

   public RecipeSerializer<ItemStackToItemStackRecipe> m_7707_() {
      return (RecipeSerializer<ItemStackToItemStackRecipe>)MekanismRecipeSerializers.CRUSHING.get();
   }

   public String m_6076_() {
      return MekanismBlocks.CRUSHER.getName();
   }

   public ItemStack m_8042_() {
      return MekanismBlocks.CRUSHER.getItemStack();
   }
}
