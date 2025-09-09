package mekanism.common.content.qio;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMaps;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.math.MathUtils;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.inventory.container.slot.HotBarSlot;
import mekanism.common.inventory.container.slot.InsertableSlot;
import mekanism.common.inventory.container.slot.MainInventorySlot;
import mekanism.common.inventory.slot.CraftingWindowInventorySlot;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

public class QIOServerCraftingTransferHandler {
   private final QIOCraftingWindow craftingWindow;
   private final ResourceLocation recipeID;
   private final Player player;
   @Nullable
   private final QIOFrequency frequency;
   private final List<HotBarSlot> hotBarSlots;
   private final List<MainInventorySlot> mainInventorySlots;
   private final Byte2ObjectMap<QIOServerCraftingTransferHandler.SlotData> availableItems = new Byte2ObjectOpenHashMap();
   private final Map<UUID, QIOServerCraftingTransferHandler.FrequencySlotData> frequencyAvailableItems = new HashMap<>();
   private final NonNullList<ItemStack> recipeToTest = NonNullList.m_122780_(9, ItemStack.f_41583_);

   public static void tryTransfer(
      QIOItemViewerContainer container,
      byte selectedCraftingGrid,
      Player player,
      ResourceLocation recipeID,
      CraftingRecipe recipe,
      Byte2ObjectMap<List<QIOCraftingTransferHelper.SingularHashedItemSource>> sources
   ) {
      QIOServerCraftingTransferHandler transferHandler = new QIOServerCraftingTransferHandler(container, selectedCraftingGrid, player, recipeID);
      transferHandler.tryTransfer(recipe, sources);
   }

   private QIOServerCraftingTransferHandler(QIOItemViewerContainer container, byte selectedCraftingGrid, Player player, ResourceLocation recipeID) {
      this.player = player;
      this.recipeID = recipeID;
      this.frequency = container.getFrequency();
      this.craftingWindow = container.getCraftingWindow(selectedCraftingGrid);
      this.hotBarSlots = container.getHotBarSlots();
      this.mainInventorySlots = container.getMainInventorySlots();
   }

