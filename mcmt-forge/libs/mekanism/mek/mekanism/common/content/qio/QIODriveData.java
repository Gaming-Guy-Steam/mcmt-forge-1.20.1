package mekanism.common.content.qio;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import mekanism.api.Action;
import mekanism.common.Mekanism;
import mekanism.common.lib.inventory.HashedItem;
import net.minecraft.world.item.ItemStack;

public class QIODriveData {
   private final QIODriveData.QIODriveKey key;
   private final long countCapacity;
   private final int typeCapacity;
   private final Object2LongMap<HashedItem> itemMap = new Object2LongOpenHashMap();
   private long itemCount;

   public QIODriveData(QIODriveData.QIODriveKey key) {
      this.key = key;
      ItemStack stack = key.getDriveStack();
      IQIODriveItem item = (IQIODriveItem)stack.m_41720_();
      this.countCapacity = item.getCountCapacity(stack);
      this.typeCapacity = item.getTypeCapacity(stack);
      item.loadItemMap(stack, this);
      this.itemCount = this.itemMap.values().longStream().sum();
      key.updateMetadata(this);
   }

   public long add(HashedItem type, long amount, Action action) {
      long stored = this.getStored(type);
      if (this.itemCount != this.countCapacity && (stored != 0L || this.itemMap.size() != this.typeCapacity)) {
         long toAdd = Math.min(amount, this.countCapacity - this.itemCount);
         if (action.execute()) {
            this.itemMap.put(type, stored + toAdd);
            this.itemCount += toAdd;
            this.key.updateMetadata(this);
            this.key.dataUpdate();
         }

         return amount - toAdd;
      } else {
         return amount;
      }
   }

   public long remove(HashedItem type, long amount, Action action) {
      long stored = this.getStored(type);
      long removed = Math.min(amount, stored);
      if (action.execute()) {
         long remaining = stored - removed;
         if (remaining > 0L) {
            this.itemMap.put(type, remaining);
         } else {
            this.itemMap.removeLong(type);
         }

         this.itemCount -= removed;
         this.key.updateMetadata(this);
         this.key.dataUpdate();
      }

      return removed;
   }

   public long getStored(HashedItem type) {
      return this.itemMap.getOrDefault(type, 0L);
   }

   public Object2LongMap<HashedItem> getItemMap() {
      return this.itemMap;
   }

   public QIODriveData.QIODriveKey getKey() {
      return this.key;
   }

   public long getCountCapacity() {
      return this.countCapacity;
   }

   public int getTypeCapacity() {
      return this.typeCapacity;
   }

   public long getTotalCount() {
      return this.itemCount;
   }

   public int getTotalTypes() {
      return this.itemMap.size();
   }

   public record QIODriveKey(IQIODriveHolder holder, int driveSlot) {
      public void save(QIODriveData data) {
         this.holder.save(this.driveSlot, data);
      }

      public void dataUpdate() {
         this.holder.onDataUpdate();
      }

      public void updateMetadata(QIODriveData data) {
         ItemStack stack = this.getDriveStack();
         if (stack.m_41720_() instanceof IQIODriveItem) {
            IQIODriveItem.DriveMetadata meta = new IQIODriveItem.DriveMetadata(data.itemCount, data.itemMap.size());
            meta.write(stack);
         } else {
            Mekanism.logger.error("Tried to update QIO meta values on an invalid ItemStack. Something has gone very wrong!");
         }
      }

      public ItemStack getDriveStack() {
         return this.holder.getDriveSlots().get(this.driveSlot).getStack();
      }
   }
}
