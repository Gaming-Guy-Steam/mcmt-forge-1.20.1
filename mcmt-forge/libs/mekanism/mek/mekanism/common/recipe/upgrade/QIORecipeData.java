package mekanism.common.recipe.upgrade;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import java.util.UUID;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.content.qio.IQIODriveItem;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class QIORecipeData implements RecipeUpgradeData<QIORecipeData> {
   private final Object2LongMap<UUID> itemMap;
   private final long itemCount;

   QIORecipeData(IQIODriveItem.DriveMetadata data, long[] serializedMap) {
      this.itemCount = data.count();
      this.itemMap = new Object2LongOpenHashMap(data.types());

      for (int i = 0; i < serializedMap.length; i++) {
         this.itemMap.put(new UUID(serializedMap[i++], serializedMap[i++]), serializedMap[i]);
      }
   }

   private QIORecipeData(Object2LongMap<UUID> itemMap, long itemCount) {
      this.itemMap = itemMap;
      this.itemCount = itemCount;
   }

   @Nullable
   public QIORecipeData merge(QIORecipeData other) {
      if (this.itemCount > Long.MAX_VALUE - other.itemCount) {
         return null;
      } else {
         Object2LongMap<UUID> smallerMap = other.itemMap;
         Object2LongMap<UUID> largerMap = this.itemMap;
         if (largerMap.size() < smallerMap.size()) {
            smallerMap = this.itemMap;
            largerMap = other.itemMap;
         }

         Object2LongMap<UUID> fullItemMap = new Object2LongOpenHashMap(largerMap);
         ObjectIterator var5 = smallerMap.object2LongEntrySet().iterator();

         while (var5.hasNext()) {
            Entry<UUID> entry = (Entry<UUID>)var5.next();
            fullItemMap.mergeLong((UUID)entry.getKey(), entry.getLongValue(), Long::sum);
         }

         return new QIORecipeData(fullItemMap, this.itemCount + other.itemCount);
      }
   }

   @Override
   public boolean applyToStack(ItemStack stack) {
      if (this.itemMap.isEmpty()) {
         return this.itemCount == 0L;
      } else {
         IQIODriveItem driveItem = (IQIODriveItem)stack.m_41720_();
         if (this.itemCount != 0L && this.itemCount <= driveItem.getCountCapacity(stack) && this.itemMap.size() <= driveItem.getTypeCapacity(stack)) {
            int i = 0;
            long[] serializedMap = new long[3 * this.itemMap.size()];
            ObjectIterator meta = this.itemMap.object2LongEntrySet().iterator();

            while (meta.hasNext()) {
               Entry<UUID> entry = (Entry<UUID>)meta.next();
               UUID uuid = (UUID)entry.getKey();
               serializedMap[i++] = uuid.getMostSignificantBits();
               serializedMap[i++] = uuid.getLeastSignificantBits();
               serializedMap[i++] = entry.getLongValue();
            }

            ItemDataUtils.setLongArrayOrRemove(stack, "qioItemMap", serializedMap);
            IQIODriveItem.DriveMetadata metax = new IQIODriveItem.DriveMetadata(this.itemCount, this.itemMap.size());
            metax.write(stack);
            return true;
         } else {
            return false;
         }
      }
   }
}