   private void tryTransfer(CraftingRecipe recipe, Byte2ObjectMap<List<QIOCraftingTransferHelper.SingularHashedItemSource>> sources) {
      for (byte slot = 0; slot < 9; slot++) {
         CraftingWindowInventorySlot inputSlot = this.craftingWindow.getInputSlot(slot);
         if (!inputSlot.isEmpty()) {
            ItemStack available = inputSlot.extractItem(inputSlot.getCount(), Action.SIMULATE, AutomationType.INTERNAL);
            if (available.m_41613_() < inputSlot.getCount()) {
               Mekanism.logger
                  .warn(
                     "Received transfer request from: {}, for: {}, and was unable to extract all items from crafting input slot: {}.",
                     new Object[]{this.player, this.recipeID, slot}
                  );
               return;
            }

            this.availableItems.put(slot, new QIOServerCraftingTransferHandler.SlotData(available));
         }
      }

      ObjectIterator var12 = sources.byte2ObjectEntrySet().iterator();

      while (var12.hasNext()) {
         Entry<List<QIOCraftingTransferHelper.SingularHashedItemSource>> entry = (Entry<List<QIOCraftingTransferHelper.SingularHashedItemSource>>)var12.next();
         byte targetSlot = entry.getByteKey();
         if (targetSlot < 0 || targetSlot >= 9) {
            Mekanism.logger
               .warn("Received transfer request from: {}, for: {}, with an invalid target slot id: {}.", new Object[]{this.player, this.recipeID, targetSlot});
            return;
         }

         int stackSize = 0;
         List<QIOCraftingTransferHelper.SingularHashedItemSource> singleSources = (List<QIOCraftingTransferHelper.SingularHashedItemSource>)entry.getValue();
         Iterator<QIOCraftingTransferHelper.SingularHashedItemSource> iter = singleSources.iterator();

         while (iter.hasNext()) {
            QIOCraftingTransferHelper.SingularHashedItemSource source = iter.next();
            byte slotx = source.getSlot();
            int used;
            if (slotx == -1) {
               used = this.simulateQIOSource(targetSlot, source.getQioSource(), source.getUsed(), stackSize);
            } else {
               used = this.simulateSlotSource(targetSlot, slotx, source.getUsed(), stackSize);
            }

            if (used == -1) {
               return;
            }

            if (used == 0) {
               iter.remove();
            } else {
               if (used < source.getUsed()) {
                  source.setUsed(used);
               }

               stackSize += used;
            }
         }

         if (singleSources.isEmpty()) {
            Mekanism.logger
               .warn("Received transfer request from: {}, for: {}, that had no valid sources, this should not be possible.", this.player, this.recipeID);
            return;
         }

         ItemStack resultItem = (ItemStack)this.recipeToTest.get(targetSlot);
         if (!resultItem.m_41619_() && resultItem.m_41741_() < stackSize) {
            Mekanism.logger
               .warn(
                  "Received transfer request from: {}, for: {}, that tried to transfer more items into: {} than can stack ({}) in one slot.",
                  new Object[]{this.player, this.recipeID, targetSlot, resultItem.m_41741_()}
               );
            return;
         }
      }

      CraftingContainer dummy = MekanismUtils.getDummyCraftingInv();

      for (int slotxx = 0; slotxx < 9; slotxx++) {
         dummy.m_6836_(slotxx, ((ItemStack)this.recipeToTest.get(slotxx)).m_255036_(1));
      }

      if (!recipe.m_5818_(dummy, this.player.m_9236_())) {
         Mekanism.logger.warn("Received transfer request from: {}, but source items aren't valid for the requested recipe: {}.", this.player, this.recipeID);
      } else if (!this.hasRoomToShuffle()) {
         Mekanism.logger
            .debug(
               "Received transfer request from: {}, but there is not enough room to shuffle items around for the requested recipe: {}.",
               this.player,
               this.recipeID
            );
      } else {
         this.transferItems(sources);
      }
   }

   private int simulateQIOSource(byte targetSlot, UUID qioSource, int used, int currentStackSize) {
      if (qioSource == null) {
         return this.fail("Received transfer request from: {}, for: {}, with no valid source.", this.player, this.recipeID);
      } else {
         QIOServerCraftingTransferHandler.FrequencySlotData slotData = this.frequencyAvailableItems.get(qioSource);
         if (slotData == null) {
            if (this.frequency == null) {
               return this.fail("Received transfer request from: {}, for: {}, with a QIO source but no selected frequency.", this.player, this.recipeID);
            }

            HashedItem storedItem = QIOGlobalItemLookup.INSTANCE.getTypeByUUID(qioSource);
            if (storedItem == null) {
               return this.fail("Received transfer request from: {}, for: {}, for item with unknown UUID: {}.", this.player, this.recipeID, qioSource);
            }

            long stored = this.frequency.getStored(storedItem);
            slotData = stored == 0L
               ? QIOServerCraftingTransferHandler.FrequencySlotData.EMPTY
               : new QIOServerCraftingTransferHandler.FrequencySlotData(storedItem, stored);
            this.frequencyAvailableItems.put(qioSource, slotData);
         }

         return this.addStackToRecipe(targetSlot, slotData, used, (byte)-1, currentStackSize);
      }
   }

