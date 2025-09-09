package mekanism.api.inventory;

import java.util.List;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public interface IMekanismInventory extends ISidedItemHandler, IContentsListener {
   default boolean hasInventory() {
      return true;
   }

   List<IInventorySlot> getInventorySlots(@Nullable Direction var1);

   @Nullable
   default IInventorySlot getInventorySlot(int slot, @Nullable Direction side) {
      List<IInventorySlot> slots = this.getInventorySlots(side);
      return slot >= 0 && slot < slots.size() ? slots.get(slot) : null;
   }

   @Override
   default void setStackInSlot(int slot, ItemStack stack, @Nullable Direction side) {
      IInventorySlot inventorySlot = this.getInventorySlot(slot, side);
      if (inventorySlot != null) {
         inventorySlot.setStack(stack);
      }
   }

   @Override
   default int getSlots(@Nullable Direction side) {
      return this.getInventorySlots(side).size();
   }

   @Override
   default ItemStack getStackInSlot(int slot, @Nullable Direction side) {
      IInventorySlot inventorySlot = this.getInventorySlot(slot, side);
      return inventorySlot == null ? ItemStack.f_41583_ : inventorySlot.getStack();
   }

   @Override
   default ItemStack insertItem(int slot, ItemStack stack, @Nullable Direction side, Action action) {
      IInventorySlot inventorySlot = this.getInventorySlot(slot, side);
      return inventorySlot == null ? stack : inventorySlot.insertItem(stack, action, side == null ? AutomationType.INTERNAL : AutomationType.EXTERNAL);
   }

   @Override
   default ItemStack extractItem(int slot, int amount, @Nullable Direction side, Action action) {
      IInventorySlot inventorySlot = this.getInventorySlot(slot, side);
      return inventorySlot == null
         ? ItemStack.f_41583_
         : inventorySlot.extractItem(amount, action, side == null ? AutomationType.INTERNAL : AutomationType.EXTERNAL);
   }

   @Override
   default int getSlotLimit(int slot, @Nullable Direction side) {
      IInventorySlot inventorySlot = this.getInventorySlot(slot, side);
      return inventorySlot == null ? 0 : inventorySlot.getLimit(ItemStack.f_41583_);
   }

   @Override
   default boolean isItemValid(int slot, ItemStack stack, @Nullable Direction side) {
      IInventorySlot inventorySlot = this.getInventorySlot(slot, side);
      return inventorySlot != null && inventorySlot.isItemValid(stack);
   }

   default boolean isInventoryEmpty(@Nullable Direction side) {
      for (IInventorySlot slot : this.getInventorySlots(side)) {
         if (!slot.isEmpty()) {
            return false;
         }
      }

      return true;
   }

   default boolean isInventoryEmpty() {
      return this.isInventoryEmpty(this.getInventorySideFor());
   }
}
