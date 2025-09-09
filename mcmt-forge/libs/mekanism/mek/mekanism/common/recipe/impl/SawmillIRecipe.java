package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

@NothingNullByDefault
public class SawmillIRecipe extends SawmillRecipe {
   public SawmillIRecipe(ResourceLocation id, ItemStackIngredient input, ItemStack mainOutput, ItemStack secondaryOutput, double secondaryChance) {
      super(id, input, mainOutput, secondaryOutput, secondaryChance);
   }

   public RecipeType<SawmillRecipe> m_6671_() {
      return (RecipeType<SawmillRecipe>)MekanismRecipeType.SAWING.get();
   }

   public RecipeSerializer<SawmillRecipe> m_7707_() {
      return (RecipeSerializer<SawmillRecipe>)MekanismRecipeSerializers.SAWING.get();
   }

   public String m_6076_() {
      return MekanismBlocks.PRECISION_SAWMILL.getName();
   }

   public ItemStack m_8042_() {
      return MekanismBlocks.PRECISION_SAWMILL.getItemStack();
   }
}
