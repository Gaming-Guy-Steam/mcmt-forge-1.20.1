package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;

public abstract class GuiInsetElement<DATA_SOURCE> extends GuiSideHolder {
   protected final int border;
   protected final int innerWidth;
   protected final int innerHeight;
   protected final DATA_SOURCE dataSource;
   protected final ResourceLocation overlay;

   public GuiInsetElement(ResourceLocation overlay, IGuiWrapper gui, DATA_SOURCE dataSource, int x, int y, int height, int innerSize, boolean left) {
      super(gui, x, y, height, left, false);
      this.overlay = overlay;
      this.dataSource = dataSource;
      this.innerWidth = innerSize;
      this.innerHeight = innerSize;
      this.border = (this.f_93618_ - this.innerWidth) / 2;
      this.clickSound = SoundEvents.f_12490_;
      this.f_93623_ = true;
   }

   @Override
   public boolean m_5953_(double xAxis, double yAxis) {
      return this.f_93623_
         && this.f_93624_
         && xAxis >= this.m_252754_() + this.border
         && xAxis < this.m_252754_() + this.f_93618_ - this.border
         && yAxis >= this.m_252907_() + this.border
         && yAxis < this.m_252907_() + this.f_93619_ - this.border;
   }

   @Override
   protected int getButtonX() {
      return super.getButtonX() + this.border + (this.left ? 1 : -1);
   }

   @Override
   protected int getButtonY() {
      return super.getButtonY() + this.border;
   }

   @Override
   protected int getButtonWidth() {
      return this.innerWidth;
   }

   @Override
   protected int getButtonHeight() {
      return this.innerHeight;
   }

   protected ResourceLocation getOverlay() {
      return this.overlay;
   }

   @Override
   public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
      if (this.buttonBackground != GuiElement.ButtonBackground.NONE) {
         this.drawButton(guiGraphics, mouseX, mouseY);
      }

      this.drawBackgroundOverlay(guiGraphics);
   }

   protected void drawBackgroundOverlay(@NotNull GuiGraphics guiGraphics) {
      guiGraphics.m_280163_(
         this.getOverlay(), this.getButtonX(), this.getButtonY(), 0.0F, 0.0F, this.innerWidth, this.innerHeight, this.innerWidth, this.innerHeight
      );
   }
}
