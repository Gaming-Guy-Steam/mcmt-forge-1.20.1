package mekanism.common.content.assemblicator;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class RecipeFormula {
   public final NonNullList<ItemStack> input = NonNullList.m_122780_(9, ItemStack.f_41583_);
   @Nullable
   public CraftingRecipe recipe;
   private final CraftingContainer dummy = MekanismUtils.getDummyCraftingInv();

   public RecipeFormula(Level world, NonNullList<ItemStack> inv) {
      for (int i = 0; i < 9; i++) {
         this.input.set(i, ((ItemStack)inv.get(i)).m_255036_(1));
      }

      this.resetToRecipe();
      this.recipe = getRecipeFromGrid(this.dummy, world);
   }

   public RecipeFormula(Level world, List<IInventorySlot> craftingGridSlots) {
      for (int i = 0; i < craftingGridSlots.size(); i++) {
         IInventorySlot craftingSlot = craftingGridSlots.get(i);
         if (!craftingSlot.isEmpty()) {
            this.input.set(i, craftingSlot.getStack().m_255036_(1));
         }
      }

      this.resetToRecipe();
      this.recipe = getRecipeFromGrid(this.dummy, world);
   }

   public ItemStack getInputStack(int slot) {
      return (ItemStack)this.input.get(slot);
   }

   private void resetToRecipe() {
      for (int i = 0; i < 9; i++) {
         this.dummy.m_6836_(i, (ItemStack)this.input.get(i));
      }
   }

   public boolean matches(Level world, List<IInventorySlot> craftingGridSlots) {
      if (this.recipe == null) {
         return false;
      } else {
         for (int i = 0; i < craftingGridSlots.size(); i++) {
            this.dummy.m_6836_(i, craftingGridSlots.get(i).getStack().m_255036_(1));
         }

         return this.recipe.m_5818_(this.dummy, world);
      }
   }

   public ItemStack assemble(RegistryAccess registryAccess) {
      return this.recipe == null ? ItemStack.f_41583_ : this.recipe.m_5874_(this.dummy, registryAccess);
   }

   public NonNullList<ItemStack> getRemainingItems() {
      return this.recipe == null ? NonNullList.m_122779_() : this.recipe.m_7457_(this.dummy);
   }

   public boolean isIngredientInPos(Level world, ItemStack stack, int i) {
      if (this.recipe == null) {
         return false;
      } else if (stack.m_41619_() && !((ItemStack)this.input.get(i)).m_41619_()) {
         return false;
      } else {
         this.resetToRecipe();
         this.dummy.m_6836_(i, stack);
         return this.recipe.m_5818_(this.dummy, world);
      }
   }

   public IntList getIngredientIndices(Level world, ItemStack stack) {
      IntList ret = new IntArrayList();
      if (this.recipe != null) {
         for (int i = 0; i < 9; i++) {
            this.dummy.m_6836_(i, stack);
            if (this.recipe.m_5818_(this.dummy, world)) {
               ret.add(i);
            }

            this.dummy.m_6836_(i, (ItemStack)this.input.get(i));
         }
      }

      return ret;
   }

   public boolean isValidFormula() {
      return this.getRecipe() != null;
   }

   @Nullable
   public CraftingRecipe getRecipe() {
      return this.recipe;
   }

   public boolean isFormulaEqual(RecipeFormula formula) {
      return formula.getRecipe() == this.getRecipe();
   }

   public void setStack(Level world, int index, ItemStack stack) {
      this.input.set(index, stack);
      this.resetToRecipe();
      this.recipe = getRecipeFromGrid(this.dummy, world);
   }

   @Nullable
   private static CraftingRecipe getRecipeFromGrid(CraftingContainer inv, Level world) {
      return (CraftingRecipe)MekanismRecipeType.getRecipeFor(RecipeType.f_44107_, inv, world).orElse(null);
   }
}
