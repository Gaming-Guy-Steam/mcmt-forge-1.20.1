package mekanism.client.gui.element;

import java.util.function.DoubleConsumer;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class GuiSlider extends GuiElement {
   private static final ResourceLocation SLIDER = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "smooth_slider.png");
   private final DoubleConsumer callback;
   private double value;
   private boolean isDragging;

   public GuiSlider(IGuiWrapper gui, int x, int y, int width, DoubleConsumer callback) {
      super(gui, x, y, width, 12);
      this.callback = callback;
   }

   public double getValue() {
      return this.value;
   }

   public void setValue(double value) {
      this.value = value;
   }

   @Override
   public void renderBackgroundOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderBackgroundOverlay(guiGraphics, mouseX, mouseY);
      GuiUtils.fill(guiGraphics, this.relativeX + 2, this.relativeY + 3, this.f_93618_ - 4, 6, -11184811);
      int posX = (int)(this.value * (this.f_93618_ - 6));
      guiGraphics.m_280163_(SLIDER, this.relativeX + posX, this.relativeY, 0.0F, 0.0F, 7, 12, 12, 12);
   }

   @Override
   public void m_7691_(double mouseX, double mouseY) {
      super.m_7691_(mouseX, mouseY);
      this.isDragging = false;
   }

   @Override
   public void onClick(double mouseX, double mouseY, int button) {
      super.onClick(mouseX, mouseY, button);
      this.set(mouseX, mouseY);
      this.isDragging = true;
   }

   @Override
   public void m_7212_(double mouseX, double mouseY, double deltaX, double deltaY) {
      super.m_7212_(mouseX, mouseY, deltaX, deltaY);
      if (this.isDragging) {
         this.set(mouseX, mouseY);
      }
   }

   private void set(double mouseX, double mouseY) {
      this.value = Mth.m_14008_((mouseX - this.m_252754_() - 2.0) / (this.f_93618_ - 6), 0.0, 1.0);
      this.callback.accept(this.value);
   }
}
