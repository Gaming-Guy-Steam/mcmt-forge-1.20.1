package mekanism.client.gui.element.bar;

import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class GuiVerticalRateBar extends GuiBar<GuiBar.IBarInfoHandler> {
   private static final ResourceLocation RATE_BAR = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_BAR, "vertical_rate.png");
   private static final int texWidth = 6;
   private static final int texHeight = 58;

   public GuiVerticalRateBar(IGuiWrapper gui, GuiBar.IBarInfoHandler handler, int x, int y) {
      super(RATE_BAR, gui, handler, x, y, 6, 58, false);
   }

   @Override
   protected void renderBarOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, double handlerLevel) {
      int displayInt = (int)(handlerLevel * 58.0);
      if (displayInt > 0) {
         guiGraphics.m_280163_(
            this.getResource(),
            this.relativeX + 1,
            this.relativeY + this.f_93619_ - 1 - displayInt,
            8.0F,
            this.f_93619_ - 2 - displayInt,
            this.f_93618_ - 2,
            displayInt,
            6,
            58
         );
      }
   }
}