   private int simulateSlotSource(byte targetSlot, byte slot, int used, int currentStackSize) {
      if (slot >= 0 && slot < 9 + Inventory.m_36059_() + 27) {
         QIOServerCraftingTransferHandler.SlotData slotData = (QIOServerCraftingTransferHandler.SlotData)this.availableItems.get(slot);
         if (slotData == null) {
            if (slot < 9) {
               return this.fail(
                  "Received transfer request from: {}, for: {}, with a request to take from crafting window slot: {}, but that slot cannot be taken from.",
                  this.player,
                  this.recipeID,
                  slot
               );
            }

            InsertableSlot inventorySlot;
            if (slot < 9 + Inventory.m_36059_()) {
               int actualSlot = slot - 9;
               if (actualSlot >= this.hotBarSlots.size()) {
                  return this.fail("Received transfer request from: {}, for: {}, could not find hotbar slot: {}.", this.player, this.recipeID, actualSlot);
               }

               inventorySlot = this.hotBarSlots.get(actualSlot);
               if (!inventorySlot.m_8010_(this.player)) {
                  return this.fail(
                     "Received transfer request from: {}, for: {}, with a request to take from hotbar slot: {}, but that slot cannot be taken from.",
                     this.player,
                     this.recipeID,
                     actualSlot
                  );
               }
            } else {
               int actualSlotx = slot - 9 - Inventory.m_36059_();
               if (actualSlotx >= this.mainInventorySlots.size()) {
                  return this.fail(
                     "Received transfer request from: {}, for: {}, could not find main inventory slot: {}.", this.player, this.recipeID, actualSlotx
                  );
               }

               inventorySlot = this.mainInventorySlots.get(actualSlotx);
               if (!inventorySlot.m_8010_(this.player)) {
                  return this.fail(
                     "Received transfer request from: {}, for: {}, with a request to take from main inventory slot: {}, but that slot cannot be taken from.",
                     this.player,
                     this.recipeID,
                     actualSlotx
                  );
               }
            }

            slotData = inventorySlot.m_6657_()
               ? new QIOServerCraftingTransferHandler.SlotData(inventorySlot.m_7993_())
               : QIOServerCraftingTransferHandler.SlotData.EMPTY;
            this.availableItems.put(slot, slotData);
         }

         return this.addStackToRecipe(targetSlot, slotData, used, slot, currentStackSize);
      } else {
         return this.fail("Received transfer request from: {}, for: {}, with an invalid slot id: {}.", this.player, this.recipeID, slot);
      }
   }

   private int addStackToRecipe(byte targetSlot, QIOServerCraftingTransferHandler.ItemData slotData, int used, byte sourceSlot, int currentStackSize) {
      if (slotData.isEmpty()) {
         return sourceSlot == -1
            ? this.fail("Received transfer request from: {}, for: {}, for an item that isn't stored in the frequency.", this.player, this.recipeID)
            : this.fail("Received transfer request from: {}, for: {}, for an empty slot: {}.", this.player, this.recipeID, sourceSlot);
      } else {
         if (slotData.getAvailable() < used) {
            if (sourceSlot == -1) {
               Mekanism.logger
                  .warn(
                     "Received transfer request from: {}, for: {}, but the QIO frequency only had {} remaining items instead of the expected: {}. Attempting to continue by only using the available number of items.",
                     new Object[]{this.player, this.recipeID, slotData.getAvailable(), used}
                  );
            } else {
               Mekanism.logger
                  .warn(
                     "Received transfer request from: {}, for: {}, but slot: {} only had {} remaining items instead of the expected: {}. Attempting to continue by only using the available number of items.",
                     new Object[]{this.player, this.recipeID, sourceSlot, slotData.getAvailable(), used}
                  );
            }

            used = slotData.getAvailable();
         }

         ItemStack currentRecipeTarget = (ItemStack)this.recipeToTest.get(targetSlot);
         ItemStack slotStack = slotData.getStack();
         if (currentRecipeTarget.m_41619_()) {
            int max = slotStack.m_41741_();
            if (used > max) {
               Mekanism.logger
                  .warn(
                     "Received transfer request from: {}, for: {}, but the item being moved can only stack to: {} but a stack of size: {} was being moved. Attempting to continue by only using as many items as can be stacked.",
                     new Object[]{this.player, this.recipeID, max, used}
                  );
               used = max;
            }

            this.recipeToTest.set(targetSlot, slotStack.m_41777_());
         } else {
            if (!ItemHandlerHelper.canItemStacksStack(currentRecipeTarget, slotStack)) {
               Mekanism.logger
                  .debug(
                     "Received transfer request from: {}, for: {}, but found items for target slot: {} cannot stack. Attempting to continue by skipping the additional stack.",
                     new Object[]{this.player, this.recipeID, targetSlot}
                  );
               return 0;
            }

            int max = currentRecipeTarget.m_41741_();
            int needed = max - currentStackSize;
            if (used > needed) {
               Mekanism.logger
                  .warn(
                     "Received transfer request from: {}, for: {}, but moving the requested amount of: {} would cause the output stack to past its max stack size ({}). Attempting to continue by only using as many items as can be stacked.",
                     new Object[]{this.player, this.recipeID, used, max}
                  );
               used = needed;
            }
         }

         slotData.simulateUse(used);
         return used;
      }
   }

