package mekanism.client.gui.element.bar;

import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class GuiHorizontalRateBar extends GuiBar<GuiBar.IBarInfoHandler> {
   private static final ResourceLocation RATE_BAR = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_BAR, "horizontal_rate.png");
   private static final int texWidth = 78;
   private static final int texHeight = 8;

   public GuiHorizontalRateBar(IGuiWrapper gui, GuiBar.IBarInfoHandler handler, int x, int y) {
      super(RATE_BAR, gui, handler, x, y, 78, 8, true);
   }

   @Override
   protected void renderBarOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, double handlerLevel) {
      int displayInt = (int)(handlerLevel * 78.0);
      if (displayInt > 0) {
         guiGraphics.m_280163_(this.getResource(), this.relativeX + 1, this.relativeY + 1, 0.0F, 0.0F, displayInt, 8, 78, 8);
      }
   }
}
