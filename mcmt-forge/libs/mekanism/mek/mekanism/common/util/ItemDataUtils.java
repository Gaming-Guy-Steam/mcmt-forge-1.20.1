package mekanism.common.util;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import mekanism.api.DataHandlerUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ItemDataUtils {
   private ItemDataUtils() {
   }

   @NotNull
   public static CompoundTag getDataMap(ItemStack stack) {
      CompoundTag tag = stack.m_41784_();
      if (tag.m_128425_("mekData", 10)) {
         return tag.m_128469_("mekData");
      } else {
         CompoundTag dataMap = new CompoundTag();
         tag.m_128365_("mekData", dataMap);
         return dataMap;
      }
   }

   @Nullable
   public static CompoundTag getDataMapIfPresent(ItemStack stack) {
      CompoundTag tag = stack.m_41783_();
      return tag != null && tag.m_128425_("mekData", 10) ? tag.m_128469_("mekData") : null;
   }

   public static boolean hasData(ItemStack stack, String key, int type) {
      CompoundTag dataMap = getDataMapIfPresent(stack);
      return dataMap != null && dataMap.m_128425_(key, type);
   }

   public static void removeData(ItemStack stack, String key) {
      CompoundTag dataMap = getDataMapIfPresent(stack);
      if (dataMap != null) {
         dataMap.m_128473_(key);
         if (dataMap.m_128456_()) {
            stack.m_41749_("mekData");
         }
      }
   }

   public static <T> T getDataValue(ItemStack stack, Function<CompoundTag, T> getter, T fallback) {
      CompoundTag dataMap = getDataMapIfPresent(stack);
      return dataMap == null ? fallback : getter.apply(dataMap);
   }

   public static int getInt(ItemStack stack, String key) {
      CompoundTag dataMap = getDataMapIfPresent(stack);
      return dataMap == null ? 0 : dataMap.m_128451_(key);
   }

   public static long getLong(ItemStack stack, String key) {
      CompoundTag dataMap = getDataMapIfPresent(stack);
      return dataMap == null ? 0L : dataMap.m_128454_(key);
   }

   public static boolean getBoolean(ItemStack stack, String key) {
      CompoundTag dataMap = getDataMapIfPresent(stack);
      return dataMap != null && dataMap.m_128471_(key);
   }

   public static double getDouble(ItemStack stack, String key) {
      CompoundTag dataMap = getDataMapIfPresent(stack);
      return dataMap == null ? 0.0 : dataMap.m_128459_(key);
   }

   public static String getString(ItemStack stack, String key) {
      return getDataValue(stack, dataMap -> dataMap.m_128461_(key), "");
   }

   public static CompoundTag getCompound(ItemStack stack, String key) {
      return getDataValue(stack, dataMap -> dataMap.m_128469_(key), new CompoundTag());
   }

   public static CompoundTag getOrAddCompound(ItemStack stack, String key) {
      CompoundTag dataMap = getDataMap(stack);
      if (dataMap.m_128425_(key, 10)) {
         return dataMap.m_128469_(key);
      } else {
         CompoundTag compound = new CompoundTag();
         dataMap.m_128365_(key, compound);
         return compound;
      }
   }

   public static void setCompoundIfPresent(ItemStack stack, String key, Consumer<CompoundTag> setter) {
      CompoundTag dataMap = getDataMapIfPresent(stack);
      if (dataMap != null && dataMap.m_128425_(key, 10)) {
         setter.accept(dataMap.m_128469_(key));
      }
   }

   @Nullable
   public static UUID getUniqueID(ItemStack stack, String key) {
      CompoundTag dataMap = getDataMapIfPresent(stack);
      return dataMap != null && dataMap.m_128403_(key) ? dataMap.m_128342_(key) : null;
   }

   public static ListTag getList(ItemStack stack, String key) {
      return getDataValue(stack, dataMap -> dataMap.m_128437_(key, 10), new ListTag());
   }

   public static void setInt(ItemStack stack, String key, int i) {
      getDataMap(stack).m_128405_(key, i);
   }

   public static void setIntOrRemove(ItemStack stack, String key, int i) {
      if (i == 0) {
         removeData(stack, key);
      } else {
         setInt(stack, key, i);
      }
   }

   public static void setLong(ItemStack stack, String key, long l) {
      getDataMap(stack).m_128356_(key, l);
   }

   public static void setLongOrRemove(ItemStack stack, String key, long l) {
      if (l == 0L) {
         removeData(stack, key);
      } else {
         setLong(stack, key, l);
      }
   }

   public static void setBoolean(ItemStack stack, String key, boolean b) {
      getDataMap(stack).m_128379_(key, b);
   }

   public static void setDouble(ItemStack stack, String key, double d) {
      getDataMap(stack).m_128347_(key, d);
   }

   public static void setString(ItemStack stack, String key, String s) {
      getDataMap(stack).m_128359_(key, s);
   }

   public static void setCompound(ItemStack stack, String key, CompoundTag tag) {
      getDataMap(stack).m_128365_(key, tag);
   }

   public static void setUUID(ItemStack stack, String key, @Nullable UUID uuid) {
      if (uuid == null) {
         removeData(stack, key);
      } else {
         getDataMap(stack).m_128362_(key, uuid);
      }
   }

   public static void setList(ItemStack stack, String key, ListTag tag) {
      getDataMap(stack).m_128365_(key, tag);
   }

   public static void setListOrRemove(ItemStack stack, String key, ListTag tag) {
      if (tag.isEmpty()) {
         removeData(stack, key);
      } else {
         setList(stack, key, tag);
      }
   }

   public static long[] getLongArray(ItemStack stack, String key) {
      return getDataValue(stack, dataMap -> dataMap.m_128467_(key), new long[0]);
   }

   public static void setLongArrayOrRemove(ItemStack stack, String key, long[] array) {
      if (array.length == 0) {
         removeData(stack, key);
      } else {
         getDataMap(stack).m_128388_(key, array);
      }
   }

   public static void readContainers(ItemStack stack, String containerKey, List<? extends INBTSerializable<CompoundTag>> containers) {
      if (!stack.m_41619_()) {
         DataHandlerUtils.readContainers(containers, getList(stack, containerKey));
      }
   }

   public static void writeContainers(ItemStack stack, String containerKey, List<? extends INBTSerializable<CompoundTag>> containers) {
      if (!stack.m_41619_()) {
         setListOrRemove(stack, containerKey, DataHandlerUtils.writeContainers(containers));
      }
   }
}
