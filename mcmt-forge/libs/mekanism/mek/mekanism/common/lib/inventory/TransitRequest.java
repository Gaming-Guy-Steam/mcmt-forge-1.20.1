package mekanism.common.lib.inventory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import mekanism.common.Mekanism;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public abstract class TransitRequest {
   private final TransitRequest.TransitResponse EMPTY = new TransitRequest.TransitResponse(ItemStack.f_41583_, null);

   public static TransitRequest.SimpleTransitRequest simple(ItemStack stack) {
      return new TransitRequest.SimpleTransitRequest(stack);
   }

   public static TransitRequest anyItem(BlockEntity tile, Direction side, int amount) {
      return definedItem(tile, side, amount, Finder.ANY);
   }

   public static TransitRequest definedItem(BlockEntity tile, Direction side, int amount, Finder finder) {
      return definedItem(tile, side, 1, amount, finder);
   }

   public static TransitRequest definedItem(BlockEntity tile, Direction side, int min, int max, Finder finder) {
      TileTransitRequest ret = new TileTransitRequest(tile, side);
      IItemHandler inventory = InventoryUtils.assertItemHandler("TransitRequest", tile, side);
      if (inventory == null) {
         return ret;
      } else {
         for (int i = inventory.getSlots() - 1; i >= 0; i--) {
            ItemStack stack = inventory.extractItem(i, max, true);
            if (!stack.m_41619_() && finder.modifies(stack)) {
               HashedItem hashed = HashedItem.raw(stack);
               int toUse = Math.min(stack.m_41613_(), max - ret.getCount(hashed));
               if (toUse != 0) {
                  ret.addItem(StackUtils.size(stack, toUse), i);
               }
            }
         }

         ret.getItemMap().entrySet().removeIf(entry -> entry.getValue().getTotalCount() < min);
         return ret;
      }
   }

   public abstract Collection<? extends TransitRequest.ItemData> getItemData();

   @NotNull
   public TransitRequest.TransitResponse addToInventory(BlockEntity tile, Direction side, int min, boolean force) {
      if (force && tile instanceof TileEntityLogisticalSorter sorter) {
         return sorter.sendHome(this);
      } else if (this.isEmpty()) {
         return this.getEmptyResponse();
      } else {
         Optional<IItemHandler> capability = CapabilityUtils.getCapability(tile, ForgeCapabilities.ITEM_HANDLER, side.m_122424_()).resolve();
         if (capability.isPresent()) {
            IItemHandler inventory = capability.get();
            int slots = inventory.getSlots();
            if (slots == 0) {
               return this.getEmptyResponse();
            }

            if (min > 1) {
               TransitRequest.TransitResponse response = TransporterManager.getPredictedInsert(inventory, this);
               if (response.isEmpty() || response.getSendingAmount() < min) {
                  return this.getEmptyResponse();
               }
            }

            for (TransitRequest.ItemData data : this.getItemData()) {
               ItemStack origInsert = StackUtils.size(data.getStack(), data.getTotalCount());
               ItemStack toInsert = origInsert.m_41777_();

               for (int i = 0; i < slots; i++) {
                  toInsert = inventory.insertItem(i, toInsert, false);
                  if (toInsert.m_41619_()) {
                     return this.createResponse(origInsert, data);
                  }
               }

               if (TransporterManager.didEmit(origInsert, toInsert)) {
                  return this.createResponse(TransporterManager.getToUse(origInsert, toInsert), data);
               }
            }
         }

         return this.getEmptyResponse();
      }
   }

   public boolean isEmpty() {
      return this.getItemData().isEmpty();
   }

   @NotNull
   public TransitRequest.TransitResponse createResponse(ItemStack inserted, TransitRequest.ItemData data) {
      return new TransitRequest.TransitResponse(inserted, data);
   }

   public TransitRequest.TransitResponse createSimpleResponse() {
      TransitRequest.ItemData data = this.getItemData().stream().findFirst().orElse(null);
      return data == null ? this.getEmptyResponse() : this.createResponse(data.itemType.createStack(data.totalCount), data);
   }

   @NotNull
   public TransitRequest.TransitResponse getEmptyResponse() {
      return this.EMPTY;
   }

   public static class ItemData {
      private final HashedItem itemType;
      protected int totalCount;

      public ItemData(HashedItem itemType) {
         this.itemType = itemType;
      }

      public HashedItem getItemType() {
         return this.itemType;
      }

      public int getTotalCount() {
         return this.totalCount;
      }

      public ItemStack getStack() {
         return this.getItemType().createStack(this.getTotalCount());
      }

      public ItemStack use(int amount) {
         Mekanism.logger.error("Can't 'use' with this type of TransitResponse: {}", this.getClass().getName());
         return ItemStack.f_41583_;
      }

      @Override
      public boolean equals(Object o) {
         if (o == this) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            TransitRequest.ItemData itemData = (TransitRequest.ItemData)o;
            return this.totalCount == itemData.totalCount && this.itemType.equals(itemData.itemType);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.itemType, this.totalCount);
      }
   }

   public static class SimpleTransitRequest extends TransitRequest {
      private final List<TransitRequest.ItemData> slotData;

      protected SimpleTransitRequest(ItemStack stack) {
         this.slotData = Collections.singletonList(new TransitRequest.SimpleTransitRequest.SimpleItemData(stack));
      }

      @Override
      public Collection<TransitRequest.ItemData> getItemData() {
         return this.slotData;
      }

      public static class SimpleItemData extends TransitRequest.ItemData {
         public SimpleItemData(ItemStack stack) {
            super(HashedItem.create(stack));
            this.totalCount = stack.m_41613_();
         }
      }
   }

   public static class TransitResponse {
      private final ItemStack inserted;
      private final TransitRequest.ItemData slotData;

      public TransitResponse(@NotNull ItemStack inserted, TransitRequest.ItemData slotData) {
         this.inserted = inserted;
         this.slotData = slotData;
      }

      public int getSendingAmount() {
         return this.inserted.m_41613_();
      }

      public TransitRequest.ItemData getSlotData() {
         return this.slotData;
      }

      public ItemStack getStack() {
         return this.inserted;
      }

      public boolean isEmpty() {
         return this.inserted.m_41619_() || this.slotData.getTotalCount() == 0;
      }

      public ItemStack getRejected() {
         return this.isEmpty() ? ItemStack.f_41583_ : this.slotData.getItemType().createStack(this.slotData.getTotalCount() - this.getSendingAmount());
      }

      public ItemStack use(int amount) {
         return this.slotData.use(amount);
      }

      public ItemStack useAll() {
         return this.use(this.getSendingAmount());
      }

      @Override
      public boolean equals(Object o) {
         if (o == this) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            TransitRequest.TransitResponse other = (TransitRequest.TransitResponse)o;
            return (this.inserted == other.inserted || ItemStack.m_41728_(this.inserted, other.inserted)) && this.slotData.equals(other.slotData);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         int code = 1;
         code = 31 * code + this.inserted.m_41720_().hashCode();
         code = 31 * code + this.inserted.m_41613_();
         if (this.inserted.m_41782_()) {
            code = 31 * code + this.inserted.m_41783_().hashCode();
         }

         return 31 * code + this.slotData.hashCode();
      }
   }
}
