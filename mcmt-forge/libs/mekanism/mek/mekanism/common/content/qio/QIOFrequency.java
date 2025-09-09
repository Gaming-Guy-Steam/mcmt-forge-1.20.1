package mekanism.common.content.qio;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.ObjLongConsumer;
import java.util.function.Supplier;
import mekanism.api.Action;
import mekanism.api.inventory.IHashedItem;
import mekanism.api.inventory.qio.IQIOFrequency;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.common.CommonWorldTickHandler;
import mekanism.common.Mekanism;
import mekanism.common.base.TagCache;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.lib.WildcardMatcher;
import mekanism.common.lib.collection.BiMultimap;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IColorableFrequency;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.network.to_client.PacketQIOItemViewerGuiSync;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.Nullable;

public class QIOFrequency extends Frequency implements IColorableFrequency, IQIOFrequency {
   private static final Random rand = new Random();
   private final Map<QIODriveData.QIODriveKey, QIODriveData> driveMap = new LinkedHashMap<>();
   private final Map<HashedItem, QIOFrequency.QIOItemTypeData> itemDataMap = new LinkedHashMap<>();
   private final Set<IQIODriveHolder> driveHolders = new HashSet<>();
   private final BiMultimap<String, HashedItem> tagLookupMap = new BiMultimap<>();
   private final Map<String, Set<HashedItem>> modIDLookupMap = new HashMap<>();
   private final Map<Item, Set<HashedItem>> fuzzyItemLookupMap = new IdentityHashMap<>();
   private final SetMultimap<String, String> tagWildcardCache = HashMultimap.create();
   private final Set<String> failedWildcardTags = new HashSet<>();
   private final SetMultimap<String, String> modIDWildcardCache = HashMultimap.create();
   private final Set<String> failedWildcardModIDs = new HashSet<>();
   private final Set<UUID> updatedItems = new HashSet<>();
   private final Set<ServerPlayer> playersViewingItems = new HashSet<>();
   private boolean needsUpdate;
   private boolean isDirty;
   private long totalCount;
   private long totalCountCapacity;
   private int totalTypeCapacity;
   private int clientTypes;
   private EnumColor color = EnumColor.INDIGO;

   public QIOFrequency(String n, @Nullable UUID uuid) {
      super(FrequencyType.QIO, n, uuid);
   }

   public QIOFrequency() {
      super(FrequencyType.QIO);
   }

   public Map<HashedItem, QIOFrequency.QIOItemTypeData> getItemDataMap() {
      return this.itemDataMap;
   }

   @Override
   public void forAllStored(ObjLongConsumer<ItemStack> consumer) {
      this.itemDataMap.forEach((type, data) -> consumer.accept(type.createStack(1), data.getCount()));
   }

   @Override
   public void forAllHashedStored(ObjLongConsumer<IHashedItem> consumer) {
      this.itemDataMap.forEach((type, data) -> consumer.accept(type, data.getCount()));
   }

   @Override
   public long massInsert(ItemStack stack, long amount, Action action) {
      if (!stack.m_41619_() && amount > 0L) {
         HashedItem type = action.execute() ? HashedItem.create(stack) : HashedItem.raw(stack);
         if (this.totalCount != this.totalCountCapacity && (this.itemDataMap.containsKey(type) || this.itemDataMap.size() != this.totalTypeCapacity)) {
            QIOFrequency.QIOItemTypeData data;
            if (action.execute()) {
               data = this.itemDataMap.computeIfAbsent(type, this::createTypeDataForAbsent);
            } else {
               data = this.itemDataMap.get(type);
               if (data == null) {
                  data = new QIOFrequency.QIOItemTypeData(type);
               }
            }

            return amount - data.add(amount, action);
         } else {
            return 0L;
         }
      } else {
         return 0L;
      }
   }

