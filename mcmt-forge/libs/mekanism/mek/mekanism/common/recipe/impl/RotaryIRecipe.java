package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.serializer.RotaryRecipeSerializer;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fluids.FluidStack;

@NothingNullByDefault
public class RotaryIRecipe extends RotaryRecipe {
   public RotaryIRecipe(ResourceLocation id, FluidStackIngredient fluidInput, GasStack gasOutput) {
      super(id, fluidInput, gasOutput);
   }

   public RotaryIRecipe(ResourceLocation id, ChemicalStackIngredient.GasStackIngredient gasInput, FluidStack fluidOutput) {
      super(id, gasInput, fluidOutput);
   }

   public RotaryIRecipe(
      ResourceLocation id, FluidStackIngredient fluidInput, ChemicalStackIngredient.GasStackIngredient gasInput, GasStack gasOutput, FluidStack fluidOutput
   ) {
      super(id, fluidInput, gasInput, gasOutput, fluidOutput);
   }

   public RecipeType<RotaryRecipe> m_6671_() {
      return (RecipeType<RotaryRecipe>)MekanismRecipeType.ROTARY.get();
   }

   public RecipeSerializer<RotaryRecipe> m_7707_() {
      return (RecipeSerializer<RotaryRecipe>)MekanismRecipeSerializers.ROTARY.get();
   }

   public String m_6076_() {
      return MekanismBlocks.ROTARY_CONDENSENTRATOR.getName();
   }

   public ItemStack m_8042_() {
      return MekanismBlocks.ROTARY_CONDENSENTRATOR.getItemStack();
   }

   public static class Factory implements RotaryRecipeSerializer.IFactory<RotaryRecipe> {
      @Override
      public RotaryRecipe create(ResourceLocation id, FluidStackIngredient fluidInput, GasStack gasOutput) {
         return new RotaryIRecipe(id, fluidInput, gasOutput);
      }

      @Override
      public RotaryRecipe create(ResourceLocation id, ChemicalStackIngredient.GasStackIngredient gasInput, FluidStack fluidOutput) {
         return new RotaryIRecipe(id, gasInput, fluidOutput);
      }

      @Override
      public RotaryRecipe create(
         ResourceLocation id, FluidStackIngredient fluidInput, ChemicalStackIngredient.GasStackIngredient gasInput, GasStack gasOutput, FluidStack fluidOutput
      ) {
         return new RotaryIRecipe(id, fluidInput, gasInput, gasOutput, fluidOutput);
      }
   }
}
