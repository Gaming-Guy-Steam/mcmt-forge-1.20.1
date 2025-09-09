package mekanism.common.capabilities.proxy;

import mekanism.api.Action;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.inventory.ISidedItemHandler;
import mekanism.common.capabilities.holder.IHolder;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ProxyItemHandler extends ProxyHandler implements IItemHandlerModifiable {
   private final ISidedItemHandler inventory;

   public ProxyItemHandler(ISidedItemHandler inventory, @Nullable Direction side, @Nullable IHolder holder) {
      super(side, holder);
      this.inventory = inventory;
   }

   public int getSlots() {
      return this.inventory.getSlots(this.side);
   }

   public ItemStack getStackInSlot(int slot) {
      return this.inventory.getStackInSlot(slot, this.side);
   }

   public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
      return !this.readOnly && !this.readOnlyInsert.getAsBoolean() ? this.inventory.insertItem(slot, stack, this.side, Action.get(!simulate)) : stack;
   }

   public ItemStack extractItem(int slot, int amount, boolean simulate) {
      return !this.readOnly && !this.readOnlyExtract.getAsBoolean()
         ? this.inventory.extractItem(slot, amount, this.side, Action.get(!simulate))
         : ItemStack.f_41583_;
   }

   public int getSlotLimit(int slot) {
      return this.inventory.getSlotLimit(slot, this.side);
   }

   public boolean isItemValid(int slot, ItemStack stack) {
      return !this.readOnly || this.inventory.isItemValid(slot, stack, this.side);
   }

   public void setStackInSlot(int slot, ItemStack stack) {
      if (!this.readOnly) {
         this.inventory.setStackInSlot(slot, stack, this.side);
      }
   }
}
