package mekanism.common.capabilities.item;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.LongSupplier;
import mekanism.api.Coord4D;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.lib.inventory.TransitRequest;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class CursedTransporterItemHandler implements IItemHandler {
   private final Map<Coord4D, Set<TransporterStack>> simulatedFlowingStacks = new Object2ObjectOpenHashMap();
   private final Set<ItemStack> seenStacks = Collections.newSetFromMap(new IdentityHashMap<>());
   private final Set<ItemStack> seenExecutedStacks = Collections.newSetFromMap(new IdentityHashMap<>());
   private final LogisticalTransporterBase transporter;
   private final LongSupplier currentTickSupplier;
   private final BlockPos fromPos;
   private long lastTick;

   public CursedTransporterItemHandler(LogisticalTransporterBase transporter, BlockPos fromPos, LongSupplier currentTickSupplier) {
      this.transporter = transporter;
      this.fromPos = fromPos;
      this.currentTickSupplier = currentTickSupplier;
   }

   public int getSlots() {
      return 9;
   }

   @NotNull
   public ItemStack getStackInSlot(int slot) {
      return ItemStack.f_41583_;
   }

   private TransitRequest getRequest(int limit, ItemStack stack) {
      return stack.m_41613_() <= limit ? TransitRequest.simple(stack) : TransitRequest.simple(stack.m_255036_(limit));
   }

   @NotNull
   public ItemStack insertItem(int slot, @NotNull ItemStack itemStack, boolean simulate) {
      if (!itemStack.m_41619_() && this.transporter.hasTransmitterNetwork()) {
         long currentTick = this.currentTickSupplier.getAsLong();
         if (currentTick != this.lastTick) {
            this.seenStacks.clear();
            this.seenExecutedStacks.clear();
            this.simulatedFlowingStacks.clear();
            this.lastTick = currentTick;
         }

         int limit = this.getSlotLimit(slot);
         TransitRequest.TransitResponse response;
         if (simulate) {
            if (this.seenExecutedStacks.contains(itemStack) || !this.seenStacks.add(itemStack)) {
               return itemStack;
            }

            TransitRequest request = this.getRequest(limit, itemStack);
            TransporterStack stack = this.transporter.createInsertStack(this.fromPos, this.transporter.getColor());
            response = stack.recalculatePath(request, this.transporter, 1, this.simulatedFlowingStacks);
            if (response.isEmpty()) {
               return itemStack;
            }

            stack.itemStack = response.getStack();
            if (stack.getPathType() != TransporterStack.Path.NONE) {
               this.simulatedFlowingStacks
                  .computeIfAbsent(new Coord4D(stack.getDest(), this.transporter.getTileWorld()), k -> new ObjectOpenHashSet())
                  .add(stack);
            }
         } else {
            if (!this.seenExecutedStacks.add(itemStack)) {
               return itemStack;
            }

            this.seenStacks.clear();
            this.simulatedFlowingStacks.clear();
            TransitRequest requestx = this.getRequest(limit, itemStack);
            response = this.transporter.insertUnchecked(this.fromPos, requestx, this.transporter.getColor(), true, 1);
            if (response.isEmpty()) {
               return itemStack;
            }
         }

         ItemStack remainder = response.getRejected();
         if (itemStack.m_41613_() > limit) {
            int extra = itemStack.m_41613_() - limit;
            if (remainder.m_41619_()) {
               remainder = itemStack.m_255036_(extra);
            } else {
               remainder.m_41769_(extra);
            }
         }

         if (!remainder.m_41619_()) {
            if (simulate) {
               this.seenStacks.add(remainder);
            } else {
               this.seenExecutedStacks.add(remainder);
            }
         }

         return remainder;
      } else {
         return itemStack;
      }
   }

   @NotNull
   public ItemStack extractItem(int slot, int amount, boolean simulate) {
      return ItemStack.f_41583_;
   }

   public int getSlotLimit(int slot) {
      return this.transporter.tier.getPullAmount();
   }

   public boolean isItemValid(int slot, @NotNull ItemStack stack) {
      return true;
   }
}
