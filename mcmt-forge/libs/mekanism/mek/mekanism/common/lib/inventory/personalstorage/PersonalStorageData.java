package mekanism.common.lib.inventory.personalstorage;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.Mekanism;
import mekanism.common.lib.MekanismSavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
class PersonalStorageData extends MekanismSavedData {
   private final Map<UUID, PersonalStorageItemInventory> inventoriesById = new HashMap<>();

   PersonalStorageItemInventory getOrAddInventory(UUID id) {
      return this.inventoriesById.computeIfAbsent(id, unused -> this.createInventory());
   }

   PersonalStorageItemInventory addInventory(UUID id, List<IInventorySlot> contents) {
      return this.inventoriesById.computeIfAbsent(id, unused -> {
         PersonalStorageItemInventory inventory = this.createInventory();
         List<IInventorySlot> inventorySlots = inventory.getInventorySlots(null);
         int i = 0;

         for (int slots = contents.size(); i < slots; i++) {
            inventorySlots.get(i).deserializeNBT((CompoundTag)contents.get(i).serializeNBT());
         }

         this.m_77762_();
         return inventory;
      });
   }

   void removeInventory(UUID id) {
      if (this.inventoriesById.remove(id) != null) {
         this.m_77762_();
      }
   }

   @NotNull
   private PersonalStorageItemInventory createInventory() {
      return new PersonalStorageItemInventory(this::m_77762_);
   }

   @Override
   public void load(@NotNull CompoundTag nbt) {
      ListTag entries = nbt.m_128437_("data", 10);

      for (int i = 0; i < entries.size(); i++) {
         CompoundTag entry = entries.m_128728_(i);
         PersonalStorageItemInventory inv = this.createInventory();
         inv.deserializeNBT(entry.m_128437_("Items", 10));
         this.inventoriesById.put(entry.m_128342_("personalStorageId"), inv);
      }
   }

   public CompoundTag m_7176_(CompoundTag compoundTag) {
      ListTag entries = new ListTag();
      this.inventoriesById.forEach((uuid, inv) -> {
         CompoundTag nbtEntry = new CompoundTag();
         nbtEntry.m_128362_("personalStorageId", uuid);
         nbtEntry.m_128365_("Items", inv.serializeNBT());
         entries.add(nbtEntry);
      });
      compoundTag.m_128365_("data", entries);
      return compoundTag;
   }

   @Override
   public void m_77757_(File file) {
      if (this.m_77764_()) {
         File folder = file.getParentFile();
         if (!folder.exists() && !folder.mkdirs()) {
            Mekanism.logger.error("Could not create personal storage directory, saves may fail");
         }
      }

      super.m_77757_(file);
   }
}
