package mekanism.common.content.transporter;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import mekanism.api.Coord4D;
import mekanism.common.lib.inventory.TransitRequest;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;

public class TransporterManager {
   private static final Map<Coord4D, Set<TransporterStack>> flowingStacks = new Object2ObjectOpenHashMap();

   private TransporterManager() {
   }

   public static void reset() {
      flowingStacks.clear();
   }

   public static void add(Level world, TransporterStack stack) {
      flowingStacks.computeIfAbsent(new Coord4D(stack.getDest(), world), k -> new ObjectOpenHashSet()).add(stack);
   }

   public static void remove(Level world, TransporterStack stack) {
      if (stack.hasPath() && stack.getPathType() != TransporterStack.Path.NONE) {
         flowingStacks.get(new Coord4D(stack.getDest(), world)).remove(stack);
      }
   }

   public static boolean didEmit(ItemStack stack, ItemStack returned) {
      return returned.m_41619_() || returned.m_41613_() < stack.m_41613_();
   }

   public static ItemStack getToUse(ItemStack stack, ItemStack returned) {
      return returned.m_41619_() ? stack : StackUtils.size(stack, stack.m_41613_() - returned.m_41613_());
   }

   private static int simulateInsert(IItemHandler handler, TransporterManager.InventoryInfo inventoryInfo, ItemStack stack, int count, boolean inFlight) {
      int maxStackSize = stack.m_41741_();

      for (int slot = 0; slot < inventoryInfo.slots && count != 0; slot++) {
         int max = inventoryInfo.getSlotLimit(handler, slot);
         if (max != 0 && handler.isItemValid(slot, stack)) {
            int destCount = inventoryInfo.stackSizes[slot];
            int mergedCount = count + destCount;
            int toAccept = count;
            boolean needsSimulation = false;
            if (destCount > 0) {
               if (destCount >= max || !InventoryUtils.areItemsStackable(inventoryInfo.inventory[slot], stack)) {
                  continue;
               }

               if (max > maxStackSize && mergedCount > maxStackSize) {
                  needsSimulation = true;
                  if (count <= maxStackSize) {
                     if (stack.m_41613_() <= maxStackSize) {
                        stack = stack.m_255036_(maxStackSize + 1);
                     }

                     toAccept = stack.m_41613_();
                  } else if (stack.m_41613_() <= maxStackSize) {
                     stack = stack.m_255036_(count);
                  }
               } else if (!inFlight) {
                  needsSimulation = true;
               }
            } else {
               needsSimulation = true;
            }

            if (needsSimulation) {
               ItemStack simulatedRemainder = handler.insertItem(slot, stack, true);
               int accepted = stack.m_41613_() - simulatedRemainder.m_41613_();
               if (accepted == 0) {
                  continue;
               }

               if (accepted < toAccept) {
                  max = inventoryInfo.actualStackSizes[slot] + accepted;
               }

               if (destCount == 0) {
                  inventoryInfo.inventory[slot] = stack;
               }
            }

            if (mergedCount <= max) {
               inventoryInfo.stackSizes[slot] = mergedCount;
               return 0;
            }

            inventoryInfo.stackSizes[slot] = max;
            count = mergedCount - max;
         }
      }

      return count;
   }

   public static TransitRequest.TransitResponse getPredictedInsert(
      Coord4D position, Direction side, IItemHandler handler, TransitRequest request, Map<Coord4D, Set<TransporterStack>> additionalFlowingStacks
   ) {
      TransporterManager.InventoryInfo inventoryInfo = new TransporterManager.InventoryInfo(handler);
      return predictFlowing(position, side, handler, inventoryInfo, flowingStacks)
            && predictFlowing(position, side, handler, inventoryInfo, additionalFlowingStacks)
         ? getPredictedInsert(inventoryInfo, handler, request)
         : request.getEmptyResponse();
   }

   private static boolean predictFlowing(
      Coord4D position, Direction side, IItemHandler handler, TransporterManager.InventoryInfo inventoryInfo, Map<Coord4D, Set<TransporterStack>> flowingStacks
   ) {
      Set<TransporterStack> transporterStacks = flowingStacks.get(position);
      if (transporterStacks != null) {
         for (TransporterStack stack : transporterStacks) {
            if (stack != null && stack.getPathType() != TransporterStack.Path.NONE) {
               int numLeftOver = simulateInsert(handler, inventoryInfo, stack.itemStack, stack.itemStack.m_41613_(), true);
               if (numLeftOver > 0 && (numLeftOver != stack.itemStack.m_41613_() || side == stack.getSideOfDest())) {
                  return false;
               }
            }
         }
      }

      return true;
   }

   private static TransitRequest.TransitResponse getPredictedInsert(
      TransporterManager.InventoryInfo inventoryInfo, IItemHandler handler, TransitRequest request
   ) {
      for (TransitRequest.ItemData data : request.getItemData()) {
         ItemStack stack = data.getStack();
         int numToSend = data.getTotalCount();
         int numLeftOver = simulateInsert(handler, inventoryInfo, stack, numToSend, false);
         if (numLeftOver != numToSend) {
            return request.createResponse(StackUtils.size(stack, numToSend - numLeftOver), data);
         }
      }

      return request.getEmptyResponse();
   }

   public static TransitRequest.TransitResponse getPredictedInsert(IItemHandler handler, TransitRequest request) {
      return getPredictedInsert(new TransporterManager.InventoryInfo(handler), handler, request);
   }

   private static class InventoryInfo {
      private final ItemStack[] inventory;
      private final int[] stackSizes;
      private final int[] actualStackSizes;
      private final int[] slotLimits;
      private final int slots;

      public InventoryInfo(IItemHandler handler) {
         this.slots = handler.getSlots();
         this.inventory = new ItemStack[this.slots];
         this.stackSizes = new int[this.slots];
         this.actualStackSizes = new int[this.slots];
         this.slotLimits = new int[this.slots];
         Arrays.fill(this.slotLimits, -1);

         for (int i = 0; i < this.slots; i++) {
            ItemStack stack = handler.getStackInSlot(i);
            this.inventory[i] = stack;
            this.actualStackSizes[i] = this.stackSizes[i] = stack.m_41613_();
         }
      }

      public int getSlotLimit(IItemHandler handler, int slot) {
         int limit = this.slotLimits[slot];
         return limit == -1 ? (this.slotLimits[slot] = handler.getSlotLimit(slot)) : limit;
      }
   }
}
