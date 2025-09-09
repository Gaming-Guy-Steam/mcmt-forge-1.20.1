package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismItems;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

@NothingNullByDefault
public class EnergyConversionIRecipe extends ItemStackToEnergyRecipe {
   public EnergyConversionIRecipe(ResourceLocation id, ItemStackIngredient input, FloatingLong output) {
      super(id, input, output);
   }

   public RecipeType<ItemStackToEnergyRecipe> m_6671_() {
      return (RecipeType<ItemStackToEnergyRecipe>)MekanismRecipeType.ENERGY_CONVERSION.get();
   }

   public RecipeSerializer<ItemStackToEnergyRecipe> m_7707_() {
      return (RecipeSerializer<ItemStackToEnergyRecipe>)MekanismRecipeSerializers.ENERGY_CONVERSION.get();
   }

   public String m_6076_() {
      return "energy_conversion";
   }

   public ItemStack m_8042_() {
      return MekanismItems.ENERGY_TABLET.getItemStack();
   }
}
