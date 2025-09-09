package mekanism.client.gui.element.bar;

import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class GuiDigitalBar extends GuiBar<GuiBar.IBarInfoHandler> {
   private static final ResourceLocation DIGITAL_BAR = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_BAR, "dynamic_digital.png");
   private static final int texWidth = 2;
   private static final int texHeight = 2;

   public GuiDigitalBar(IGuiWrapper gui, GuiBar.IBarInfoHandler handler, int x, int y, int width) {
      super(DIGITAL_BAR, gui, handler, x, y, width - 2, 6, true);
   }

   @Override
   protected void renderBarOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, double handlerLevel) {
   }

   @Override
   public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      guiGraphics.m_280411_(DIGITAL_BAR, this.relativeX, this.relativeY, this.f_93618_, this.f_93619_, 1.0F, 0.0F, 1, 1, 2, 2);
      guiGraphics.m_280411_(DIGITAL_BAR, this.relativeX + 1, this.relativeY + 1, this.f_93618_ - 2, 6, 1.0F, 1.0F, 1, 1, 2, 2);
      guiGraphics.m_280411_(
         DIGITAL_BAR, this.relativeX + 1, this.relativeY + 1, calculateScaled(this.getHandler().getLevel(), this.f_93618_ - 2), 6, 0.0F, 0.0F, 1, 1, 2, 2
      );
   }
}
