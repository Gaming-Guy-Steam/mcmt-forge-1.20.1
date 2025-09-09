package mekanism.common.integration.computer;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import mekanism.api.text.EnumColor;
import mekanism.common.content.filter.BaseFilter;
import mekanism.common.content.filter.FilterType;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.content.oredictionificator.OredictionificatorFilter;
import mekanism.common.content.oredictionificator.OredictionificatorItemFilter;
import mekanism.common.content.qio.filter.QIOFilter;
import mekanism.common.content.qio.filter.QIOItemStackFilter;
import mekanism.common.content.transporter.SorterFilter;
import mekanism.common.content.transporter.SorterItemStackFilter;
import mekanism.common.tile.machine.TileEntityOredictionificator;
import mekanism.common.util.text.InputValidator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpecialConverters {
   @Nullable
   public static <ENUM extends Enum<?>> ENUM sanitizeStringToEnum(Class<? extends ENUM> expectedType, String argument) {
      if (!argument.isEmpty()) {
         ENUM[] enumConstants = (ENUM[])expectedType.getEnumConstants();

         for (ENUM enumConstant : enumConstants) {
            if (argument.equalsIgnoreCase(enumConstant.name())) {
               return enumConstant;
            }
         }
      }

      return null;
   }

   private static ItemStack tryCreateFilterItem(@Nullable String rawName, @Nullable String rawNBT) throws ComputerException {
      Item item = tryCreateItem(rawName);
      if (item == Items.f_41852_) {
         return ItemStack.f_41583_;
      } else {
         ItemStack stack = new ItemStack(item);
         if (rawNBT != null) {
            try {
               stack.m_41751_(NbtUtils.m_178024_(rawNBT));
            } catch (CommandSyntaxException var5) {
               throw new ComputerException("Invalid SNBT: " + var5.getMessage());
            }
         }

         return stack;
      }
   }

   private static Item tryCreateItem(@Nullable Object rawName) {
      if (rawName instanceof String name) {
         ResourceLocation itemName = ResourceLocation.m_135820_(name);
         if (itemName != null) {
            Item item = (Item)ForgeRegistries.ITEMS.getValue(itemName);
            if (item != null) {
               return item;
            }
         }
      }

      return Items.f_41852_;
   }

   @Nullable
   private static String tryGetFilterTag(@Nullable Object rawTag) {
      if (rawTag instanceof String tag && !tag.isEmpty()) {
         String var2 = tag.toLowerCase(Locale.ROOT);
         if (InputValidator.test(var2, InputValidator.RESOURCE_LOCATION.or(InputValidator.WILDCARD_CHARS))) {
            return var2;
         }
      }

      return null;
   }

   @Nullable
   private static String tryGetFilterModId(@Nullable Object rawModId) {
      if (rawModId instanceof String modId && !modId.isEmpty()) {
         String var2 = modId.toLowerCase(Locale.ROOT);
         if (InputValidator.test(var2, InputValidator.RL_NAMESPACE.or(InputValidator.WILDCARD_CHARS))) {
            return var2;
         }
      }

      return null;
   }

   private static boolean getBooleanFromRaw(@Nullable Object raw) {
      return raw instanceof Boolean bool ? bool : false;
   }

   static int getIntFromRaw(@Nullable Object raw) {
      return raw instanceof Number number ? number.intValue() : 0;
   }

   @NotNull
   public static <FILTER extends IFilter<FILTER>> FILTER convertMapToFilter(@NotNull Class<FILTER> expectedType, @NotNull Map<?, ?> map) throws ComputerException {
      if (map.get("type") instanceof String string) {
         FilterType filterType = sanitizeStringToEnum(FilterType.class, string);
         if (filterType == null) {
            throw new ComputerException("Unknown 'type' value");
         } else {
            IFilter<?> filter = BaseFilter.fromType(filterType);
            if (!expectedType.isInstance(filter)) {
               throw new ComputerException("Type is not of an expected format");
            } else {
               if (map.get("enabled") instanceof Boolean enable) {
                  filter.setEnabled(enable);
               }

               if (filter instanceof IItemStackFilter<?> itemFilter) {
                  decodeItemStackFilter(map, itemFilter);
               } else if (filter instanceof IModIDFilter<?> modIDFilter) {
                  decodeModIdFilter(map, modIDFilter);
               } else if (filter instanceof ITagFilter<?> tagFilter) {
                  decodeTagFilter(map, tagFilter);
               }

               if (filter instanceof MinerFilter<?> minerFilter) {
                  decodeMinerFilter(map, minerFilter);
               } else if (filter instanceof SorterFilter<?> sorterFilter) {
                  decodeSorterFilter(map, sorterFilter);
               } else if (filter instanceof QIOFilter<?> qioFilter) {
                  decodeQioFilter(map, qioFilter);
               } else if (filter instanceof OredictionificatorFilter<?, ?, ?> oredictionificatorFilter) {
                  decodeOreDictFilter(map, oredictionificatorFilter);
               }

               return expectedType.cast(filter);
            }
         }
      } else {
         throw new ComputerException("Missing 'type' element");
      }
   }

   private static void decodeOreDictFilter(@NotNull Map<?, ?> map, OredictionificatorFilter<?, ?, ?> oredictionificatorFilter) throws ComputerException {
      if (map.get("target") instanceof String tag && !tag.isEmpty()) {
         ResourceLocation rl = ResourceLocation.m_135820_(tag);
         if (rl != null && TileEntityOredictionificator.isValidTarget(rl)) {
            oredictionificatorFilter.setFilter(rl);
            if (oredictionificatorFilter instanceof OredictionificatorItemFilter itemFilter) {
               Item item = tryCreateItem(map.get("selected"));
               if (item != Items.f_41852_) {
                  itemFilter.setSelectedOutput(item);
               }
            }
         } else {
            throw new ComputerException("Invalid 'target'");
         }
      } else {
         throw new ComputerException("Missing 'target'");
      }
   }

   private static void decodeQioFilter(@NotNull Map<?, ?> map, QIOFilter<?> qioFilter) {
      if (qioFilter instanceof QIOItemStackFilter qioItemFilter) {
         qioItemFilter.fuzzyMode = getBooleanFromRaw(map.get("fuzzy"));
      }
   }

   private static void decodeSorterFilter(@NotNull Map<?, ?> map, SorterFilter<?> sorterFilter) throws ComputerException {
      sorterFilter.allowDefault = getBooleanFromRaw(map.get("allowDefault"));
      Object rawColor = map.get("color");
      if (rawColor instanceof String) {
         sorterFilter.color = sanitizeStringToEnum(EnumColor.class, (String)rawColor);
      }

      sorterFilter.sizeMode = getBooleanFromRaw(map.get("size"));
      sorterFilter.min = getIntFromRaw(map.get("min"));
      sorterFilter.max = getIntFromRaw(map.get("max"));
      if (sorterFilter.min >= 0 && sorterFilter.max >= 0 && sorterFilter.min <= sorterFilter.max && sorterFilter.max <= 64) {
         if (sorterFilter instanceof SorterItemStackFilter sorterItemFilter) {
            sorterItemFilter.fuzzyMode = getBooleanFromRaw(map.get("fuzzy"));
         }
      } else {
         throw new ComputerException("Invalid or min/max: 0 <= min <= max <= 64");
      }
   }

   private static void decodeMinerFilter(@NotNull Map<?, ?> map, MinerFilter<?> minerFilter) {
      minerFilter.requiresReplacement = getBooleanFromRaw(map.get("requiresReplacement"));
      minerFilter.replaceTarget = tryCreateItem(map.get("replaceTarget"));
   }

   private static void decodeTagFilter(@NotNull Map<?, ?> map, ITagFilter<?> tagFilter) throws ComputerException {
      String tag = tryGetFilterTag(map.get("tag"));
      if (tag == null) {
         throw new ComputerException("Invalid or missing tag specified for Tag filter");
      } else {
         tagFilter.setTagName(tag);
      }
   }

   private static void decodeModIdFilter(@NotNull Map<?, ?> map, IModIDFilter<?> modIDFilter) throws ComputerException {
      String modId = tryGetFilterModId(map.get("modId"));
      if (modId == null) {
         throw new ComputerException("Invalid or missing modId specified for Mod Id filter");
      } else {
         modIDFilter.setModID(modId);
      }
   }

   private static void decodeItemStackFilter(@NotNull Map<?, ?> map, IItemStackFilter<?> itemFilter) throws ComputerException {
      ItemStack stack = tryCreateFilterItem((String)map.get("item"), (String)map.get("itemNBT"));
      if (stack.m_41619_()) {
         throw new ComputerException("Invalid or missing item specified for ItemStack filter");
      } else {
         itemFilter.setItemStack(stack);
      }
   }

   static Map<String, Object> wrapStack(ResourceLocation name, String sizeKey, int amount, @Nullable CompoundTag tag) {
      boolean hasTag = tag != null && !tag.m_128456_() && amount > 0;
      Map<String, Object> wrapped = new HashMap<>(hasTag ? 3 : 2);
      wrapped.put("name", name == null ? "unknown" : name.toString());
      wrapped.put(sizeKey, amount);
      if (hasTag) {
         wrapped.put("nbt", wrapNBT(tag));
      }

      return wrapped;
   }

   static String wrapNBT(@NotNull CompoundTag nbt) {
      return NbtUtils.m_178063_(nbt);
   }
}
