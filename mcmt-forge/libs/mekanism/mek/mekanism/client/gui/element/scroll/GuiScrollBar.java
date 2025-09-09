package mekanism.client.gui.element.scroll;

import java.util.function.IntSupplier;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class GuiScrollBar extends GuiScrollableElement {
   private static final ResourceLocation BAR = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "scroll_bar.png");
   private static final int TEXTURE_WIDTH = 24;
   private static final int TEXTURE_HEIGHT = 15;
   private final IntSupplier maxElements;
   private final IntSupplier focusedElements;

   public GuiScrollBar(IGuiWrapper gui, int x, int y, int height, IntSupplier maxElements, IntSupplier focusedElements) {
      super(BAR, gui, x, y, 14, height, 1, 1, 12, 15, height - 2);
      this.maxElements = maxElements;
      this.focusedElements = focusedElements;
   }

   @Override
   public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
      GuiUtils.renderBackgroundTexture(guiGraphics, GuiElementHolder.HOLDER, 32, 32, this.relativeX, this.relativeY, this.barWidth + 2, this.f_93619_, 256, 256);
      guiGraphics.m_280163_(
         this.getResource(),
         this.barX,
         this.barY + this.getScroll(),
         this.needsScrollBars() ? 0.0F : this.barWidth,
         0.0F,
         this.barWidth,
         this.barHeight,
         24,
         15
      );
   }

   @Override
   protected int getMaxElements() {
      return this.maxElements.getAsInt();
   }

   @Override
   protected int getFocusedElements() {
      return this.focusedElements.getAsInt();
   }
}
