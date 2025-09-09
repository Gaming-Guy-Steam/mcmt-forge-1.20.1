package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

@NothingNullByDefault
public class ActivatingIRecipe extends GasToGasRecipe {
   public ActivatingIRecipe(ResourceLocation id, ChemicalStackIngredient.GasStackIngredient input, GasStack output) {
      super(id, input, output);
   }

   public RecipeType<GasToGasRecipe> m_6671_() {
      return (RecipeType<GasToGasRecipe>)MekanismRecipeType.ACTIVATING.get();
   }

   public RecipeSerializer<GasToGasRecipe> m_7707_() {
      return (RecipeSerializer<GasToGasRecipe>)MekanismRecipeSerializers.ACTIVATING.get();
   }

   public String m_6076_() {
      return MekanismBlocks.SOLAR_NEUTRON_ACTIVATOR.getName();
   }

   public ItemStack m_8042_() {
      return MekanismBlocks.SOLAR_NEUTRON_ACTIVATOR.getItemStack();
   }
}