   public ItemStack addItem(ItemStack stack) {
      if (stack.m_41619_()) {
         return ItemStack.f_41583_;
      } else if (this.totalCount == this.totalCountCapacity) {
         return stack;
      } else {
         HashedItem type = HashedItem.create(stack);
         QIOFrequency.QIOItemTypeData data = this.itemDataMap
            .computeIfAbsent(type, t -> this.itemDataMap.size() == this.totalTypeCapacity ? null : this.createTypeDataForAbsent(t));
         return data == null ? stack : type.createStack(MathUtils.clampToInt(data.add((long)stack.m_41613_(), Action.EXECUTE)));
      }
   }

   private QIOFrequency.QIOItemTypeData createTypeDataForAbsent(HashedItem type) {
      ItemStack stack = type.getInternalStack();
      List<String> tags = TagCache.getItemTags(stack);
      if (!tags.isEmpty()) {
         boolean hasAllKeys = this.tagLookupMap.hasAllKeys(tags);
         if (this.tagLookupMap.putAll(tags, type) && !hasAllKeys) {
            this.tagWildcardCache.clear();
            this.failedWildcardTags.clear();
         }
      }

      this.modIDLookupMap.computeIfAbsent(MekanismUtils.getModId(stack), modID -> {
         this.modIDWildcardCache.clear();
         this.failedWildcardModIDs.clear();
         return new HashSet<>();
      }).add(type);
      this.fuzzyItemLookupMap.computeIfAbsent(stack.m_41720_(), item -> new HashSet<>()).add(type);
      QIOGlobalItemLookup.INSTANCE.getOrTrackUUID(type);
      return new QIOFrequency.QIOItemTypeData(type);
   }

   @Override
   public long massExtract(ItemStack stack, long amount, Action action) {
      if (amount > 0L && !stack.m_41619_() && !this.itemDataMap.isEmpty()) {
         HashedItem type = HashedItem.raw(stack);
         QIOFrequency.QIOItemTypeData data = this.itemDataMap.get(type);
         if (data == null) {
            return 0L;
         } else {
            long removed = data.remove(amount, action);
            if (action.execute() && data.count == 0L) {
               this.removeItemData(data.itemType);
            }

            return removed;
         }
      } else {
         return 0L;
      }
   }

   public ItemStack removeItem(int amount) {
      return this.removeByType(null, amount);
   }

   public ItemStack removeItem(ItemStack stack, int amount) {
      return stack.m_41619_() ? ItemStack.f_41583_ : this.removeByType(HashedItem.raw(stack), amount);
   }

   public ItemStack removeByType(@Nullable HashedItem itemType, int amount) {
      if (!this.itemDataMap.isEmpty() && amount > 0) {
         QIOFrequency.QIOItemTypeData data;
         if (itemType == null) {
            Entry<HashedItem, QIOFrequency.QIOItemTypeData> entry = this.itemDataMap.entrySet().iterator().next();
            itemType = entry.getKey();
            data = entry.getValue();
         } else {
            data = this.itemDataMap.get(itemType);
            if (data == null) {
               return ItemStack.f_41583_;
            }
         }

         ItemStack removed = data.remove(amount);
         if (data.count == 0L) {
            this.removeItemData(data.itemType);
         }

         return removed;
      } else {
         return ItemStack.f_41583_;
      }
   }

   private void removeItemData(HashedItem type) {
      this.itemDataMap.remove(type);
      Set<String> tags = new HashSet<>(this.tagLookupMap.getKeys(type));
      if (this.tagLookupMap.removeValue(type) && !this.tagLookupMap.hasAllKeys(tags)) {
         this.tagWildcardCache.clear();
      }

      ItemStack stack = type.getInternalStack();
      String modID = MekanismUtils.getModId(stack);
      Set<HashedItem> itemsForMod = this.modIDLookupMap.get(modID);
      if (itemsForMod != null && itemsForMod.remove(type) && itemsForMod.isEmpty()) {
         this.modIDLookupMap.remove(modID);
         this.modIDWildcardCache.clear();
      }

      Item item = stack.m_41720_();
      Set<HashedItem> itemsByFuzzy = this.fuzzyItemLookupMap.get(item);
      if (itemsByFuzzy != null && itemsByFuzzy.remove(type) && itemsByFuzzy.isEmpty()) {
         this.fuzzyItemLookupMap.remove(item);
      }
   }

