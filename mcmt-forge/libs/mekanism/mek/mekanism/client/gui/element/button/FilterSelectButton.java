package mekanism.client.gui.element.button;

import com.mojang.blaze3d.systems.RenderSystem;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FilterSelectButton extends MekanismButton {
   private static final ResourceLocation ARROWS = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_BUTTON, "filter_arrows.png");
   private static final int TEXTURE_WIDTH = 22;
   private static final int TEXTURE_HEIGHT = 14;
   private final boolean down;

   public FilterSelectButton(IGuiWrapper gui, int x, int y, boolean down, @NotNull Runnable onPress, @Nullable GuiElement.IHoverable onHover) {
      super(gui, x, y, 11, 7, Component.m_237119_(), onPress, onHover);
      this.down = down;
   }

   @Override
   public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      if (this.resetColorBeforeRender()) {
         MekanismRenderer.resetColor(guiGraphics);
      }

      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.enableDepthTest();
      int width = this.getButtonWidth();
      int height = this.getButtonHeight();
      int x = this.getButtonX();
      int y = this.getButtonY();
      guiGraphics.m_280163_(ARROWS, x, y, this.isMouseOverCheckWindows(mouseX, mouseY) ? width : 0.0F, this.down ? 7.0F : 0.0F, width, height, 22, 14);
   }

   @Override
   public boolean m_5953_(double xAxis, double yAxis) {
      if (!super.m_5953_(xAxis, yAxis)) {
         return false;
      } else {
         double xShifted = xAxis - this.m_252754_();
         double yShifted = yAxis - this.m_252907_();
         if (this.down) {
            if (yShifted < 2.0) {
               return true;
            } else if (yShifted < 3.0) {
               return xShifted >= 1.0 && xShifted < 10.0;
            } else if (yShifted < 4.0) {
               return xShifted >= 2.0 && xShifted < 9.0;
            } else if (yShifted < 5.0) {
               return xShifted >= 3.0 && xShifted < 8.0;
            } else {
               return yShifted < 6.0 ? xShifted >= 4.0 && xShifted < 7.0 : xShifted >= 5.0 && xShifted < 6.0;
            }
         } else if (yShifted < 1.0) {
            return xShifted >= 5.0 && xShifted < 6.0;
         } else if (yShifted < 2.0) {
            return xShifted >= 4.0 && xShifted < 7.0;
         } else if (yShifted < 3.0) {
            return xShifted >= 3.0 && xShifted < 8.0;
         } else if (yShifted < 4.0) {
            return xShifted >= 2.0 && xShifted < 9.0;
         } else {
            return !(yShifted < 5.0) ? true : xShifted >= 1.0 && xShifted < 10.0;
         }
      }
   }
}
