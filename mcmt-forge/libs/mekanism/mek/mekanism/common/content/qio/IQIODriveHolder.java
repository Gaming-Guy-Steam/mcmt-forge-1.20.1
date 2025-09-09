package mekanism.common.content.qio;

import java.util.List;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.Mekanism;
import net.minecraft.world.item.ItemStack;

public interface IQIODriveHolder extends IQIOFrequencyHolder {
   List<IInventorySlot> getDriveSlots();

   void onDataUpdate();

   default void save(int slot, QIODriveData data) {
      ItemStack stack = this.getDriveSlots().get(slot).getStack();
      if (stack.m_41720_() instanceof IQIODriveItem item) {
         item.writeItemMap(stack, data);
      } else {
         Mekanism.logger.error("Tried to save data map to an invalid item. Something has gone very wrong!");
      }
   }
}
