package mekanism.common.integration.computer;

import com.google.common.collect.UnmodifiableIterator;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import mekanism.api.Coord4D;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.EnumColor;
import mekanism.common.content.filter.FilterType;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.content.miner.MinerItemStackFilter;
import mekanism.common.content.miner.MinerModIDFilter;
import mekanism.common.content.miner.MinerTagFilter;
import mekanism.common.content.oredictionificator.OredictionificatorFilter;
import mekanism.common.content.oredictionificator.OredictionificatorItemFilter;
import mekanism.common.content.qio.filter.QIOFilter;
import mekanism.common.content.qio.filter.QIOItemStackFilter;
import mekanism.common.content.qio.filter.QIOModIDFilter;
import mekanism.common.content.qio.filter.QIOTagFilter;
import mekanism.common.content.transporter.SorterFilter;
import mekanism.common.content.transporter.SorterItemStackFilter;
import mekanism.common.content.transporter.SorterModIDFilter;
import mekanism.common.content.transporter.SorterTagFilter;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.util.RegistryUtils;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseComputerHelper {
   public static final Lazy<Map<Class<?>, TableType>> BUILTIN_TABLES = Lazy.of(BaseComputerHelper::getBuiltInTables);

   @NotNull
   private <T> T requireNonNull(int param, @Nullable T value) throws ComputerException {
      return value;
   }

   @NotNull
   public <T extends Enum<T>> T getEnum(int param, Class<T> enumClazz) throws ComputerException {
      return this.requireNonNull(param, SpecialConverters.sanitizeStringToEnum(enumClazz, this.getString(param)));
   }

   public abstract boolean getBoolean(int param) throws ComputerException;

   public abstract byte getByte(int param) throws ComputerException;

   public abstract short getShort(int param) throws ComputerException;

   public abstract int getInt(int param) throws ComputerException;

   public abstract long getLong(int param) throws ComputerException;

   public abstract char getChar(int param) throws ComputerException;

   public abstract float getFloat(int param) throws ComputerException;

   public abstract double getDouble(int param) throws ComputerException;

   public FloatingLong getFloatingLong(int param) throws ComputerException {
      double finiteDouble = this.getDouble(param);
      return finiteDouble < 0.0 ? FloatingLong.ZERO : FloatingLong.createConst(finiteDouble);
   }

   @NotNull
   public abstract String getString(int param) throws ComputerException;

   @NotNull
   public abstract Map<?, ?> getMap(int param) throws ComputerException;

   @Nullable
   public <FILTER extends IFilter<FILTER>> FILTER getFilter(int param, Class<FILTER> expectedType) throws ComputerException {
      return SpecialConverters.convertMapToFilter(expectedType, this.getMap(param));
   }

   @NotNull
   public ResourceLocation getResourceLocation(int param) throws ComputerException {
      return this.requireNonNull(param, ResourceLocation.m_135820_(this.getString(param)));
   }

   public Item getItem(int param) throws ComputerException {
      ResourceLocation itemName = this.getResourceLocation(param);
      return getItemFromResourceLocation(itemName);
   }

   @NotNull
   private static Item getItemFromResourceLocation(ResourceLocation itemName) {
      if (itemName == null) {
         return Items.f_41852_;
      } else {
         Item item = (Item)ForgeRegistries.ITEMS.getValue(itemName);
         return item != null ? item : Items.f_41852_;
      }
   }

   public ItemStack getItemStack(int param) throws ComputerException {
      Map<?, ?> map = this.getMap(param);

      try {
         Item item = getItemFromResourceLocation(ResourceLocation.m_135820_((String)map.get("name")));
         int count = SpecialConverters.getIntFromRaw(map.get("count"));
         String nbt = (String)map.get("nbt");
         ItemStack stack = new ItemStack(item, count);
         if (nbt != null) {
            stack.m_41751_(NbtUtils.m_178024_(nbt));
         }

         return stack;
      } catch (ClassCastException var7) {
         throw new ComputerException("Invalid ItemStack at index " + param);
      } catch (CommandSyntaxException var8) {
         throw new ComputerException("Invalid NBT data");
      }
   }

   public Object voidResult() {
      return null;
   }

   public Object convert(@Nullable FloatingLong result) {
      if (result == null) {
         return 0;
      } else {
         return result.getDecimal() == 0 && result.getValue() >= 0L ? result.longValue() : result.doubleValue();
      }
   }

   public Object convert(int i) {
      return i;
   }

   public Object convert(long i) {
      return i;
   }

   public Object convert(double d) {
      return d;
   }

   public Object convert(String s) {
      return s;
   }

   public Object convert(boolean b) {
      return b;
   }

   public <T> Object convert(@Nullable Collection<T> list, @NotNull Function<T, Object> converter) {
      if (list == null) {
         return Collections.emptyList();
      } else {
         List<Object> converted = new ArrayList<>(list.size());

         for (T el : list) {
            converted.add(converter.apply(el));
         }

         return converted;
      }
   }

   public Object convert(@Nullable ResourceLocation rl) {
      return rl != null ? rl.toString() : null;
   }

   public Object convert(@Nullable UUID uuid) {
      return uuid != null ? uuid.toString() : null;
   }

   public Object convert(@Nullable ChemicalStack<?> stack) {
      if (stack == null) {
         return null;
      } else {
         Map<String, Object> wrapped = new HashMap<>(2);
         wrapped.put("name", this.convert(stack.getTypeRegistryName()));
         wrapped.put("amount", stack.getAmount());
         return wrapped;
      }
   }

   public Object convert(@Nullable FluidStack stack) {
      return stack == null ? null : SpecialConverters.wrapStack(RegistryUtils.getName(stack.getFluid()), "amount", stack.getAmount(), stack.getTag());
   }

   public Object convert(@Nullable ItemStack stack) {
      return stack == null ? null : SpecialConverters.wrapStack(RegistryUtils.getName(stack.m_41720_()), "count", stack.m_41613_(), stack.m_41783_());
   }

   public Object convert(@Nullable BlockState state) {
      if (state == null) {
         return null;
      } else {
         Map<String, Object> wrapped = new HashMap<>(2);
         ResourceLocation name = RegistryUtils.getName(state.m_60734_());
         if (name != null) {
            wrapped.put("block", this.convert(name));
         }

         Map<String, Object> stateData = new HashMap<>();
         UnmodifiableIterator var5 = state.m_61148_().entrySet().iterator();

         while (var5.hasNext()) {
            Entry<Property<?>, Comparable<?>> entry = (Entry<Property<?>, Comparable<?>>)var5.next();
            Property<?> property = entry.getKey();
            Object value = entry.getValue();
            if (!(property instanceof IntegerProperty) && !(property instanceof BooleanProperty)) {
               value = Util.m_137453_(property, value);
            }

            stateData.put(property.m_61708_(), value);
         }

         if (!stateData.isEmpty()) {
            wrapped.put("state", stateData);
         }

         return wrapped;
      }
   }

   public Object convert(@Nullable Vec3i pos) {
      if (pos == null) {
         return null;
      } else {
         Map<String, Object> wrapped = new HashMap<>(3);
         wrapped.put("x", pos.m_123341_());
         wrapped.put("y", pos.m_123342_());
         wrapped.put("z", pos.m_123343_());
         return wrapped;
      }
   }

   public Object convert(@Nullable Coord4D coord) {
      if (coord == null) {
         return null;
      } else {
         Map<String, Object> wrapped = new HashMap<>(4);
         wrapped.put("x", coord.getX());
         wrapped.put("y", coord.getY());
         wrapped.put("z", coord.getZ());
         wrapped.put("dimension", this.convert(coord.dimension.m_135782_()));
         return wrapped;
      }
   }

   public Object convert(@Nullable Frequency frequency) {
      if (frequency == null) {
         return null;
      } else {
         Frequency.FrequencyIdentity identity = frequency.getIdentity();
         Map<String, Object> wrapped = new HashMap<>(2);
         wrapped.put("key", identity.key().toString());
         wrapped.put("public", identity.isPublic());
         return wrapped;
      }
   }

   public Object convert(@Nullable Enum<?> res) {
      return res != null ? res.name() : null;
   }

   protected Map<String, Object> convertFilterCommon(IFilter<?> result) {
      Map<String, Object> wrapped = new HashMap<>();
      wrapped.put("type", this.convert(result.getFilterType()));
      wrapped.put("enabled", result.isEnabled());
      if (result instanceof IItemStackFilter<?> itemFilter) {
         ItemStack stack = itemFilter.getItemStack();
         wrapped.put("item", this.convert(stack.m_41720_()));
         if (!stack.m_41619_()) {
            CompoundTag tag = stack.m_41783_();
            if (tag != null && !tag.m_128456_()) {
               wrapped.put("itemNBT", SpecialConverters.wrapNBT(tag));
            }
         }
      } else if (result instanceof IModIDFilter<?> modIDFilter) {
         wrapped.put("modId", modIDFilter.getModID());
      } else if (result instanceof ITagFilter<?> tagFilter) {
         wrapped.put("tag", tagFilter.getTagName());
      }

      return wrapped;
   }

   public Object convert(@Nullable MinerFilter<?> minerFilter) {
      if (minerFilter == null) {
         return null;
      } else {
         Map<String, Object> wrapped = this.convertFilterCommon(minerFilter);
         wrapped.put("requiresReplacement", minerFilter.requiresReplacement);
         wrapped.put("replaceTarget", this.convert(minerFilter.replaceTarget));
         return wrapped;
      }
   }

   public Object convert(@Nullable SorterFilter<?> sorterFilter) {
      if (sorterFilter == null) {
         return null;
      } else {
         Map<String, Object> wrapped = this.convertFilterCommon(sorterFilter);
         wrapped.put("allowDefault", sorterFilter.allowDefault);
         wrapped.put("color", this.convert(sorterFilter.color));
         wrapped.put("size", sorterFilter.sizeMode);
         wrapped.put("min", sorterFilter.min);
         wrapped.put("max", sorterFilter.max);
         if (sorterFilter instanceof SorterItemStackFilter filter) {
            wrapped.put("fuzzy", filter.fuzzyMode);
         }

         return wrapped;
      }
   }

   public Object convert(@Nullable QIOFilter<?> qioFilter) {
      if (qioFilter == null) {
         return null;
      } else {
         Map<String, Object> wrapped = this.convertFilterCommon(qioFilter);
         if (qioFilter instanceof QIOItemStackFilter filter) {
            wrapped.put("fuzzy", filter.fuzzyMode);
         }

         return wrapped;
      }
   }

   public Object convert(@Nullable OredictionificatorFilter<?, ?, ?> filter) {
      if (filter == null) {
         return null;
      } else {
         Map<String, Object> wrapped = this.convertFilterCommon(filter);
         wrapped.put("target", filter.getFilterText());
         if (filter instanceof OredictionificatorItemFilter itemFilter) {
            wrapped.put("selected", this.convert(itemFilter.getResultElement()));
         }

         return wrapped;
      }
   }

   public <KEY, VALUE> Object convert(@NotNull Map<KEY, VALUE> res, Function<KEY, Object> keyConverter, @NotNull Function<VALUE, Object> valueConverter) {
      return res.entrySet()
         .stream()
         .collect(Collectors.toMap(entry -> keyConverter.apply((KEY)entry.getKey()), entry -> valueConverter.apply((VALUE)entry.getValue()), (a, b) -> b));
   }

   public Object convert(@Nullable Item item) {
      return item == null ? null : this.convert(RegistryUtils.getName(item));
   }

   public Object convert(@Nullable Convertable<?> convertable) {
      return convertable == null ? null : convertable.convert(this);
   }

   public Object convert(@Nullable MethodHelpData methodHelpData) {
      if (methodHelpData == null) {
         return null;
      } else {
         Map<String, Object> helpData = new HashMap<>();
         helpData.put("name", methodHelpData.methodName());
         if (methodHelpData.params() != null) {
            helpData.put("params", methodHelpData.params().stream().map(p -> {
               Map<String, Object> arg = new HashMap<>();
               arg.put("name", p.name());
               arg.put("type", p.type());
               if (p.values() != null) {
                  arg.put("values", p.values());
               }

               return arg;
            }).toList());
         }

         Map<String, Object> returns = new HashMap<>();
         returns.put("type", methodHelpData.returns().type());
         if (methodHelpData.returns().values() != null) {
            returns.put("values", methodHelpData.returns().values());
         }

         helpData.put("returns", returns);
         if (methodHelpData.description() != null) {
            helpData.put("description", methodHelpData.description());
         }

         return helpData;
      }
   }

   public static Class<?> convertType(Class<?> clazz) {
      if (clazz == UUID.class || clazz == ResourceLocation.class || clazz == Item.class || Enum.class.isAssignableFrom(clazz)) {
         return String.class;
      } else if (clazz == Frequency.class
         || clazz == Coord4D.class
         || clazz == Vec3i.class
         || clazz == FluidStack.class
         || clazz == ItemStack.class
         || clazz == BlockState.class) {
         return Map.class;
      } else if (ChemicalStack.class.isAssignableFrom(clazz) || IFilter.class.isAssignableFrom(clazz)) {
         return Map.class;
      } else {
         return clazz == Convertable.class ? Map.class : clazz;
      }
   }

   private static Map<Class<?>, TableType> getBuiltInTables() {
      Map<Class<?>, TableType> types = new HashMap<>();
      TableType.builder(Coord4D.class, "An xyz position with a dimension component")
         .addField("x", int.class, "The x component")
         .addField("y", int.class, "The y component")
         .addField("z", int.class, "The z component")
         .addField("dimension", ResourceLocation.class, "The dimension component")
         .build(types);
      TableType.builder(BlockPos.class, "An xyz position")
         .addField("x", int.class, "The x component")
         .addField("y", int.class, "The y component")
         .addField("z", int.class, "The z component")
         .build(types);
      TableType.builder(ItemStack.class, "A stack of Item(s)")
         .addField("name", Item.class, "The Item's registered name")
         .addField("count", int.class, "The count of items in the stack")
         .addField("nbt", String.class, "Any NBT of the item, in Command JSON format")
         .build(types);
      TableType.builder(FluidStack.class, "An amount of fluid")
         .addField("name", ResourceLocation.class, "The Fluid's registered name, e.g. minecraft:water")
         .addField("amount", int.class, "The amount in mB")
         .addField("nbt", String.class, "Any NBT of the fluid, in Command JSON format")
         .build(types);
      TableType.builder(ChemicalStack.class, "An amount of Gas/Fluid/Slurry/Pigment")
         .addField("name", Item.class, "The Chemical's registered name")
         .addField("amount", int.class, "The amount in mB")
         .build(types);
      TableType.builder(BlockState.class, "A Block State")
         .addField("block", String.class, "The Block's registered name, e.g. minecraft:sand")
         .addField("state", Map.class, "Any state parameters will be in Table format under this key. Not present if there are none")
         .build(types);
      TableType.builder(Frequency.class, "A frequency's identity")
         .addField("key", String.class, "Usually the name of the frequency entered in the GUI")
         .addField("public", boolean.class, "Whether the Frequency is public or not")
         .build(types);
      TableType.builder(
            IFilter.class,
            "Common Filter properties. Use the API Global to make constructing these a little easier.\nFilters are a combination of these base properties, an ItemStack or Mod Id or Tag component, and a device specific type.\nThe exception to that is an Oredictionificator filter, which does not have an item/mod/tag component."
         )
         .addField("type", FilterType.class, "The type of filter in this structure")
         .addField("enabled", boolean.class, "Whether the filter is enabled when added to a device")
         .build(types);
      TableType.builder(MinerFilter.class, "A Digital Miner filter")
         .extendedFrom(IFilter.class)
         .addField("requiresReplacement", boolean.class, "Whether the filter requires a replacement to be done before it will allow mining")
         .addField("replaceTarget", Item.class, "The name of the item block that will be used to replace a mined block")
         .build(types);
      TableType.builder(OredictionificatorItemFilter.class, "An Oredictionificator filter")
         .extendedFrom(IFilter.class)
         .addField("target", String.class, "The target tag to match (input)")
         .addField("selected", Item.class, "The selected output item's registered name. Optional for adding a filter")
         .build(types);
      TableType.builder(SorterFilter.class, "A Logistical Sorter filter")
         .extendedFrom(IFilter.class)
         .addField("allowDefault", boolean.class, "Allows the filtered item to travel to the default color destination")
         .addField("color", EnumColor.class, "The color configured, nil if none")
         .addField("size", boolean.class, "If Size Mode is enabled")
         .addField("min", int.class, "In Size Mode, the minimum to send")
         .addField("max", int.class, "In Size Mode, the maximum to send")
         .build(types);
      TableType.builder(QIOFilter.class, "A Quantum Item Orchestration filter").extendedFrom(IFilter.class).build(types);
      buildFilterVariants(types, SorterFilter.class, SorterItemStackFilter.class, SorterModIDFilter.class, SorterTagFilter.class, "Logistical Sorter", true);
      buildFilterVariants(types, MinerFilter.class, MinerItemStackFilter.class, MinerModIDFilter.class, MinerTagFilter.class, "Digital Miner", false);
      buildFilterVariants(types, QIOFilter.class, QIOItemStackFilter.class, QIOModIDFilter.class, QIOTagFilter.class, "QIO", true);
      return types;
   }

   private static <BASE> void buildFilterVariants(
      Map<Class<?>, TableType> types,
      Class<BASE> deviceFilterType,
      Class<? extends BASE> itemStackFilterClass,
      Class<? extends BASE> modIDFilterClass,
      Class<? extends BASE> tagFilterClass,
      String deviceName,
      boolean hasFuzzyItem
   ) {
      TableType.Builder itemstackBuilder = TableType.builder(itemStackFilterClass, deviceName + " filter with ItemStack filter properties")
         .extendedFrom(deviceFilterType)
         .addField("item", Item.class, "The filtered item's registered name")
         .addField("itemNBT", String.class, "The NBT data of the filtered item, optional");
      if (hasFuzzyItem) {
         itemstackBuilder.addField("fuzzy", boolean.class, "Whether Fuzzy mode is enabled (checks only the item name/type)");
      }

      itemstackBuilder.build(types);
      TableType.builder(modIDFilterClass, deviceName + " filter with Mod Id filter properties")
         .extendedFrom(deviceFilterType)
         .addField("modId", String.class, "The mod id to filter. e.g. mekansim")
         .build(types);
      TableType.builder(tagFilterClass, deviceName + " filter with Tag filter properties")
         .extendedFrom(deviceFilterType)
         .addField("tag", String.class, "The tag to filter. e.g. forge:ores")
         .build(types);
   }
}
