package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.PigmentMixingRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

@NothingNullByDefault
public class PigmentMixingIRecipe extends PigmentMixingRecipe {
   public PigmentMixingIRecipe(
      ResourceLocation id,
      ChemicalStackIngredient.PigmentStackIngredient leftInput,
      ChemicalStackIngredient.PigmentStackIngredient rightInput,
      PigmentStack output
   ) {
      super(id, leftInput, rightInput, output);
   }

   public RecipeType<PigmentMixingRecipe> m_6671_() {
      return (RecipeType<PigmentMixingRecipe>)MekanismRecipeType.PIGMENT_MIXING.get();
   }

   public RecipeSerializer<PigmentMixingRecipe> m_7707_() {
      return (RecipeSerializer<PigmentMixingRecipe>)MekanismRecipeSerializers.PIGMENT_MIXING.get();
   }

   public String m_6076_() {
      return MekanismBlocks.PIGMENT_MIXER.getName();
   }

   public ItemStack m_8042_() {
      return MekanismBlocks.PIGMENT_MIXER.getItemStack();
   }
}
