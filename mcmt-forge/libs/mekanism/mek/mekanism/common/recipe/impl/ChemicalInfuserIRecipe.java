package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

@NothingNullByDefault
public class ChemicalInfuserIRecipe extends ChemicalInfuserRecipe {
   public ChemicalInfuserIRecipe(
      ResourceLocation id, ChemicalStackIngredient.GasStackIngredient leftInput, ChemicalStackIngredient.GasStackIngredient rightInput, GasStack output
   ) {
      super(id, leftInput, rightInput, output);
   }

   public RecipeType<ChemicalInfuserRecipe> m_6671_() {
      return (RecipeType<ChemicalInfuserRecipe>)MekanismRecipeType.CHEMICAL_INFUSING.get();
   }

   public RecipeSerializer<ChemicalInfuserRecipe> m_7707_() {
      return (RecipeSerializer<ChemicalInfuserRecipe>)MekanismRecipeSerializers.CHEMICAL_INFUSING.get();
   }

   public String m_6076_() {
      return MekanismBlocks.CHEMICAL_INFUSER.getName();
   }

   public ItemStack m_8042_() {
      return MekanismBlocks.CHEMICAL_INFUSER.getItemStack();
   }
}
