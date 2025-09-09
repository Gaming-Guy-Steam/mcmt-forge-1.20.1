package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.ItemStackToFluidRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.RegistryUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fluids.FluidStack;

@NothingNullByDefault
public class NutritionalLiquifierIRecipe extends ItemStackToFluidRecipe {
   public NutritionalLiquifierIRecipe(Item item, ItemStackIngredient input, FluidStack output) {
      this(Mekanism.rl("liquifier/" + RegistryUtils.getName(item).toString().replace(':', '/')), input, output);
   }

   public NutritionalLiquifierIRecipe(ResourceLocation id, ItemStackIngredient input, FluidStack output) {
      super(id, input, output);
   }

   public RecipeType<ItemStackToFluidRecipe> m_6671_() {
      return null;
   }

   public RecipeSerializer<ItemStackToFluidRecipe> m_7707_() {
      return null;
   }

   public String m_6076_() {
      return MekanismBlocks.NUTRITIONAL_LIQUIFIER.getName();
   }

   public ItemStack m_8042_() {
      return MekanismBlocks.NUTRITIONAL_LIQUIFIER.getItemStack();
   }
}
