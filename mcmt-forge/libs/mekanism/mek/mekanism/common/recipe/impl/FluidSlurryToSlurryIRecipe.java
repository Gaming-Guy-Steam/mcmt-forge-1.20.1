package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.FluidSlurryToSlurryRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

@NothingNullByDefault
public class FluidSlurryToSlurryIRecipe extends FluidSlurryToSlurryRecipe {
   public FluidSlurryToSlurryIRecipe(
      ResourceLocation id, FluidStackIngredient fluidInput, ChemicalStackIngredient.SlurryStackIngredient slurryInput, SlurryStack output
   ) {
      super(id, fluidInput, slurryInput, output);
   }

   public RecipeType<FluidSlurryToSlurryRecipe> m_6671_() {
      return (RecipeType<FluidSlurryToSlurryRecipe>)MekanismRecipeType.WASHING.get();
   }

   public RecipeSerializer<FluidSlurryToSlurryRecipe> m_7707_() {
      return (RecipeSerializer<FluidSlurryToSlurryRecipe>)MekanismRecipeSerializers.WASHING.get();
   }

   public String m_6076_() {
      return MekanismBlocks.CHEMICAL_WASHER.getName();
   }

   public ItemStack m_8042_() {
      return MekanismBlocks.CHEMICAL_WASHER.getItemStack();
   }
}