   public Set<HashedItem> getTypesForItem(Item item) {
      return Collections.unmodifiableSet(this.fuzzyItemLookupMap.getOrDefault(item, Collections.emptySet()));
   }

   public Object2LongMap<HashedItem> getStacksByItem(Item item) {
      return this.getStacksWithCounts(this.fuzzyItemLookupMap.get(item));
   }

   public Object2LongMap<HashedItem> getStacksByTag(String tag) {
      return this.getStacksWithCounts(this.tagLookupMap.getValues(tag));
   }

   public Object2LongMap<HashedItem> getStacksByModID(String modID) {
      return this.getStacksWithCounts(this.modIDLookupMap.get(modID));
   }

   private Object2LongMap<HashedItem> getStacksWithCounts(@Nullable Set<HashedItem> items) {
      if (items != null && !items.isEmpty()) {
         Object2LongMap<HashedItem> ret = new Object2LongOpenHashMap();

         for (HashedItem item : items) {
            ret.put(item, this.getStored(item));
         }

         return ret;
      } else {
         return Object2LongMaps.emptyMap();
      }
   }

   public Object2LongMap<HashedItem> getStacksByTagWildcard(String wildcard) {
      if (!this.hasMatchingElements(this.tagWildcardCache, this.failedWildcardTags, wildcard, this.tagLookupMap::getAllKeys)) {
         return Object2LongMaps.emptyMap();
      } else {
         Object2LongMap<HashedItem> ret = new Object2LongOpenHashMap();

         for (String match : this.tagWildcardCache.get(wildcard)) {
            for (HashedItem item : this.tagLookupMap.getValues(match)) {
               ret.computeIfAbsent(item, type -> this.getStored(type));
            }
         }

         return ret;
      }
   }

   public Object2LongMap<HashedItem> getStacksByModIDWildcard(String wildcard) {
      if (!this.hasMatchingElements(this.modIDWildcardCache, this.failedWildcardModIDs, wildcard, this.modIDLookupMap::keySet)) {
         return Object2LongMaps.emptyMap();
      } else {
         Object2LongMap<HashedItem> ret = new Object2LongOpenHashMap();

         for (String match : this.modIDWildcardCache.get(wildcard)) {
            for (HashedItem item : this.modIDLookupMap.get(match)) {
               ret.put(item, this.getStored(item));
            }
         }

         return ret;
      }
   }

   private boolean hasMatchingElements(
      SetMultimap<String, String> wildcardCache, Set<String> failedWildcards, String wildcard, Supplier<Set<String>> entriesSupplier
   ) {
      if (failedWildcards.contains(wildcard)) {
         return false;
      } else if (!wildcardCache.containsKey(wildcard) && !this.buildWildcardMapping(wildcardCache, wildcard, entriesSupplier.get())) {
         failedWildcards.add(wildcard);
         return false;
      } else {
         return true;
      }
   }

   private boolean buildWildcardMapping(SetMultimap<String, String> wildcardCache, String wildcard, Set<String> entries) {
      boolean added = false;

      for (String entry : entries) {
         if (WildcardMatcher.matches(wildcard, entry)) {
            added |= wildcardCache.put(wildcard, entry);
         }
      }

      return added;
   }

   public void openItemViewer(ServerPlayer player) {
      this.playersViewingItems.add(player);
      Object2LongMap<HashedItem.UUIDAwareHashedItem> map = new Object2LongOpenHashMap(this.itemDataMap.size());

      for (QIOFrequency.QIOItemTypeData data : this.itemDataMap.values()) {
         map.put(new HashedItem.UUIDAwareHashedItem(data.itemType, QIOGlobalItemLookup.INSTANCE.getOrTrackUUID(data.itemType)), data.count);
      }

      Mekanism.packetHandler().sendTo(PacketQIOItemViewerGuiSync.batch(map, this.totalCountCapacity, this.totalTypeCapacity), player);
   }

