package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.ItemStackToPigmentRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

@NothingNullByDefault
public class PigmentExtractingIRecipe extends ItemStackToPigmentRecipe {
   public PigmentExtractingIRecipe(ResourceLocation id, ItemStackIngredient input, PigmentStack output) {
      super(id, input, output);
   }

   public RecipeType<ItemStackToPigmentRecipe> m_6671_() {
      return (RecipeType<ItemStackToPigmentRecipe>)MekanismRecipeType.PIGMENT_EXTRACTING.get();
   }

   public RecipeSerializer<ItemStackToPigmentRecipe> m_7707_() {
      return (RecipeSerializer<ItemStackToPigmentRecipe>)MekanismRecipeSerializers.PIGMENT_EXTRACTING.get();
   }

   public String m_6076_() {
      return MekanismBlocks.PIGMENT_EXTRACTOR.getName();
   }

   public ItemStack m_8042_() {
      return MekanismBlocks.PIGMENT_EXTRACTOR.getItemStack();
   }
}
