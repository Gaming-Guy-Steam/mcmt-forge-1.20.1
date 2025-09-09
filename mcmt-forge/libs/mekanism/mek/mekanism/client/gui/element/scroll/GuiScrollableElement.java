package mekanism.client.gui.element.scroll;

import mekanism.api.math.MathUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiTexturedElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public abstract class GuiScrollableElement extends GuiTexturedElement {
   protected double scroll;
   private boolean isDragging;
   private int dragOffset;
   protected final int maxBarHeight;
   protected final int barWidth;
   protected final int barHeight;
   protected final int barXShift;
   protected int barX;
   protected int barY;

   protected GuiScrollableElement(
      ResourceLocation resource,
      IGuiWrapper gui,
      int x,
      int y,
      int width,
      int height,
      int barXShift,
      int barYShift,
      int barWidth,
      int barHeight,
      int maxBarHeight
   ) {
      super(resource, gui, x, y, width, height);
      this.barXShift = barXShift;
      this.barX = this.relativeX + barXShift;
      this.barY = this.relativeY + barYShift;
      this.barWidth = barWidth;
      this.barHeight = barHeight;
      this.maxBarHeight = maxBarHeight;
   }

   @Override
   public void move(int changeX, int changeY) {
      super.move(changeX, changeY);
      this.barX += changeX;
      this.barY += changeY;
   }

   protected abstract int getMaxElements();

   protected abstract int getFocusedElements();

   @Override
   public void onClick(double mouseX, double mouseY, int button) {
      super.onClick(mouseX, mouseY, button);
      int scroll = this.getScroll();
      int x = this.getGuiLeft() + this.barX;
      int y = this.getGuiTop() + this.barY;
      if (mouseX >= x && mouseX <= x + this.barWidth && mouseY >= y + scroll && mouseY <= y + scroll + this.barHeight) {
         if (this.needsScrollBars()) {
            double yAxis = mouseY - this.getGuiTop();
            this.dragOffset = (int)(yAxis - (scroll + this.barY));
            this.isDragging = true;
         } else {
            this.scroll = 0.0;
         }
      }
   }

   @Override
   public void m_7212_(double mouseX, double mouseY, double deltaX, double deltaY) {
      super.m_7212_(mouseX, mouseY, deltaX, deltaY);
      if (this.isDragging && this.needsScrollBars()) {
         double yAxis = mouseY - this.getGuiTop();
         this.scroll = Mth.m_14008_((yAxis - this.barY - this.dragOffset) / this.getMax(), 0.0, 1.0);
      }
   }

   @Override
   public void m_7691_(double mouseX, double mouseY) {
      super.m_7691_(mouseX, mouseY);
      this.dragOffset = 0;
      this.isDragging = false;
   }

   protected boolean needsScrollBars() {
      return this.getMaxElements() > this.getFocusedElements();
   }

   protected final int getElements() {
      return this.getMaxElements() - this.getFocusedElements();
   }

   protected int getScrollElementScaler() {
      return 1;
   }

   private int getMax() {
      return this.maxBarHeight - this.barHeight;
   }

   protected int getScroll() {
      int max = this.getMax();
      return Mth.m_14045_((int)(this.scroll * max), 0, max);
   }

   public int getCurrentSelection() {
      return this.needsScrollBars() ? (int)((this.getElements() + 0.5) * this.scroll) : 0;
   }

   public boolean adjustScroll(double delta) {
      if (delta != 0.0 && this.needsScrollBars()) {
         int elements = MathUtils.clampToInt(Math.ceil((double)this.getElements() / this.getScrollElementScaler()));
         if (elements > 0) {
            if (delta > 0.0) {
               delta = 1.0;
            } else {
               delta = -1.0;
            }

            this.scroll = (float)Mth.m_14008_(this.scroll - delta / elements, 0.0, 1.0);
            return true;
         }
      }

      return false;
   }

   protected void drawScrollBar(GuiGraphics guiGraphics, int textureWidth, int textureHeight) {
      ResourceLocation texture = this.getResource();
      guiGraphics.m_280163_(texture, this.barX - 1, this.barY - 1, 0.0F, 0.0F, textureWidth, 1, textureWidth, textureHeight);
      guiGraphics.m_280411_(texture, this.barX - 1, this.barY, textureWidth, this.maxBarHeight, 0.0F, 1.0F, textureWidth, 1, textureWidth, textureHeight);
      guiGraphics.m_280163_(texture, this.barX - 1, this.relativeY + this.maxBarHeight + 2, 0.0F, 0.0F, textureWidth, 1, textureWidth, textureHeight);
      guiGraphics.m_280163_(texture, this.barX, this.barY + this.getScroll(), 0.0F, 2.0F, this.barWidth, this.barHeight, textureWidth, textureHeight);
   }

   @Override
   public boolean hasPersistentData() {
      return true;
   }

   @Override
   public void syncFrom(GuiElement element) {
      super.syncFrom(element);
      GuiScrollableElement old = (GuiScrollableElement)element;
      if (this.needsScrollBars() && old.needsScrollBars()) {
         this.scroll = old.scroll;
      }
   }
}