   public void closeItemViewer(ServerPlayer player) {
      this.playersViewingItems.remove(player);
   }

   @Override
   public EnumColor getColor() {
      return this.color;
   }

   @Override
   public void setColor(EnumColor color) {
      if (this.color != color) {
         this.color = color;
         this.dirty = true;
      }
   }

   public long getTotalItemCount() {
      return this.totalCount;
   }

   public long getTotalItemCountCapacity() {
      return this.totalCountCapacity;
   }

   public int getTotalItemTypes(boolean remote) {
      return remote ? this.clientTypes : this.itemDataMap.size();
   }

   public int getTotalItemTypeCapacity() {
      return this.totalTypeCapacity;
   }

   @Override
   public long getStored(ItemStack type) {
      return type.m_41619_() ? 0L : this.getStored(HashedItem.raw(type));
   }

   public long getStored(HashedItem itemType) {
      QIOFrequency.QIOItemTypeData data = this.itemDataMap.get(itemType);
      return data == null ? 0L : data.count;
   }

   public boolean isStoring(HashedItem itemType) {
      return this.getStored(itemType) > 0L;
   }

   public QIODriveData getDriveData(QIODriveData.QIODriveKey key) {
      return this.driveMap.get(key);
   }

   public Collection<QIODriveData> getAllDrives() {
      return this.driveMap.values();
   }

   @Override
   public boolean tick() {
      boolean superDirty = super.tick();
      if (!this.updatedItems.isEmpty() || this.needsUpdate) {
         Lazy<PacketQIOItemViewerGuiSync> lazyPacket = Lazy.of(() -> {
            Object2LongMap<HashedItem.UUIDAwareHashedItem> map = new Object2LongOpenHashMap(this.updatedItems.size());
            this.updatedItems.forEach(uuid -> {
               HashedItem type = QIOGlobalItemLookup.INSTANCE.getTypeByUUID(uuid);
               if (type != null) {
                  QIOFrequency.QIOItemTypeData data = this.itemDataMap.get(type);
                  map.put(new HashedItem.UUIDAwareHashedItem(type, uuid), data == null ? 0L : data.count);
               }
            });
            return PacketQIOItemViewerGuiSync.update(map, this.totalCountCapacity, this.totalTypeCapacity);
         });
         Iterator<ServerPlayer> viewingIterator = this.playersViewingItems.iterator();

         while (viewingIterator.hasNext()) {
            ServerPlayer player = viewingIterator.next();
            if (player.f_36096_ instanceof QIOItemViewerContainer) {
               Mekanism.packetHandler().sendTo((PacketQIOItemViewerGuiSync)lazyPacket.get(), player);
            } else {
               viewingIterator.remove();
            }
         }

         this.updatedItems.clear();
         this.needsUpdate = false;
      }

      if (this.isDirty && rand.nextInt(100) == 0) {
         this.saveAll();
         this.isDirty = false;
      }

      if (CommonWorldTickHandler.flushTagAndRecipeCaches) {
         this.tagLookupMap.clear();
         this.tagWildcardCache.clear();
         this.itemDataMap.values().forEach(item -> this.tagLookupMap.putAll(TagCache.getItemTags(item.itemType.getInternalStack()), item.itemType));
      }

      return superDirty;
   }

   @Override
   public boolean onDeactivate(BlockEntity tile) {
      boolean changedData = super.onDeactivate(tile);
      if (tile instanceof IQIODriveHolder holder) {
         int i = 0;

         for (int size = holder.getDriveSlots().size(); i < size; i++) {
            QIODriveData.QIODriveKey key = new QIODriveData.QIODriveKey(holder, i);
            this.removeDrive(key, true);
         }

         this.driveHolders.remove(holder);
      }

      return changedData;
   }

