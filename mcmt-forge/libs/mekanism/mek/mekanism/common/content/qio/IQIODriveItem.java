package mekanism.common.content.qio;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import java.util.UUID;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.world.item.ItemStack;

public interface IQIODriveItem {
   default boolean hasStoredItemMap(ItemStack stack) {
      return ItemDataUtils.hasData(stack, "qioItemMap", 12);
   }

   default void loadItemMap(ItemStack stack, QIODriveData data) {
      if (this.hasStoredItemMap(stack)) {
         Object2LongMap<HashedItem> itemMap = data.getItemMap();
         long[] array = ItemDataUtils.getLongArray(stack, "qioItemMap");
         if (array.length % 3 == 0) {
            for (int i = 0; i < array.length; i++) {
               UUID uuid = new UUID(array[i++], array[i++]);
               HashedItem type = QIOGlobalItemLookup.INSTANCE.getTypeByUUID(uuid);
               if (type != null) {
                  itemMap.put(type, array[i]);
               }
            }
         }
      }
   }

   default void writeItemMap(ItemStack stack, QIODriveData map) {
      int i = 0;
      Object2LongMap<HashedItem> itemMap = map.getItemMap();
      long[] serializedMap = new long[3 * itemMap.size()];
      ObjectIterator var6 = itemMap.object2LongEntrySet().iterator();

      while (var6.hasNext()) {
         Entry<HashedItem> entry = (Entry<HashedItem>)var6.next();
         UUID uuid = QIOGlobalItemLookup.INSTANCE.getOrTrackUUID((HashedItem)entry.getKey());
         serializedMap[i++] = uuid.getMostSignificantBits();
         serializedMap[i++] = uuid.getLeastSignificantBits();
         serializedMap[i++] = entry.getLongValue();
      }

      ItemDataUtils.setLongArrayOrRemove(stack, "qioItemMap", serializedMap);
   }

   long getCountCapacity(ItemStack stack);

   int getTypeCapacity(ItemStack stack);

   public record DriveMetadata(long count, int types) {
      public void write(ItemStack stack) {
         ItemDataUtils.setLongOrRemove(stack, "qioMetaCount", this.count);
         ItemDataUtils.setIntOrRemove(stack, "qioMetaTypes", this.types);
      }

      public static IQIODriveItem.DriveMetadata load(ItemStack stack) {
         return new IQIODriveItem.DriveMetadata(ItemDataUtils.getLong(stack, "qioMetaCount"), ItemDataUtils.getInt(stack, "qioMetaTypes"));
      }
   }
}
