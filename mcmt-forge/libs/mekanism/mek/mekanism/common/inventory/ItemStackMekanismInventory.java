package mekanism.common.inventory;

import java.util.List;
import mekanism.api.DataHandlerUtils;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ItemStackMekanismInventory implements IMekanismInventory {
   private final List<IInventorySlot> slots;
   @NotNull
   protected final ItemStack stack;

   protected ItemStackMekanismInventory(@NotNull ItemStack stack) {
      this.stack = stack;
      this.slots = this.getInitialInventory();
      if (!stack.m_41619_() && stack.m_41720_() instanceof IItemSustainedInventory sustainedInventory) {
         DataHandlerUtils.readContainers(this.getInventorySlots(null), sustainedInventory.getSustainedInventory(stack));
      }
   }

   protected abstract List<IInventorySlot> getInitialInventory();

   @NotNull
   @Override
   public List<IInventorySlot> getInventorySlots(@Nullable Direction side) {
      return this.slots;
   }

   @Override
   public void onContentsChanged() {
      if (!this.stack.m_41619_() && this.stack.m_41720_() instanceof IItemSustainedInventory sustainedInventory) {
         sustainedInventory.setSustainedInventory(DataHandlerUtils.writeContainers(this.getInventorySlots(null)), this.stack);
      }
   }
}
