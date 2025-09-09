package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.ItemStackToInfuseTypeRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

@NothingNullByDefault
public class InfusionConversionIRecipe extends ItemStackToInfuseTypeRecipe {
   public InfusionConversionIRecipe(ResourceLocation id, ItemStackIngredient input, InfusionStack output) {
      super(id, input, output);
   }

   public RecipeType<ItemStackToInfuseTypeRecipe> m_6671_() {
      return (RecipeType<ItemStackToInfuseTypeRecipe>)MekanismRecipeType.INFUSION_CONVERSION.get();
   }

   public RecipeSerializer<ItemStackToInfuseTypeRecipe> m_7707_() {
      return (RecipeSerializer<ItemStackToInfuseTypeRecipe>)MekanismRecipeSerializers.INFUSION_CONVERSION.get();
   }

   public String m_6076_() {
      return "infusion_conversion";
   }

   public ItemStack m_8042_() {
      return MekanismBlocks.METALLURGIC_INFUSER.getItemStack();
   }
}
