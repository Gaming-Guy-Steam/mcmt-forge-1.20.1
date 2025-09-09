package mekanism.client.gui.element.scroll;

import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public abstract class GuiScrollList extends GuiScrollableElement {
   public static final ResourceLocation SCROLL_LIST = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "scroll_list.png");
   public static final int TEXTURE_WIDTH = 6;
   public static final int TEXTURE_HEIGHT = 6;
   private final ResourceLocation background;
   private final int backgroundSideSize;
   protected final int elementHeight;

   protected GuiScrollList(IGuiWrapper gui, int x, int y, int width, int height, int elementHeight, ResourceLocation background, int backgroundSideSize) {
      super(SCROLL_LIST, gui, x, y, width, height, width - 6, 2, 4, 4, height - 4);
      this.elementHeight = elementHeight;
      this.background = background;
      this.backgroundSideSize = backgroundSideSize;
   }

   @Override
   protected int getFocusedElements() {
      return (this.f_93619_ - 2) / this.elementHeight;
   }

   public abstract boolean hasSelection();

   protected abstract void setSelected(int index);

   public abstract void clearSelection();

   protected abstract void renderElements(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks);

   @Override
   public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
      this.renderBackgroundTexture(guiGraphics, this.background, this.backgroundSideSize, this.backgroundSideSize);
      this.drawScrollBar(guiGraphics, 6, 6);
      this.renderElements(guiGraphics, mouseX, mouseY, partialTicks);
   }

   @Override
   public void onClick(double mouseX, double mouseY, int button) {
      super.onClick(mouseX, mouseY, button);
      if (mouseX >= this.m_252754_() + 1
         && mouseX < this.m_252754_() + this.barXShift - 1
         && mouseY >= this.m_252907_() + 1
         && mouseY < this.m_252907_() + this.f_93619_ - 1) {
         int index = this.getCurrentSelection();
         int focused = this.getFocusedElements();
         int maxElements = this.getMaxElements();

         for (int i = 0; i < focused && index + i < maxElements; i++) {
            int shiftedY = this.m_252907_() + 1 + this.elementHeight * i;
            if (mouseY >= shiftedY && mouseY <= shiftedY + this.elementHeight) {
               this.setSelected(index + i);
               return;
            }
         }

         this.clearSelection();
      }
   }

   @Override
   public boolean m_6050_(double mouseX, double mouseY, double delta) {
      return this.m_5953_(mouseX, mouseY) && this.adjustScroll(delta) || super.m_6050_(mouseX, mouseY, delta);
   }
}
