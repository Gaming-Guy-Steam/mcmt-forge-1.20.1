package mekanism.common.tile.qio;

import mekanism.api.IContentsListener;
import mekanism.common.CommonWorldTickHandler;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.content.qio.IQIOCraftingWindowHolder;
import mekanism.common.content.qio.QIOCraftingWindow;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityQIODashboard extends TileEntityQIOComponent implements IQIOCraftingWindowHolder {
   private QIOCraftingWindow[] craftingWindows;
   private boolean recipesChecked = false;

   public TileEntityQIODashboard(BlockPos pos, BlockState state) {
      super(MekanismBlocks.QIO_DASHBOARD, pos, state);
   }

   @Override
   protected void presetVariables() {
      super.presetVariables();
      this.craftingWindows = new QIOCraftingWindow[3];

      for (byte tableIndex = 0; tableIndex < this.craftingWindows.length; tableIndex++) {
         this.craftingWindows[tableIndex] = new QIOCraftingWindow(this, tableIndex);
      }
   }

   @NotNull
   @Override
   protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
      InventorySlotHelper builder = InventorySlotHelper.readOnly();

      for (QIOCraftingWindow craftingWindow : this.craftingWindows) {
         for (int slot = 0; slot < 9; slot++) {
            builder.addSlot(craftingWindow.getInputSlot(slot));
         }

         builder.addSlot(craftingWindow.getOutputSlot());
      }

      return builder.build();
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      if (CommonWorldTickHandler.flushTagAndRecipeCaches || !this.recipesChecked) {
         this.recipesChecked = true;

         for (QIOCraftingWindow craftingWindow : this.craftingWindows) {
            craftingWindow.invalidateRecipe();
         }
      }
   }

   @Nullable
   @Override
   public Level getHolderWorld() {
      return this.f_58857_;
   }

   @Override
   public QIOCraftingWindow[] getCraftingWindows() {
      return this.craftingWindows;
   }

   @Nullable
   @Override
   public QIOFrequency getFrequency() {
      return this.getQIOFrequency();
   }

   private void validateWindow(int window) throws ComputerException {
      if (window < 0 || window >= this.craftingWindows.length) {
         throw new ComputerException("Window '%d' is out of bounds, must be between 0 and %d.", window, this.craftingWindows.length);
      }
   }

   @ComputerMethod
   ItemStack getCraftingInput(int window, int slot) throws ComputerException {
      this.validateWindow(window);
      if (slot >= 0 && slot < 9) {
         return this.craftingWindows[window].getInputSlot(slot).getStack();
      } else {
         throw new ComputerException("Slot '%d' is out of bounds, must be between 0 and 9.", slot);
      }
   }

   @ComputerMethod
   ItemStack getCraftingOutput(int window) throws ComputerException {
      this.validateWindow(window);
      return this.craftingWindows[window].getOutputSlot().getStack();
   }
}
