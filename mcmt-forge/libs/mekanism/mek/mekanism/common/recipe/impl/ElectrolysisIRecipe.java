package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

@NothingNullByDefault
public class ElectrolysisIRecipe extends ElectrolysisRecipe {
   public ElectrolysisIRecipe(ResourceLocation id, FluidStackIngredient input, FloatingLong energyMultiplier, GasStack leftGasOutput, GasStack rightGasOutput) {
      super(id, input, energyMultiplier, leftGasOutput, rightGasOutput);
   }

   public RecipeType<ElectrolysisRecipe> m_6671_() {
      return (RecipeType<ElectrolysisRecipe>)MekanismRecipeType.SEPARATING.get();
   }

   public RecipeSerializer<ElectrolysisRecipe> m_7707_() {
      return (RecipeSerializer<ElectrolysisRecipe>)MekanismRecipeSerializers.SEPARATING.get();
   }

   public String m_6076_() {
      return MekanismBlocks.ELECTROLYTIC_SEPARATOR.getName();
   }

   public ItemStack m_8042_() {
      return MekanismBlocks.ELECTROLYTIC_SEPARATOR.getItemStack();
   }
}
