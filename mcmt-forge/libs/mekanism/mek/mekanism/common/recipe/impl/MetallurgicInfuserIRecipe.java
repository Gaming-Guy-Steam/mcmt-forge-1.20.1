package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

@NothingNullByDefault
public class MetallurgicInfuserIRecipe extends MetallurgicInfuserRecipe {
   public MetallurgicInfuserIRecipe(
      ResourceLocation id, ItemStackIngredient itemInput, ChemicalStackIngredient.InfusionStackIngredient infusionInput, ItemStack output
   ) {
      super(id, itemInput, infusionInput, output);
   }

   public RecipeType<MetallurgicInfuserRecipe> m_6671_() {
      return (RecipeType<MetallurgicInfuserRecipe>)MekanismRecipeType.METALLURGIC_INFUSING.get();
   }

   public RecipeSerializer<MetallurgicInfuserRecipe> m_7707_() {
      return (RecipeSerializer<MetallurgicInfuserRecipe>)MekanismRecipeSerializers.METALLURGIC_INFUSING.get();
   }

   public String m_6076_() {
      return MekanismBlocks.METALLURGIC_INFUSER.getName();
   }

   public ItemStack m_8042_() {
      return MekanismBlocks.METALLURGIC_INFUSER.getItemStack();
   }
}
