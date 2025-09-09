package mekanism.common.inventory;

import java.util.Collections;
import java.util.List;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.inventory.slot.BinInventorySlot;
import mekanism.common.item.block.ItemBlockBin;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BinMekanismInventory extends ItemStackMekanismInventory {
   private BinInventorySlot binSlot;

   private BinMekanismInventory(@NotNull ItemStack stack) {
      super(stack);
   }

   @NotNull
   @Override
   protected List<IInventorySlot> getInitialInventory() {
      this.binSlot = BinInventorySlot.create(this, ((ItemBlockBin)this.stack.m_41720_()).getTier());
      return Collections.singletonList(this.binSlot);
   }

   @Nullable
   public static BinMekanismInventory create(@NotNull ItemStack stack) {
      return !stack.m_41619_() && stack.m_41720_() instanceof ItemBlockBin ? new BinMekanismInventory(stack) : null;
   }

   public BinInventorySlot getBinSlot() {
      return this.binSlot;
   }
}
