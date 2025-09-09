package mekanism.client.gui.element.bar;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.lib.Color;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class GuiDynamicHorizontalRateBar extends GuiBar<GuiBar.IBarInfoHandler> {
   private static final ResourceLocation RATE_BAR = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_BAR, "dynamic_rate.png");
   private static final int texWidth = 3;
   private static final int texHeight = 8;
   private final Color.ColorFunction colorFunction;

   public GuiDynamicHorizontalRateBar(IGuiWrapper gui, GuiBar.IBarInfoHandler handler, int x, int y, int width) {
      this(gui, handler, x, y, width, Color.ColorFunction.HEAT);
   }

   public GuiDynamicHorizontalRateBar(IGuiWrapper gui, GuiBar.IBarInfoHandler handler, int x, int y, int width, Color.ColorFunction colorFunction) {
      super(RATE_BAR, gui, handler, x, y, width, 8, true);
      this.colorFunction = colorFunction;
   }

   @Override
   protected void renderBarOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, double handlerLevel) {
      int displayInt = (int)(handlerLevel * (this.f_93618_ - 2));
      if (displayInt > 0) {
         for (int i = 0; i < displayInt; i++) {
            float level = (float)i / (this.f_93618_ - 2);
            MekanismRenderer.color(guiGraphics, this.colorFunction.getColor(level));
            if (i == 0) {
               guiGraphics.m_280163_(this.getResource(), this.relativeX + 1, this.relativeY + 1, 0.0F, 0.0F, 1, 8, 3, 8);
            } else if (i == displayInt - 1) {
               guiGraphics.m_280163_(this.getResource(), this.relativeX + 1 + i, this.relativeY + 1, 2.0F, 0.0F, 1, 8, 3, 8);
            } else {
               guiGraphics.m_280163_(this.getResource(), this.relativeX + 1 + i, this.relativeY + 1, 1.0F, 0.0F, 1, 8, 3, 8);
            }
         }

         MekanismRenderer.resetColor(guiGraphics);
      }
   }
}