   private boolean hasRoomToShuffle() {
      Object2IntMap<HashedItem> leftOverInput = new Object2IntArrayMap(9);

      for (byte inputSlot = 0; inputSlot < 9; inputSlot++) {
         QIOServerCraftingTransferHandler.SlotData inputSlotData = (QIOServerCraftingTransferHandler.SlotData)this.availableItems.get(inputSlot);
         if (inputSlotData != null && inputSlotData.getAvailable() > 0) {
            leftOverInput.mergeInt(HashedItem.raw(inputSlotData.getStack()), inputSlotData.getAvailable(), Integer::sum);
         }
      }

      if (!leftOverInput.isEmpty()) {
         QIOCraftingTransferHelper.BaseSimulatedInventory simulatedInventory = new QIOCraftingTransferHelper.BaseSimulatedInventory(
            this.hotBarSlots, this.mainInventorySlots
         ) {
            @Override
            protected int getRemaining(int slot, ItemStack currentStored) {
               QIOServerCraftingTransferHandler.SlotData slotData = (QIOServerCraftingTransferHandler.SlotData)QIOServerCraftingTransferHandler.this.availableItems
                  .get((byte)(slot + 9));
               return slotData == null ? currentStored.m_41613_() : slotData.getAvailable();
            }
         };
         Object2IntMap<HashedItem> stillLeftOver = simulatedInventory.shuffleInputs(leftOverInput, this.frequency != null);
         if (stillLeftOver == null) {
            return false;
         }

         if (!stillLeftOver.isEmpty() && this.frequency != null) {
            int availableItemTypes = this.frequency.getTotalItemTypeCapacity() - this.frequency.getTotalItemTypes(false);
            long availableItemSpace = this.frequency.getTotalItemCountCapacity() - this.frequency.getTotalItemCount();

            for (QIOServerCraftingTransferHandler.FrequencySlotData slotData : this.frequencyAvailableItems.values()) {
               availableItemSpace += slotData.getUsed();
               if (slotData.getAvailable() == 0) {
                  availableItemTypes++;
               }
            }

            ObjectIterator var18 = stillLeftOver.object2IntEntrySet().iterator();

            while (var18.hasNext()) {
               it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<HashedItem> entry = (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<HashedItem>)var18.next();
               availableItemSpace -= entry.getIntValue();
               if (availableItemSpace <= 0L) {
                  return false;
               }

               if (this.frequency.isStoring((HashedItem)entry.getKey())) {
                  UUID uuid = QIOGlobalItemLookup.INSTANCE.getUUIDForType((HashedItem)entry.getKey());
                  QIOServerCraftingTransferHandler.FrequencySlotData slotDatax = this.frequencyAvailableItems.get(uuid);
                  if (slotDatax != null && slotDatax.getAvailable() == 0) {
                     if (--availableItemTypes <= 0) {
                        return false;
                     }
                  }
               } else if (--availableItemTypes <= 0) {
                  return false;
               }
            }

            Collection<QIODriveData> drives = this.frequency.getAllDrives();
            List<QIOServerCraftingTransferHandler.SimulatedQIODrive> simulatedDrives = new ArrayList<>(drives.size());

            for (QIODriveData drive : drives) {
               simulatedDrives.add(new QIOServerCraftingTransferHandler.SimulatedQIODrive(drive));
            }

            for (java.util.Map.Entry<UUID, QIOServerCraftingTransferHandler.FrequencySlotData> entryx : this.frequencyAvailableItems.entrySet()) {
               QIOServerCraftingTransferHandler.FrequencySlotData slotDatax = entryx.getValue();
               HashedItem type = slotDatax.getType();
               if (type != null) {
                  int toRemove = slotDatax.getUsed();

                  for (QIOServerCraftingTransferHandler.SimulatedQIODrive drive : simulatedDrives) {
                     toRemove = drive.remove(type, toRemove);
                     if (toRemove == 0) {
                        break;
                     }
                  }
               }
            }

            ObjectIterator var24 = stillLeftOver.object2IntEntrySet().iterator();

            while (var24.hasNext()) {
               it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<HashedItem> entryxx = (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<HashedItem>)var24.next();
               HashedItem item = (HashedItem)entryxx.getKey();
               int toAdd = entryxx.getIntValue();

               for (QIOServerCraftingTransferHandler.SimulatedQIODrive drivex : simulatedDrives) {
                  toAdd = drivex.add(item, toAdd, true);
                  if (toAdd == 0) {
                     break;
                  }
               }

               if (toAdd > 0) {
                  for (QIOServerCraftingTransferHandler.SimulatedQIODrive drivexx : simulatedDrives) {
                     toAdd = drivexx.add(item, toAdd, false);
                     if (toAdd == 0) {
                        break;
                     }
                  }

                  if (toAdd > 0) {
                     return false;
                  }
               }
            }
         }
      }

      return true;
   }

