package mekanism.common.inventory;

import java.util.UUID;
import java.util.function.Supplier;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.util.MekanismUtils;
import net.minecraft.world.item.ItemStack;

public interface ISlotClickHandler {
   void onClick(Supplier<ISlotClickHandler.IScrollableSlot> slotProvider, int button, boolean hasShiftDown, ItemStack heldItem);

   public interface IScrollableSlot {
      HashedItem item();

      UUID itemUUID();

      long count();

      default String getDisplayName() {
         return this.item().getInternalStack().m_41786_().getString();
      }

      default String getModID() {
         return MekanismUtils.getModId(this.item().getInternalStack());
      }
   }
}
