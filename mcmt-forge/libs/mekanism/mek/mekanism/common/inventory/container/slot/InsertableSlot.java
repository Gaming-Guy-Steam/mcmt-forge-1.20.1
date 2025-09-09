package mekanism.common.inventory.container.slot;

import mekanism.api.Action;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

public class InsertableSlot extends Slot implements IInsertableSlot {
   public InsertableSlot(Container inventory, int index, int x, int y) {
      super(inventory, index, x, y);
   }

   public int m_5866_(ItemStack stack) {
      return Math.min(this.m_6641_(), stack.m_41741_());
   }

   @NotNull
   @Override
   public ItemStack insertItem(@NotNull ItemStack stack, Action action) {
      if (!stack.m_41619_() && this.m_5857_(stack)) {
         ItemStack current = this.m_7993_();
         int needed = this.m_5866_(stack) - current.m_41613_();
         if (needed <= 0) {
            return stack;
         } else if (!current.m_41619_() && !ItemHandlerHelper.canItemStacksStack(current, stack)) {
            return stack;
         } else {
            int toAdd = Math.min(stack.m_41613_(), needed);
            if (action.execute()) {
               this.m_5852_(stack.m_255036_(current.m_41613_() + toAdd));
            }

            return stack.m_255036_(stack.m_41613_() - toAdd);
         }
      } else {
         return stack;
      }
   }
}
