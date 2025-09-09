package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

@NothingNullByDefault
public class PressurizedReactionIRecipe extends PressurizedReactionRecipe {
   public PressurizedReactionIRecipe(
      ResourceLocation id,
      ItemStackIngredient inputSolid,
      FluidStackIngredient inputFluid,
      ChemicalStackIngredient.GasStackIngredient inputGas,
      FloatingLong energyRequired,
      int duration,
      ItemStack outputItem,
      GasStack outputGas
   ) {
      super(id, inputSolid, inputFluid, inputGas, energyRequired, duration, outputItem, outputGas);
   }

   public RecipeType<PressurizedReactionRecipe> m_6671_() {
      return (RecipeType<PressurizedReactionRecipe>)MekanismRecipeType.REACTION.get();
   }

   public RecipeSerializer<PressurizedReactionRecipe> m_7707_() {
      return (RecipeSerializer<PressurizedReactionRecipe>)MekanismRecipeSerializers.REACTION.get();
   }

   public String m_6076_() {
      return MekanismBlocks.PRESSURIZED_REACTION_CHAMBER.getName();
   }

   public ItemStack m_8042_() {
      return MekanismBlocks.PRESSURIZED_REACTION_CHAMBER.getItemStack();
   }
}
