package mekanism.common.lib.inventory;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import mekanism.common.Mekanism;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.RegistryUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;

public class TileTransitRequest extends TransitRequest {
   private final BlockEntity tile;
   private final Direction side;
   private final Map<HashedItem, TileTransitRequest.TileItemData> itemMap = new LinkedHashMap<>();

   public TileTransitRequest(BlockEntity tile, Direction side) {
      this.tile = tile;
      this.side = side;
   }

   public void addItem(ItemStack stack, int slot) {
      HashedItem hashed = HashedItem.create(stack);
      this.itemMap.computeIfAbsent(hashed, x$0 -> new TileTransitRequest.TileItemData(x$0)).addSlot(slot, stack);
   }

   public int getCount(HashedItem itemType) {
      TransitRequest.ItemData data = this.itemMap.get(itemType);
      return data == null ? 0 : data.getTotalCount();
   }

   protected Direction getSide() {
      return this.side;
   }

   public Map<HashedItem, TileTransitRequest.TileItemData> getItemMap() {
      return this.itemMap;
   }

   @Override
   public Collection<TileTransitRequest.TileItemData> getItemData() {
      return this.itemMap.values();
   }

   public class TileItemData extends TransitRequest.ItemData {
      private final Int2IntMap slotMap = new Int2IntOpenHashMap();

      public TileItemData(HashedItem itemType) {
         super(itemType);
      }

      public void addSlot(int id, ItemStack stack) {
         this.slotMap.put(id, stack.m_41613_());
         this.totalCount = this.totalCount + stack.m_41613_();
      }

      @Override
      public ItemStack use(int amount) {
         Direction side = TileTransitRequest.this.getSide();
         IItemHandler handler = InventoryUtils.assertItemHandler("TileTransitRequest", TileTransitRequest.this.tile, side);
         if (handler != null && !this.slotMap.isEmpty()) {
            HashedItem itemType = this.getItemType();
            ItemStack itemStack = itemType.getInternalStack();
            ObjectIterator<Entry> iterator = this.slotMap.int2IntEntrySet().iterator();

            while (iterator.hasNext()) {
               Entry entry = (Entry)iterator.next();
               int slot = entry.getIntKey();
               int currentCount = entry.getIntValue();
               int toUse = Math.min(amount, currentCount);
               ItemStack ret = handler.extractItem(slot, toUse, false);
               boolean stackable = InventoryUtils.areItemsStackable(itemStack, ret);
               if (!stackable || ret.m_41613_() != toUse) {
                  Mekanism.logger
                     .warn("An inventory's returned content {} does not line up with TileTransitRequest's prediction.", stackable ? "count" : "type");
                  Mekanism.logger.warn("TileTransitRequest item: {}, toUse: {}, ret: {}, slot: {}", new Object[]{itemStack, toUse, ret, slot});
                  Mekanism.logger
                     .warn(
                        "Tile: {} {} {}",
                        new Object[]{RegistryUtils.getName(TileTransitRequest.this.tile.m_58903_()), TileTransitRequest.this.tile.m_58899_(), side}
                     );
               }

               amount -= toUse;
               this.totalCount -= toUse;
               if (this.totalCount == 0) {
                  TileTransitRequest.this.itemMap.remove(itemType);
               }

               currentCount -= toUse;
               if (currentCount == 0) {
                  iterator.remove();
               } else {
                  entry.setValue(currentCount);
               }

               if (amount == 0) {
                  break;
               }
            }
         }

         return this.getStack();
      }
   }
}
