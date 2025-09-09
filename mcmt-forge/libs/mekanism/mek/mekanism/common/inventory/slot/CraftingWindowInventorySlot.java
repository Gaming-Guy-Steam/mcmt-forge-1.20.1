package mekanism.common.inventory.slot;

import java.util.function.BiPredicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.content.qio.QIOCraftingWindow;
import mekanism.common.inventory.container.slot.VirtualInventoryContainerSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class CraftingWindowInventorySlot extends BasicInventorySlot {
   protected final QIOCraftingWindow craftingWindow;
   @Nullable
   private final IContentsListener inputTypeChange;
   private ItemStack lastCurrent = ItemStack.f_41583_;
   private boolean wasEmpty = true;

   public static CraftingWindowInventorySlot input(QIOCraftingWindow window, @Nullable IContentsListener saveListener) {
      return new CraftingWindowInventorySlot(notExternal, alwaysTrueBi, window, saveListener, window);
   }

   protected CraftingWindowInventorySlot(
      BiPredicate<ItemStack, AutomationType> canExtract,
      BiPredicate<ItemStack, AutomationType> canInsert,
      QIOCraftingWindow craftingWindow,
      @Nullable IContentsListener saveListener,
      @Nullable IContentsListener inputTypeChange
   ) {
      super(canExtract, canInsert, alwaysTrue, saveListener, 0, 0);
      this.craftingWindow = craftingWindow;
      this.inputTypeChange = inputTypeChange;
   }

   @NotNull
   public VirtualInventoryContainerSlot createContainerSlot() {
      return new VirtualInventoryContainerSlot(this, this.craftingWindow.getWindowData(), this.getSlotOverlay(), this::setStackUnchecked);
   }

   @Override
   public void onContentsChanged() {
      super.onContentsChanged();
      if (this.inputTypeChange != null
         && (
            this.current.m_41619_() != this.wasEmpty
               || this.current != this.lastCurrent && !ItemHandlerHelper.canItemStacksStack(this.current, this.lastCurrent)
         )) {
         this.lastCurrent = this.current;
         this.wasEmpty = this.current.m_41619_();
         this.inputTypeChange.onContentsChanged();
      }
   }
}
