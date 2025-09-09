package mekanism.api.inventory;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public interface IInventorySlot extends INBTSerializable<CompoundTag>, IContentsListener {
   ItemStack getStack();

   void setStack(ItemStack var1);

   default ItemStack insertItem(ItemStack stack, Action action, AutomationType automationType) {
      if (!stack.m_41619_() && this.isItemValid(stack)) {
         int needed = this.getLimit(stack) - this.getCount();
         if (needed <= 0) {
            return stack;
         } else {
            boolean sameType = false;
            if (!this.isEmpty() && !(sameType = ItemHandlerHelper.canItemStacksStack(this.getStack(), stack))) {
               return stack;
            } else {
               int toAdd = Math.min(stack.m_41613_(), needed);
               if (action.execute()) {
                  if (sameType) {
                     this.growStack(toAdd, action);
                  } else {
                     this.setStack(stack.m_255036_(toAdd));
                  }
               }

               return stack.m_255036_(stack.m_41613_() - toAdd);
            }
         }
      } else {
         return stack;
      }
   }

   default ItemStack extractItem(int amount, Action action, AutomationType automationType) {
      if (!this.isEmpty() && amount >= 1) {
         ItemStack current = this.getStack();
         int currentAmount = Math.min(this.getCount(), current.m_41741_());
         if (currentAmount < amount) {
            amount = currentAmount;
         }

         ItemStack toReturn = current.m_255036_(amount);
         if (action.execute()) {
            this.shrinkStack(amount, action);
         }

         return toReturn;
      } else {
         return ItemStack.f_41583_;
      }
   }

   int getLimit(ItemStack var1);

   boolean isItemValid(ItemStack var1);

   @Nullable
   Slot createContainerSlot();

   default int setStackSize(int amount, Action action) {
      if (this.isEmpty()) {
         return 0;
      } else if (amount <= 0) {
         if (action.execute()) {
            this.setEmpty();
         }

         return 0;
      } else {
         ItemStack stack = this.getStack();
         int maxStackSize = this.getLimit(stack);
         if (amount > maxStackSize) {
            amount = maxStackSize;
         }

         if (stack.m_41613_() != amount && !action.simulate()) {
            this.setStack(stack.m_255036_(amount));
            return amount;
         } else {
            return amount;
         }
      }
   }

   default int growStack(int amount, Action action) {
      int current = this.getCount();
      if (amount > 0) {
         amount = Math.min(amount, this.getLimit(this.getStack()));
      }

      int newSize = this.setStackSize(current + amount, action);
      return newSize - current;
   }

   default int shrinkStack(int amount, Action action) {
      return -this.growStack(-amount, action);
   }

   default boolean isEmpty() {
      return this.getStack().m_41619_();
   }

   default void setEmpty() {
      this.setStack(ItemStack.f_41583_);
   }

   default int getCount() {
      return this.getStack().m_41613_();
   }
}
