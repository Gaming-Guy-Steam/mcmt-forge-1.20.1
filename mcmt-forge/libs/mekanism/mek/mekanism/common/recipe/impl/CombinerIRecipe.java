package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

@NothingNullByDefault
public class CombinerIRecipe extends CombinerRecipe {
   public CombinerIRecipe(ResourceLocation id, ItemStackIngredient mainInput, ItemStackIngredient extraInput, ItemStack output) {
      super(id, mainInput, extraInput, output);
   }

   public RecipeType<CombinerRecipe> m_6671_() {
      return (RecipeType<CombinerRecipe>)MekanismRecipeType.COMBINING.get();
   }

   public RecipeSerializer<CombinerRecipe> m_7707_() {
      return (RecipeSerializer<CombinerRecipe>)MekanismRecipeSerializers.COMBINING.get();
   }

   public String m_6076_() {
      return MekanismBlocks.COMBINER.getName();
   }

   public ItemStack m_8042_() {
      return MekanismBlocks.COMBINER.getItemStack();
   }
}
