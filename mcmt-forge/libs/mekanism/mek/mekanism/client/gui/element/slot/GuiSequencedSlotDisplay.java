package mekanism.client.gui.element.slot;

import java.util.Collections;
import java.util.List;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.NonNullSupplier;
import org.jetbrains.annotations.NotNull;

public class GuiSequencedSlotDisplay extends GuiElement {
   private List<ItemStack> iterStacks = Collections.emptyList();
   private int stackIndex;
   private int stackSwitchTicker;
   @NotNull
   private ItemStack renderStack = ItemStack.f_41583_;
   private final NonNullSupplier<List<ItemStack>> stackListSupplier;

   public GuiSequencedSlotDisplay(IGuiWrapper gui, int x, int y, NonNullSupplier<List<ItemStack>> stackListSupplier) {
      super(gui, x, y, 16, 16);
      this.stackListSupplier = stackListSupplier;
      this.f_93623_ = false;
   }

   @Override
   public void tick() {
      super.tick();
      if (this.stackSwitchTicker > 0) {
         this.stackSwitchTicker--;
      }

      if (this.iterStacks.isEmpty()) {
         this.renderStack = ItemStack.f_41583_;
      } else if (this.stackSwitchTicker == 0) {
         int size = this.iterStacks.size();
         if (this.stackIndex == -1 || this.stackIndex == size - 1) {
            this.stackIndex = 0;
         } else if (this.stackIndex < size - 1) {
            this.stackIndex++;
         }

         this.stackIndex = Math.min(size - 1, this.stackIndex);
         this.renderStack = this.iterStacks.get(this.stackIndex);
         this.stackSwitchTicker = 20;
      }
   }

   @Override
   public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
      this.gui().renderItem(guiGraphics, this.renderStack, this.relativeX, this.relativeY);
   }

   public void updateStackList() {
      this.iterStacks = (List<ItemStack>)this.stackListSupplier.get();
      this.stackSwitchTicker = 0;
      this.tick();
      this.stackIndex = -1;
   }

   @NotNull
   public ItemStack getRenderStack() {
      return this.renderStack;
   }
}