   @Override
   public boolean update(BlockEntity tile) {
      boolean changedData = super.update(tile);
      if (tile instanceof IQIODriveHolder holder && this.driveHolders.add(holder)) {
         int i = 0;

         for (int slots = holder.getDriveSlots().size(); i < slots; i++) {
            this.addDrive(new QIODriveData.QIODriveKey(holder, i));
         }
      }

      return changedData;
   }

   @Override
   public void onRemove() {
      super.onRemove();
      Set<QIODriveData.QIODriveKey> keys = new HashSet<>(this.driveMap.keySet());
      keys.forEach(key -> this.removeDrive(key, false));
      this.driveMap.clear();
      this.playersViewingItems.forEach(player -> Mekanism.packetHandler().sendTo(PacketQIOItemViewerGuiSync.kill(), player));
   }

   @Override
   public int getSyncHash() {
      int code = super.getSyncHash();
      code = 31 * code + Long.hashCode(this.totalCount);
      code = 31 * code + Long.hashCode(this.totalCountCapacity);
      code = 31 * code + this.itemDataMap.size();
      code = 31 * code + this.totalTypeCapacity;
      return 31 * code + this.color.ordinal();
   }

   @Override
   public void write(FriendlyByteBuf buf) {
      super.write(buf);
      buf.m_130103_(this.totalCount);
      buf.m_130103_(this.totalCountCapacity);
      buf.m_130130_(this.itemDataMap.size());
      buf.m_130130_(this.totalTypeCapacity);
      buf.m_130068_(this.color);
   }

   @Override
   public void read(FriendlyByteBuf buf) {
      super.read(buf);
      this.totalCount = buf.m_130258_();
      this.totalCountCapacity = buf.m_130258_();
      this.clientTypes = buf.m_130242_();
      this.totalTypeCapacity = buf.m_130242_();
      this.color = (EnumColor)buf.m_130066_(EnumColor.class);
   }

   @Override
   public void write(CompoundTag nbtTags) {
      super.write(nbtTags);
      NBTUtils.writeEnum(nbtTags, "color", this.color);
   }

   @Override
   protected void read(CompoundTag nbtTags) {
      super.read(nbtTags);
      NBTUtils.setEnumIfPresent(nbtTags, "color", EnumColor::byIndexStatic, color -> this.color = color);
   }

   public void addDrive(QIODriveData.QIODriveKey key) {
      if (key.getDriveStack().m_41720_() instanceof IQIODriveItem) {
         if (this.driveMap.containsKey(key)) {
            this.removeDrive(key, true);
         }

         QIODriveData data = new QIODriveData(key);
         this.totalCountCapacity = this.totalCountCapacity + data.getCountCapacity();
         this.totalTypeCapacity = this.totalTypeCapacity + data.getTypeCapacity();
         this.driveMap.put(key, data);
         data.getItemMap().forEach((storedKey, value) -> {
            this.itemDataMap.computeIfAbsent(storedKey, this::createTypeDataForAbsent).addFromDrive(data, value);
            this.markForUpdate(storedKey);
         });
         this.setNeedsUpdate();
      }
   }

   public void removeDrive(QIODriveData.QIODriveKey key, boolean updateItemMap) {
      if (this.driveMap.containsKey(key)) {
         QIODriveData data = this.driveMap.get(key);
         if (updateItemMap) {
            data.getItemMap().forEach((storedKey, value) -> {
               QIOFrequency.QIOItemTypeData itemData = this.itemDataMap.get(storedKey);
               if (itemData != null) {
                  itemData.containingDrives.remove(key);
                  itemData.count = itemData.count - value;
                  this.totalCount = this.totalCount - value;
                  this.markForUpdate(storedKey);
                  if (itemData.containingDrives.isEmpty() || itemData.count == 0L) {
                     this.removeItemData(storedKey);
                  }
               }
            });
            this.setNeedsUpdate();
         }

         this.totalCountCapacity = this.totalCountCapacity - data.getCountCapacity();
         this.totalTypeCapacity = this.totalTypeCapacity - data.getTypeCapacity();
         this.driveMap.remove(key);
         key.updateMetadata(data);
         key.save(data);
      }
   }

