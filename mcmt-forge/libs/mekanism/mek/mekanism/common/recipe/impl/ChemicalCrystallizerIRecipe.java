package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

@NothingNullByDefault
public class ChemicalCrystallizerIRecipe extends ChemicalCrystallizerRecipe {
   public ChemicalCrystallizerIRecipe(ResourceLocation id, ChemicalStackIngredient<?, ?> input, ItemStack output) {
      super(id, input, output);
   }

   public RecipeType<ChemicalCrystallizerRecipe> m_6671_() {
      return (RecipeType<ChemicalCrystallizerRecipe>)MekanismRecipeType.CRYSTALLIZING.get();
   }

   public RecipeSerializer<ChemicalCrystallizerRecipe> m_7707_() {
      return (RecipeSerializer<ChemicalCrystallizerRecipe>)MekanismRecipeSerializers.CRYSTALLIZING.get();
   }

   public String m_6076_() {
      return MekanismBlocks.CHEMICAL_CRYSTALLIZER.getName();
   }

   public ItemStack m_8042_() {
      return MekanismBlocks.CHEMICAL_CRYSTALLIZER.getItemStack();
   }
}
