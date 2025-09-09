package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.PaintingRecipe;
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
public class PaintingIRecipe extends PaintingRecipe {
   public PaintingIRecipe(ResourceLocation id, ItemStackIngredient itemInput, ChemicalStackIngredient.PigmentStackIngredient pigmentInput, ItemStack output) {
      super(id, itemInput, pigmentInput, output);
   }

   public RecipeType<PaintingRecipe> m_6671_() {
      return (RecipeType<PaintingRecipe>)MekanismRecipeType.PAINTING.get();
   }

   public RecipeSerializer<PaintingRecipe> m_7707_() {
      return (RecipeSerializer<PaintingRecipe>)MekanismRecipeSerializers.PAINTING.get();
   }

   public String m_6076_() {
      return MekanismBlocks.PAINTING_MACHINE.getName();
   }

   public ItemStack m_8042_() {
      return MekanismBlocks.PAINTING_MACHINE.getItemStack();
   }
}
