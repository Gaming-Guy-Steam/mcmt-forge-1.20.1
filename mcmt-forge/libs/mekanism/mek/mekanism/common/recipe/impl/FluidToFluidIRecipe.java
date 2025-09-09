package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fluids.FluidStack;

@NothingNullByDefault
public class FluidToFluidIRecipe extends FluidToFluidRecipe {
   public FluidToFluidIRecipe(ResourceLocation id, FluidStackIngredient input, FluidStack output) {
      super(id, input, output);
   }

   public RecipeType<FluidToFluidRecipe> m_6671_() {
      return (RecipeType<FluidToFluidRecipe>)MekanismRecipeType.EVAPORATING.get();
   }

   public RecipeSerializer<FluidToFluidRecipe> m_7707_() {
      return (RecipeSerializer<FluidToFluidRecipe>)MekanismRecipeSerializers.EVAPORATING.get();
   }

   public String m_6076_() {
      return MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER.getName();
   }

   public ItemStack m_8042_() {
      return MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER.getItemStack();
   }
}
