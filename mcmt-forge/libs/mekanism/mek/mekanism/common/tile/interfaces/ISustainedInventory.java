package mekanism.common.tile.interfaces;

import net.minecraft.nbt.ListTag;

public interface ISustainedInventory {
   void setSustainedInventory(ListTag nbtTags);

   ListTag getSustainedInventory();

   default boolean hasSustainedInventory() {
      ListTag inventory = this.getSustainedInventory();
      return inventory != null && !inventory.isEmpty();
   }
}
