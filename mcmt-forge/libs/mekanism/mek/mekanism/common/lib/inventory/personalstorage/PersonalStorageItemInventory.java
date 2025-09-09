package mekanism.common.lib.inventory.personalstorage;

import mekanism.api.DataHandlerUtils;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.nbt.ListTag;
import net.minecraftforge.common.util.INBTSerializable;

@NothingNullByDefault
public class PersonalStorageItemInventory extends AbstractPersonalStorageItemInventory implements INBTSerializable<ListTag> {
   private final IContentsListener parent;

   PersonalStorageItemInventory(IContentsListener parent) {
      this.parent = parent;
   }

   @Override
   public void onContentsChanged() {
      this.parent.onContentsChanged();
   }

   public ListTag serializeNBT() {
      return DataHandlerUtils.writeContainers(this.slots);
   }

   public void deserializeNBT(ListTag nbt) {
      DataHandlerUtils.readContainers(this.slots, nbt);
   }
}
