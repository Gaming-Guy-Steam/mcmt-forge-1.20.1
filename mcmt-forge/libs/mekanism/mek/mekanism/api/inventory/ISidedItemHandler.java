package mekanism.api.inventory;

import mekanism.api.Action;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public interface ISidedItemHandler extends IItemHandlerModifiable {
   @Nullable
   default Direction getInventorySideFor() {
      return null;
   }

   void setStackInSlot(int var1, ItemStack var2, @Nullable Direction var3);

   default void setStackInSlot(int slot, ItemStack stack) {
      this.setStackInSlot(slot, stack, this.getInventorySideFor());
   }

   int getSlots(@Nullable Direction var1);

   default int getSlots() {
      return this.getSlots(this.getInventorySideFor());
   }

   ItemStack getStackInSlot(int var1, @Nullable Direction var2);

   default ItemStack getStackInSlot(int slot) {
      return this.getStackInSlot(slot, this.getInventorySideFor());
   }

   ItemStack insertItem(int var1, ItemStack var2, @Nullable Direction var3, Action var4);

   default ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
      return this.insertItem(slot, stack, this.getInventorySideFor(), Action.get(!simulate));
   }

   ItemStack extractItem(int var1, int var2, @Nullable Direction var3, Action var4);

   default ItemStack extractItem(int slot, int amount, boolean simulate) {
      return this.extractItem(slot, amount, this.getInventorySideFor(), Action.get(!simulate));
   }

   int getSlotLimit(int var1, @Nullable Direction var2);

   default int getSlotLimit(int slot) {
      return this.getSlotLimit(slot, this.getInventorySideFor());
   }

   boolean isItemValid(int var1, ItemStack var2, @Nullable Direction var3);

   default boolean isItemValid(int slot, ItemStack stack) {
      return this.isItemValid(slot, stack, this.getInventorySideFor());
   }
}
