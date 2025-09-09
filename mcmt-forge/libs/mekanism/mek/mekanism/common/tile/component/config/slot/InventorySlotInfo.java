package mekanism.common.tile.component.config.slot;

import java.util.List;
import mekanism.api.inventory.IInventorySlot;

public class InventorySlotInfo extends BaseSlotInfo {
   private final List<IInventorySlot> inventorySlots;

   public InventorySlotInfo(boolean canInput, boolean canOutput, IInventorySlot... slots) {
      this(canInput, canOutput, List.of(slots));
   }

   public InventorySlotInfo(boolean canInput, boolean canOutput, List<IInventorySlot> slots) {
      super(canInput, canOutput);
      this.inventorySlots = slots;
   }

   public List<IInventorySlot> getSlots() {
      return this.inventorySlots;
   }

   public boolean hasSlot(IInventorySlot slot) {
      return this.getSlots().contains(slot);
   }
}