   private void transferItems(Byte2ObjectMap<List<QIOCraftingTransferHelper.SingularHashedItemSource>> sources) {
      SelectedWindowData windowData = this.craftingWindow.getWindowData();
      Byte2ObjectMap<ItemStack> targetContents = new Byte2ObjectArrayMap(sources.size());
      ObjectIterator remainingCraftingGridContents = sources.byte2ObjectEntrySet().iterator();

      while (remainingCraftingGridContents.hasNext()) {
         Entry<List<QIOCraftingTransferHelper.SingularHashedItemSource>> entry = (Entry<List<QIOCraftingTransferHelper.SingularHashedItemSource>>)remainingCraftingGridContents.next();

         for (QIOCraftingTransferHelper.SingularHashedItemSource source : (List)entry.getValue()) {
            byte slot = source.getSlot();
            ItemStack stack;
            if (slot == -1) {
               UUID qioSource = source.getQioSource();
               HashedItem storedItem = QIOGlobalItemLookup.INSTANCE.getTypeByUUID(qioSource);
               if (storedItem == null) {
                  this.bail(
                     targetContents, "Received transfer request from: {}, for: {}, for item with unknown UUID: {}.", this.player, this.recipeID, qioSource
                  );
                  return;
               }

               if (!this.frequency.isStoring(storedItem)) {
                  this.bail(
                     targetContents,
                     "Received transfer request from: {}, for: {}, could not find stored item with UUID: {}. This likely means that more of it was requested than is stored.",
                     this.player,
                     this.recipeID,
                     qioSource
                  );
                  return;
               }

               stack = this.frequency.removeByType(storedItem, source.getUsed());
               if (stack.m_41619_()) {
                  this.bail(
                     targetContents,
                     "Received transfer request from: {}, for: {}, but could not extract item: {} with nbt: {} from the QIO.",
                     this.player,
                     this.recipeID,
                     storedItem.getItem(),
                     storedItem.getInternalTag()
                  );
                  return;
               }

               if (stack.m_41613_() < source.getUsed()) {
                  Mekanism.logger
                     .warn(
                        "Received transfer request from: {}, for: {}, but was unable to extract the expected amount: {} of item: {} with nbt: {} from the QIO. This should not be possible as it should have been caught during simulation. Attempting to continue anyways with the actual extracted amount of {}.",
                        new Object[]{this.player, this.recipeID, source.getUsed(), storedItem.getItem(), storedItem.getInternalTag(), stack.m_41613_()}
                     );
               }
            } else {
               int actualSlot;
               String slotType;
               if (slot < 9) {
                  actualSlot = slot;
                  slotType = "crafting window";
                  stack = this.craftingWindow.getInputSlot(slot).extractItem(source.getUsed(), Action.EXECUTE, AutomationType.MANUAL);
               } else if (slot < 9 + Inventory.m_36059_()) {
                  actualSlot = slot - 9;
                  slotType = "hotbar";
                  stack = this.hotBarSlots.get(actualSlot).m_6201_(source.getUsed());
               } else {
                  actualSlot = slot - 9 - Inventory.m_36059_();
                  slotType = "main inventory";
                  stack = this.mainInventorySlots.get(actualSlot).m_6201_(source.getUsed());
               }

               if (stack.m_41619_()) {
                  this.bail(
                     targetContents,
                     "Received transfer request from: {}, for: {}, could not extract item from {} slot: {}. This likely means that more of it was requested than is stored.",
                     this.player,
                     this.recipeID,
                     slotType,
                     actualSlot
                  );
                  return;
               }

               if (stack.m_41613_() < source.getUsed()) {
                  Mekanism.logger
                     .warn(
                        "Received transfer request from: {}, for: {}, but was unable to extract the expected amount: {} from {} slot: {}. This should not be possible as it should have been caught during simulation. Attempting to continue anyways with the actual extracted amount of {}.",
                        new Object[]{this.player, this.recipeID, source.getUsed(), slotType, actualSlot, stack.m_41613_()}
                     );
               }
            }

            byte targetSlot = entry.getByteKey();
            if (targetContents.containsKey(targetSlot)) {
               ItemStack existing = (ItemStack)targetContents.get(targetSlot);
               if (ItemHandlerHelper.canItemStacksStack(existing, stack)) {
                  int needed = existing.m_41741_() - existing.m_41613_();
                  if (stack.m_41613_() <= needed) {
                     existing.m_41769_(stack.m_41613_());
                  } else {
                     existing.m_41769_(needed);
                     stack.m_41774_(needed);
                     Mekanism.logger
                        .warn(
                           "Received transfer request from: {}, for: {}, but contents could not fully fit into target slot: {}. This should not be able to happen, returning excess stack, and attempting to continue.",
                           new Object[]{this.player, this.recipeID, targetSlot}
                        );
                     this.returnItem(stack, windowData);
                  }
               } else {
                  Mekanism.logger
                     .warn(
                        "Received transfer request from: {}, for: {}, but contents could not stack into target slot: {}. This should not be able to happen, returning extra stack, and attempting to continue.",
                        new Object[]{this.player, this.recipeID, targetSlot}
                     );
                  this.returnItem(stack, windowData);
               }
            } else {
               targetContents.put(targetSlot, stack);
            }
         }
      }

      Byte2ObjectMap<ItemStack> remainingCraftingGridContentsx = new Byte2ObjectArrayMap(9);

      for (byte slotx = 0; slotx < 9; slotx++) {
         CraftingWindowInventorySlot inputSlot = this.craftingWindow.getInputSlot(slotx);
         if (!inputSlot.isEmpty()) {
            ItemStack stackx = inputSlot.extractItem(inputSlot.getCount(), Action.EXECUTE, AutomationType.MANUAL);
            if (stackx.m_41619_()) {
               this.bail(
                  targetContents,
                  remainingCraftingGridContentsx,
                  "Received transfer request from: {}, for: {}, but failed to remove items from crafting input slot: {}. This should not be possible as it should have been caught by an earlier check.",
                  this.player,
                  this.recipeID,
                  slotx
               );
               return;
            }

            remainingCraftingGridContentsx.put(slotx, stackx);
         }
      }

      ObjectIterator<Entry<ItemStack>> iter = targetContents.byte2ObjectEntrySet().iterator();

      while (iter.hasNext()) {
         Entry<ItemStack> entry = (Entry<ItemStack>)iter.next();
         byte targetSlot = entry.getByteKey();
         CraftingWindowInventorySlot inputSlot = this.craftingWindow.getInputSlot(targetSlot);
         ItemStack remainder = inputSlot.insertItem((ItemStack)entry.getValue(), Action.EXECUTE, AutomationType.MANUAL);
         if (remainder.m_41619_()) {
            iter.remove();
         } else {
            targetContents.put(targetSlot, remainder);
            Mekanism.logger
               .warn(
                  "Received transfer request from: {}, for: {}, but was unable to fully insert it into the {} crafting input slot. This should not be possible as it should have been caught during simulation. Attempting to continue anyways.",
                  new Object[]{this.player, this.recipeID, targetSlot}
               );
         }
      }

      iter = remainingCraftingGridContentsx.byte2ObjectEntrySet().iterator();

      while (iter.hasNext()) {
         Entry<ItemStack> entry = (Entry<ItemStack>)iter.next();
         ItemStack stackx = this.returnItemToInventory((ItemStack)entry.getValue(), windowData);
         if (!stackx.m_41619_()) {
            CraftingWindowInventorySlot inputSlot = this.craftingWindow.getInputSlot(entry.getByteKey());
            if (ItemHandlerHelper.canItemStacksStack(inputSlot.getStack(), stackx)) {
               stackx = inputSlot.insertItem(stackx, Action.EXECUTE, AutomationType.MANUAL);
            }

            if (!stackx.m_41619_()) {
               if (this.frequency != null) {
                  stackx = this.frequency.addItem(stackx);
               }

               if (!stackx.m_41619_()) {
                  this.player.m_36176_(stackx, false);
                  Mekanism.logger
                     .warn(
                        "Received transfer request from: {}, for: {}, and was unable to fit all contents that were in the crafting window into the player's inventory/QIO system; dropping items by player.",
                        this.player,
                        this.recipeID
                     );
               }
            }
         }
      }

      if (!targetContents.isEmpty()) {
         this.bail(
            targetContents,
            "Received transfer request from: {}, for: {}, but ended up with {} items that could not be transferred into the proper crafting grid slot. This should not be possible as it should have been caught during simulation.",
            this.player,
            this.recipeID,
            targetContents.size()
         );
      }
   }

