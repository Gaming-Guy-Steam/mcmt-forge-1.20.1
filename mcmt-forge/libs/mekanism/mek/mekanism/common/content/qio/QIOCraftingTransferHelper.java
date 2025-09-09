package mekanism.common.content.qio;

import it.unimi.dsi.fastutil.bytes.Byte2IntArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.MathUtils;
import mekanism.common.inventory.container.slot.HotBarSlot;
import mekanism.common.inventory.container.slot.InsertableSlot;
import mekanism.common.inventory.container.slot.MainInventorySlot;
import mekanism.common.lib.inventory.HashedItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QIOCraftingTransferHelper {
   public final Map<HashedItem, QIOCraftingTransferHelper.HashedItemSource> reverseLookup;
   private byte emptyInventorySlots;
   private boolean isValid = true;

   public QIOCraftingTransferHelper(
      Object2LongMap<HashedItem.UUIDAwareHashedItem> cachedInventory,
      List<HotBarSlot> hotBarSlots,
      List<MainInventorySlot> mainInventorySlots,
      QIOCraftingWindow craftingWindow,
      Player player
   ) {
      this.reverseLookup = new HashMap<>();
      ObjectIterator inventorySlotIndex = cachedInventory.object2LongEntrySet().iterator();

      while (inventorySlotIndex.hasNext()) {
         Entry<HashedItem.UUIDAwareHashedItem> entry = (Entry<HashedItem.UUIDAwareHashedItem>)inventorySlotIndex.next();
         HashedItem.UUIDAwareHashedItem source = (HashedItem.UUIDAwareHashedItem)entry.getKey();
         this.reverseLookup
            .computeIfAbsent(source.asRawHashedItem(), item -> new QIOCraftingTransferHelper.HashedItemSource())
            .addQIOSlot(source.getUUID(), entry.getLongValue());
      }

      for (inventorySlotIndex = 0; inventorySlotIndex < 9; inventorySlotIndex++) {
         IInventorySlot slot = craftingWindow.getInputSlot(inventorySlotIndex);
         if (!slot.isEmpty()) {
            if (slot.extractItem(1, Action.SIMULATE, AutomationType.MANUAL).m_41619_()) {
               this.isValid = false;
               return;
            }

            this.reverseLookup
               .computeIfAbsent(HashedItem.raw(slot.getStack()), item -> new QIOCraftingTransferHelper.HashedItemSource())
               .addSlot(inventorySlotIndex, slot.getCount());
         }
      }

      byte var10 = this.addSlotsToMap(player, hotBarSlots, inventorySlotIndex);
      this.addSlotsToMap(player, mainInventorySlots, var10);
   }

   private byte addSlotsToMap(Player player, List<? extends Slot> slots, byte inventorySlotIndex) {
      for (Slot slot : slots) {
         if (slot.m_6657_()) {
            if (slot.m_8010_(player)) {
               ItemStack stack = slot.m_7993_();
               this.reverseLookup
                  .computeIfAbsent(HashedItem.raw(stack), item -> new QIOCraftingTransferHelper.HashedItemSource())
                  .addSlot(inventorySlotIndex, stack.m_41613_());
            }
         } else {
            this.emptyInventorySlots++;
         }

         inventorySlotIndex++;
      }

      return inventorySlotIndex;
   }

   public boolean isInvalid() {
      return !this.isValid;
   }

   public byte getEmptyInventorySlots() {
      return this.emptyInventorySlots;
   }

   @Nullable
   public QIOCraftingTransferHelper.HashedItemSource getSource(@NotNull HashedItem item) {
      return this.reverseLookup.get(item);
   }

   public abstract static class BaseSimulatedInventory {
      private final ItemStack[] inventory;
      private final int[] stackSizes;
      private final int[] slotLimits;

      protected BaseSimulatedInventory(List<HotBarSlot> hotBarSlots, List<MainInventorySlot> mainInventorySlots) {
         int hotBarSize = hotBarSlots.size();
         int slots = hotBarSize + mainInventorySlots.size();
         this.inventory = new ItemStack[slots];
         this.stackSizes = new int[slots];
         this.slotLimits = new int[slots];

         for (int slot = 0; slot < slots; slot++) {
            InsertableSlot inventorySlot;
            if (slot < hotBarSize) {
               inventorySlot = hotBarSlots.get(slot);
            } else {
               inventorySlot = mainInventorySlots.get(slot - hotBarSize);
            }

            ItemStack stack = inventorySlot.m_7993_();
            int remaining = stack.m_41619_() ? 0 : this.getRemaining(slot, stack);
            if (remaining == 0) {
               stack = ItemStack.f_41583_;
            }

            this.stackSizes[slot] = remaining;
            this.inventory[slot] = stack;
            if (stack.m_41619_()) {
               this.slotLimits[slot] = inventorySlot.m_5866_(stack);
            } else {
               this.slotLimits[slot] = Math.min(inventorySlot.m_5866_(stack), stack.m_41741_());
            }
         }
      }

      protected abstract int getRemaining(int slot, ItemStack currentStored);

      public int shuffleItem(HashedItem type, int amount) {
         if (amount == 0) {
            return 0;
         } else {
            ItemStack stack = type.getInternalStack();

            for (int slot = 0; slot < this.inventory.length; slot++) {
               int currentAmount = this.stackSizes[slot];
               int max = this.slotLimits[slot];
               if (currentAmount < max && ItemHandlerHelper.canItemStacksStack(this.inventory[slot], stack)) {
                  int toPlace = Math.min(max - currentAmount, amount);
                  this.stackSizes[slot] = currentAmount + toPlace;
                  amount -= toPlace;
                  if (amount == 0) {
                     return 0;
                  }
               }
            }

            for (int slotx = 0; slotx < this.inventory.length; slotx++) {
               if (this.inventory[slotx].m_41619_()) {
                  int max = this.slotLimits[slotx];
                  if (max > 0) {
                     this.inventory[slotx] = stack;
                     int var10;
                     this.slotLimits[slotx] = var10 = Math.min(max, stack.m_41741_());
                     int toPlace = this.stackSizes[slotx] = Math.min(amount, var10);
                     amount -= toPlace;
                     if (amount == 0) {
                        return 0;
                     }
                  }
               }
            }

            return amount;
         }
      }

      @Nullable
      public Object2IntMap<HashedItem> shuffleInputs(Object2IntMap<HashedItem> leftOverInput, boolean hasFrequency) {
         Object2IntMap<HashedItem> stillLeftOver = (Object2IntMap<HashedItem>)(hasFrequency
            ? new Object2IntArrayMap(leftOverInput.size())
            : Object2IntMaps.emptyMap());
         ObjectIterator var4 = leftOverInput.object2IntEntrySet().iterator();

         while (var4.hasNext()) {
            it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<HashedItem> entry = (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<HashedItem>)var4.next();
            int remaining = this.shuffleItem((HashedItem)entry.getKey(), entry.getIntValue());
            if (remaining > 0) {
               if (!hasFrequency) {
                  return null;
               }

               stillLeftOver.put((HashedItem)entry.getKey(), remaining);
            }
         }

         return stillLeftOver;
      }
   }

   public static class HashedItemSource {
      @Nullable
      private Object2LongMap<UUID> qioSources;
      @Nullable
      private Byte2IntMap slots;
      private long available;
      private long matches;

      public long getAvailable() {
         return this.available;
      }

      public void matchFound() {
         this.matches++;
      }

      public boolean hasMoreRemaining() {
         return this.available > this.matches;
      }

      private void addQIOSlot(UUID source, long stored) {
         if (this.qioSources == null) {
            this.qioSources = new Object2LongOpenHashMap();
         }

         this.qioSources.put(source, stored);
         this.available += stored;
      }

      private void addSlot(byte slot, int count) {
         if (this.slots == null) {
            this.slots = new Byte2IntArrayMap();
         }

         this.slots.put(slot, count);
         this.available += count;
      }

      public int getSlotRemaining(byte slot) {
         return this.slots == null ? 0 : this.slots.getOrDefault(slot, 0);
      }

      public long getQIORemaining(UUID uuid) {
         return this.qioSources == null ? 0L : this.qioSources.getOrDefault(uuid, 0L);
      }

      public boolean hasQIOSources() {
         return this.qioSources != null;
      }

      public List<QIOCraftingTransferHelper.SingularHashedItemSource> use(int toUse) {
         if (toUse > this.available) {
            return Collections.emptyList();
         } else {
            this.matches--;
            List<QIOCraftingTransferHelper.SingularHashedItemSource> sources = new ArrayList<>();
            if (this.slots != null) {
               ObjectIterator<it.unimi.dsi.fastutil.bytes.Byte2IntMap.Entry> iter = this.slots.byte2IntEntrySet().iterator();

               while (iter.hasNext()) {
                  it.unimi.dsi.fastutil.bytes.Byte2IntMap.Entry entry = (it.unimi.dsi.fastutil.bytes.Byte2IntMap.Entry)iter.next();
                  int stored = entry.getIntValue();
                  byte slot = entry.getByteKey();
                  if (stored > toUse) {
                     this.slots.put(slot, stored - toUse);
                     this.available -= toUse;
                     sources.add(new QIOCraftingTransferHelper.SingularHashedItemSource(slot, toUse));
                     return sources;
                  }

                  this.available -= stored;
                  sources.add(new QIOCraftingTransferHelper.SingularHashedItemSource(slot, MathUtils.clampToInt((long)stored)));
                  iter.remove();
                  if (stored == toUse) {
                     return sources;
                  }

                  toUse -= stored;
               }
            }

            if (this.qioSources != null) {
               ObjectIterator<Entry<UUID>> iter = this.qioSources.object2LongEntrySet().iterator();

               while (iter.hasNext()) {
                  Entry<UUID> entryx = (Entry<UUID>)iter.next();
                  long storedx = entryx.getLongValue();
                  UUID key = (UUID)entryx.getKey();
                  if (storedx > toUse) {
                     this.qioSources.put(key, storedx - toUse);
                     this.available -= toUse;
                     sources.add(new QIOCraftingTransferHelper.SingularHashedItemSource(key, toUse));
                     return sources;
                  }

                  this.available -= storedx;
                  sources.add(new QIOCraftingTransferHelper.SingularHashedItemSource(key, MathUtils.clampToInt(storedx)));
                  iter.remove();
                  if (storedx == toUse) {
                     return sources;
                  }

                  toUse = (int)(toUse - storedx);
               }
            }

            return Collections.emptyList();
         }
      }
   }

   public static class SingularHashedItemSource {
      @Nullable
      private final UUID qioSource;
      private final byte slot;
      private int used;

      public SingularHashedItemSource(@NotNull UUID qioSource, int used) {
         this.qioSource = qioSource;
         this.slot = -1;
         this.used = used;
      }

      public SingularHashedItemSource(byte slot, int used) {
         this.qioSource = null;
         this.slot = slot;
         this.used = used;
      }

      public int getUsed() {
         return this.used;
      }

      public void setUsed(int used) {
         if (used >= 0 && used <= this.used) {
            this.used = used;
         } else {
            throw new IllegalArgumentException("Used must be a lower amount than currently being used if getting updated.");
         }
      }

      public byte getSlot() {
         return this.slot;
      }

      @Nullable
      public UUID getQioSource() {
         return this.qioSource;
      }
   }
}