   public void saveAll() {
      this.driveMap.forEach((key, value) -> {
         key.updateMetadata(value);
         key.save(value);
      });
   }

   private void setNeedsUpdate(@Nullable HashedItem changedItem) {
      this.isDirty = true;
      if (!this.playersViewingItems.isEmpty()) {
         this.needsUpdate = true;
         if (changedItem != null) {
            this.updatedItems.add(QIOGlobalItemLookup.INSTANCE.getUUIDForType(changedItem));
         }
      }
   }

   private void markForUpdate(HashedItem changedItem) {
      if (!this.playersViewingItems.isEmpty()) {
         this.updatedItems.add(QIOGlobalItemLookup.INSTANCE.getUUIDForType(changedItem));
      }
   }

   private void setNeedsUpdate() {
      this.setNeedsUpdate(null);
   }

   public class QIOItemTypeData {
      private final HashedItem itemType;
      private long count = 0L;
      private final Set<QIODriveData.QIODriveKey> containingDrives = new HashSet<>();

      public QIOItemTypeData(HashedItem itemType) {
         this.itemType = itemType;
      }

      private void addFromDrive(QIODriveData data, long toAdd) {
         this.count += toAdd;
         QIOFrequency.this.totalCount += toAdd;
         this.containingDrives.add(data.getKey());
         QIOFrequency.this.setNeedsUpdate();
      }

      private long add(long amount, Action action) {
         long toAdd = amount;

         for (QIODriveData.QIODriveKey key : this.containingDrives) {
            toAdd = this.addItemsToDrive(toAdd, QIOFrequency.this.driveMap.get(key), action);
            if (toAdd == 0L) {
               break;
            }
         }

         if (toAdd > 0L) {
            for (QIODriveData data : QIOFrequency.this.driveMap.values()) {
               if (!this.containingDrives.contains(data.getKey())) {
                  toAdd = this.addItemsToDrive(toAdd, data, action);
                  if (toAdd == 0L) {
                     break;
                  }
               }
            }
         }

         if (action.execute()) {
            this.count += amount - toAdd;
            QIOFrequency.this.totalCount += amount - toAdd;
            QIOFrequency.this.setNeedsUpdate(this.itemType);
         }

         return toAdd;
      }

      private long addItemsToDrive(long toAdd, QIODriveData data, Action action) {
         long rejects = data.add(this.itemType, toAdd, action);
         if (action.execute() && rejects < toAdd) {
            this.containingDrives.add(data.getKey());
         }

         return rejects;
      }

      private long remove(long amount, Action action) {
         long removed = 0L;
         Iterator<QIODriveData.QIODriveKey> iter = this.containingDrives.iterator();

         while (iter.hasNext()) {
            QIODriveData data = QIOFrequency.this.driveMap.get(iter.next());
            removed += data.remove(this.itemType, amount - removed, action);
            if (action.execute() && data.getStored(this.itemType) == 0L) {
               iter.remove();
            }

            if (removed == amount) {
               break;
            }
         }

         if (action.execute()) {
            this.count -= removed;
            QIOFrequency.this.totalCount -= removed;
            QIOFrequency.this.setNeedsUpdate(this.itemType);
         }

         return removed;
      }

      private ItemStack remove(int amount) {
         int removed = MathUtils.clampToInt(this.remove(amount, Action.EXECUTE));
         return removed == 0 ? ItemStack.f_41583_ : this.itemType.createStack(removed);
      }

      public long getCount() {
         return this.count;
      }
   }
}