   private void bail(Byte2ObjectMap<ItemStack> targetContents, String format, Object... args) {
      this.bail(targetContents, Byte2ObjectMaps.emptyMap(), format, args);
   }

   private void bail(Byte2ObjectMap<ItemStack> targetContents, Byte2ObjectMap<ItemStack> remainingCraftingGridContents, String format, Object... args) {
      Mekanism.logger.warn(format, args);
      SelectedWindowData windowData = this.craftingWindow.getWindowData();
      ObjectIterator var6 = targetContents.values().iterator();

      while (var6.hasNext()) {
         ItemStack stack = (ItemStack)var6.next();
         this.returnItem(stack, windowData);
      }

      var6 = remainingCraftingGridContents.byte2ObjectEntrySet().iterator();

      while (var6.hasNext()) {
         Entry<ItemStack> entry = (Entry<ItemStack>)var6.next();
         ItemStack stack = (ItemStack)entry.getValue();
         CraftingWindowInventorySlot inputSlot = this.craftingWindow.getInputSlot(entry.getByteKey());
         if (ItemHandlerHelper.canItemStacksStack(inputSlot.getStack(), stack)) {
            stack = inputSlot.insertItem(stack, Action.EXECUTE, AutomationType.MANUAL);
            if (stack.m_41619_()) {
               continue;
            }
         }

         this.returnItem(stack, windowData);
      }
   }

