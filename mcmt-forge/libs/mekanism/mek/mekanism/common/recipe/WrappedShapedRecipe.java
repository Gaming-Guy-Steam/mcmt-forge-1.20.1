package mekanism.common.recipe;

import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.IShapedRecipe;

@NothingNullByDefault
public abstract class WrappedShapedRecipe implements CraftingRecipe, IShapedRecipe<CraftingContainer> {
   private final ShapedRecipe internal;

   protected WrappedShapedRecipe(ShapedRecipe internal) {
      this.internal = internal;
   }

   public ShapedRecipe getInternal() {
      return this.internal;
   }

   public CraftingBookCategory m_245232_() {
      return this.internal.m_245232_();
   }

   public abstract ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess);

   public boolean matches(CraftingContainer inv, Level world) {
      return this.internal.m_5818_(inv, world) && !this.assemble(inv, world.m_9598_()).m_41619_();
   }

   public boolean m_8004_(int width, int height) {
      return this.internal.m_8004_(width, height);
   }

   public ItemStack m_8043_(RegistryAccess registryAccess) {
      return this.internal.m_8043_(registryAccess);
   }

   public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
      return this.internal.m_7457_(inv);
   }

   public NonNullList<Ingredient> m_7527_() {
      return this.internal.m_7527_();
   }

   public boolean m_5598_() {
      return this.internal.m_5598_();
   }

   public String m_6076_() {
      return this.internal.m_6076_();
   }

   public ItemStack m_8042_() {
      return this.internal.m_8042_();
   }

   public ResourceLocation m_6423_() {
      return this.internal.m_6423_();
   }

   public int getRecipeWidth() {
      return this.internal.getRecipeWidth();
   }

   public int getRecipeHeight() {
      return this.internal.getRecipeHeight();
   }

   public boolean m_142505_() {
      return this.internal.m_142505_();
   }
}
