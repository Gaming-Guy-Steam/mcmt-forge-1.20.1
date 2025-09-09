package mekanism.common.inventory;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.content.qio.IQIOCraftingWindowHolder;
import mekanism.common.content.qio.QIOCraftingWindow;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.lib.frequency.IFrequencyItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PortableQIODashboardInventory extends ItemStackMekanismInventory implements IQIOCraftingWindowHolder {
   @Nullable
   private final Level world;
   private QIOCraftingWindow[] craftingWindows;

   public PortableQIODashboardInventory(ItemStack stack, @NotNull Inventory inv) {
      super(stack);
      this.world = inv.f_35978_.m_9236_();

      for (QIOCraftingWindow craftingWindow : this.craftingWindows) {
         craftingWindow.invalidateRecipe();
      }
   }

   @Override
   protected List<IInventorySlot> getInitialInventory() {
      List<IInventorySlot> slots = new ArrayList<>();
      this.craftingWindows = new QIOCraftingWindow[3];

      for (byte tableIndex = 0; tableIndex < this.craftingWindows.length; tableIndex++) {
         QIOCraftingWindow craftingWindow = new QIOCraftingWindow(this, tableIndex);
         this.craftingWindows[tableIndex] = craftingWindow;

         for (int slot = 0; slot < 9; slot++) {
            slots.add(craftingWindow.getInputSlot(slot));
         }

         slots.add(craftingWindow.getOutputSlot());
      }

      return slots;
   }

   @Nullable
   @Override
   public Level getHolderWorld() {
      return this.world;
   }

   @Override
   public QIOCraftingWindow[] getCraftingWindows() {
      return this.craftingWindows;
   }

   @Nullable
   @Override
   public QIOFrequency getFrequency() {
      if (this.world != null && !this.world.m_5776_()) {
         IFrequencyItem frequencyItem = (IFrequencyItem)this.stack.m_41720_();
         if (frequencyItem.hasFrequency(this.stack)) {
            if (frequencyItem.getFrequency(this.stack) instanceof QIOFrequency freq) {
               return freq;
            }

            frequencyItem.setFrequency(this.stack, null);
         }
      }

      return null;
   }
}