   private void returnItem(ItemStack stack, @Nullable SelectedWindowData windowData) {
      stack = this.returnItemToInventory(stack, windowData);
      if (!stack.m_41619_()) {
         if (this.frequency != null) {
            stack = this.frequency.addItem(stack);
         }

         if (!stack.m_41619_()) {
            this.player.m_36176_(stack, false);
         }
      }
   }

   private ItemStack returnItemToInventory(ItemStack stack, @Nullable SelectedWindowData windowData) {
      stack = MekanismContainer.insertItem(this.hotBarSlots, stack, true, windowData);
      stack = MekanismContainer.insertItem(this.mainInventorySlots, stack, true, windowData);
      stack = MekanismContainer.insertItem(this.hotBarSlots, stack, false, windowData);
      return MekanismContainer.insertItem(this.mainInventorySlots, stack, false, windowData);
   }

   private int fail(String format, Object... args) {
      Mekanism.logger.warn(format, args);
      return -1;
   }

   private static class FrequencySlotData extends QIOServerCraftingTransferHandler.ItemData {
      public static final QIOServerCraftingTransferHandler.FrequencySlotData EMPTY = new QIOServerCraftingTransferHandler.FrequencySlotData(null, 0L);
      private final HashedItem type;
      private int used;

      public FrequencySlotData(HashedItem type, long stored) {
         super(MathUtils.clampToInt(stored));
         this.type = type;
      }

