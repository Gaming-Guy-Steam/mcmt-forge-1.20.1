package mekanism.common.inventory.container.slot;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.InventoryMenu;

public class OffhandSlot extends InsertableSlot {
   public OffhandSlot(Container inventory, int index, int x, int y) {
      super(inventory, index, x, y);
      this.setBackground(InventoryMenu.f_39692_, InventoryMenu.f_39697_);
   }
}
