package mekanism.common.item.interfaces;

import java.util.List;
import mekanism.api.inventory.IInventorySlot;
import net.minecraft.world.item.ItemStack;

public interface IDroppableContents {
   default boolean canContentsDrop(ItemStack stack) {
      return true;
   }

   List<IInventorySlot> getDroppedSlots(ItemStack stack);
}