      @Override
      public boolean isEmpty() {
         return this == EMPTY || this.type == null;
      }

      @Override
      public ItemStack getStack() {
         return this.type == null ? ItemStack.f_41583_ : this.type.getInternalStack();
      }

      @Override
      public void simulateUse(int used) {
         super.simulateUse(used);
         this.used += used;
      }

      public int getUsed() {
         return this.used;
      }

      public HashedItem getType() {
         return this.type;
      }
   }

   private abstract static class ItemData {
      private int available;

      protected ItemData(int available) {
         this.available = available;
      }

      public abstract boolean isEmpty();

      public int getAvailable() {
         return this.available;
      }

      public void simulateUse(int used) {
         this.available -= used;
      }

      protected abstract ItemStack getStack();
   }

   private static class SimulatedQIODrive {
      private final Object2LongMap<HashedItem> sourceItemMap;
      private Set<HashedItem> removedTypes;
      private int availableItemTypes;
      private long availableItemSpace;

      public SimulatedQIODrive(QIODriveData sourceDrive) {
         this.sourceItemMap = sourceDrive.getItemMap();
         this.availableItemSpace = sourceDrive.getCountCapacity() - sourceDrive.getTotalCount();
         this.availableItemTypes = sourceDrive.getTypeCapacity() - sourceDrive.getTotalTypes();
      }

      public int remove(HashedItem item, int count) {
         long stored = this.sourceItemMap.getOrDefault(item, 0L);
         if (stored == 0L) {
            return count;
         } else if (stored <= count) {
            if (this.removedTypes == null) {
               this.removedTypes = new HashSet<>();
            }

            this.removedTypes.add(item);
            this.availableItemTypes++;
            this.availableItemSpace += stored;
            return count - (int)stored;
         } else {
            this.availableItemSpace += count;
            return 0;
         }
      }

      public int add(HashedItem item, int count, boolean mustContain) {
         if (this.availableItemSpace == 0L) {
            return count;
         } else {
            boolean contains = this.sourceItemMap.containsKey(item) && (this.removedTypes == null || !this.removedTypes.contains(item));
            if (mustContain != contains) {
               return count;
            } else {
               if (!contains) {
                  if (this.availableItemTypes == 0) {
                     return count;
                  }

                  this.availableItemTypes--;
               }

               if (count < this.availableItemSpace) {
                  this.availableItemSpace -= count;
                  return 0;
               } else {
                  count -= (int)this.availableItemSpace;
                  this.availableItemSpace = 0L;
                  return count;
               }
            }
         }
      }
   }

   private static class SlotData extends QIOServerCraftingTransferHandler.ItemData {
      public static final QIOServerCraftingTransferHandler.SlotData EMPTY = new QIOServerCraftingTransferHandler.SlotData(ItemStack.f_41583_, 0);
      private final ItemStack stack;

      public SlotData(ItemStack stack) {
         this(stack, stack.m_41613_());
      }

      protected SlotData(ItemStack stack, int available) {
         super(available);
         this.stack = stack;
      }

      @Override
      public boolean isEmpty() {
         return this == EMPTY || this.stack.m_41619_();
      }

      @Override
      public ItemStack getStack() {
         return this.stack;
      }
   }
}
