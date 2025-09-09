package mekanism.client.gui.element.slot;

import java.util.Optional;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.VirtualSlotContainerScreen;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.jei.interfaces.IJEIIngredientHelper;
import mekanism.common.inventory.container.IGUIWindow;
import mekanism.common.inventory.container.slot.IVirtualSlot;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.slot.VirtualInventoryContainerSlot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiVirtualSlot extends GuiSlot implements IJEIIngredientHelper {
   private IVirtualSlot virtualSlot;

   public GuiVirtualSlot(@Nullable IGUIWindow window, SlotType type, IGuiWrapper gui, int x, int y, VirtualInventoryContainerSlot containerSlot) {
      this(type, gui, x, y);
      if (containerSlot != null) {
         SlotOverlay slotOverlay = containerSlot.getSlotOverlay();
         if (slotOverlay != null) {
            this.with(slotOverlay);
         }

         this.updateVirtualSlot(window, containerSlot);
      }
   }

   public GuiVirtualSlot(SlotType type, IGuiWrapper gui, int x, int y) {
      super(type, gui, x, y);
      this.setRenderHover(true);
   }

   public boolean isElementForSlot(IVirtualSlot virtualSlot) {
      return this.virtualSlot == virtualSlot;
   }

   public void updateVirtualSlot(@Nullable IGUIWindow window, @NotNull IVirtualSlot virtualSlot) {
      this.virtualSlot = virtualSlot;
      this.virtualSlot.updatePosition(window, () -> this.relativeX + 1, () -> this.relativeY + 1);
   }

   @Override
   protected void drawContents(@NotNull GuiGraphics guiGraphics) {
      if (this.virtualSlot != null) {
         ItemStack stack = this.virtualSlot.getStackToRender();
         if (!stack.m_41619_()) {
            int xPos = this.relativeX + 1;
            int yPos = this.relativeY + 1;
            if (this.virtualSlot.shouldDrawOverlay()) {
               guiGraphics.m_285944_(RenderType.m_286086_(), xPos, yPos, xPos + 16, yPos + 16, -2130706433);
            }

            this.gui().renderItemWithOverlay(guiGraphics, stack, xPos, yPos, 1.0F, this.virtualSlot.getTooltipOverride());
         }
      }
   }

   @Nullable
   @Override
   public GuiElement mouseClickedNested(double mouseX, double mouseY, int button) {
      if (mouseX >= this.m_252754_()
         && mouseY >= this.m_252907_()
         && mouseX < this.m_252754_() + this.f_93618_
         && mouseY < this.m_252907_() + this.f_93619_
         && this.gui() instanceof VirtualSlotContainerScreen<?> screen
         && this.virtualSlot != null) {
         return screen.slotClicked(this.virtualSlot.getSlot(), button) ? this : null;
      } else {
         return super.mouseClickedNested(mouseX, mouseY, button);
      }
   }

   @Override
   public Optional<?> getIngredient(double mouseX, double mouseY) {
      return this.virtualSlot == null ? Optional.empty() : Optional.of(this.virtualSlot.getStackToRender());
   }

   @Override
   public Rect2i getIngredientBounds(double mouseX, double mouseY) {
      return new Rect2i(this.m_252754_() + 1, this.m_252907_() + 1, 16, 16);
   }
}
