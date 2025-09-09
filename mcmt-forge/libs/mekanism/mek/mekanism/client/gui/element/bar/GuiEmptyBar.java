package mekanism.client.gui.element.bar;

import mekanism.client.gui.IGuiWrapper;
import net.minecraft.client.gui.GuiGraphics;

public class GuiEmptyBar extends GuiBar<GuiBar.IBarInfoHandler> {
   private static final GuiBar.IBarInfoHandler EMPTY_INFO = () -> 0.0;

   public GuiEmptyBar(IGuiWrapper gui, int x, int y, int width, int height) {
      super(null, gui, EMPTY_INFO, x, y, width, height, width >= height);
   }

   @Override
   protected void renderBarOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, double handlerLevel) {
   }
}
