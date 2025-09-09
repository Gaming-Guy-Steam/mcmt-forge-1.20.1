package mekanism.client.gui.element.window;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.gui.element.button.GuiCloseButton;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.inventory.container.IGUIWindow;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.lib.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

public class GuiWindow extends GuiTexturedElement implements IGUIWindow {
   private static final Color OVERLAY_COLOR = Color.rgbai(60, 60, 60, 128);
   private final SelectedWindowData windowData;
   private boolean dragging = false;
   private double dragX;
   private double dragY;
   private int prevDX;
   private int prevDY;
   private Consumer<GuiWindow> closeListener;
   private Consumer<GuiWindow> reattachListener;
   protected GuiWindow.InteractionStrategy interactionStrategy = GuiWindow.InteractionStrategy.CONTAINER;

   private static SelectedWindowData.WindowPosition calculateOpenPosition(IGuiWrapper gui, SelectedWindowData windowData, int x, int y, int width, int height) {
      SelectedWindowData.WindowPosition lastPosition = windowData.getLastPosition();
      int lastX = lastPosition.x();
      if (lastX != Integer.MAX_VALUE) {
         int guiLeft = gui.getLeft();
         if (guiLeft + lastX < 0) {
            lastX = -guiLeft;
         } else if (guiLeft + lastX + width > minecraft.m_91268_().m_85445_()) {
            lastX = minecraft.m_91268_().m_85445_() - guiLeft - width;
         }
      }

      int lastY = lastPosition.y();
      if (lastY != Integer.MAX_VALUE) {
         int guiTop = gui.getTop();
         if (guiTop + lastY < 0) {
            lastY = -guiTop;
         } else if (guiTop + lastY + height > minecraft.m_91268_().m_85446_()) {
            lastY = minecraft.m_91268_().m_85446_() - guiTop - height;
         }
      }

      return new SelectedWindowData.WindowPosition(lastX == Integer.MAX_VALUE ? x : lastX, lastY == Integer.MAX_VALUE ? y : lastY);
   }

   public GuiWindow(IGuiWrapper gui, int x, int y, int width, int height, SelectedWindowData.WindowType windowType) {
      this(
         gui,
         x,
         y,
         width,
         height,
         windowType == SelectedWindowData.WindowType.UNSPECIFIED ? SelectedWindowData.UNSPECIFIED : new SelectedWindowData(windowType)
      );
   }

   public GuiWindow(IGuiWrapper gui, int x, int y, int width, int height, SelectedWindowData windowData) {
      this(gui, calculateOpenPosition(gui, windowData, x, y, width, height), width, height, windowData);
   }

   private GuiWindow(IGuiWrapper gui, SelectedWindowData.WindowPosition calculatedPosition, int width, int height, SelectedWindowData windowData) {
      super(GuiMekanism.BASE_BACKGROUND, gui, calculatedPosition.x(), calculatedPosition.y(), width, height);
      this.windowData = windowData;
      this.isOverlay = true;
      this.f_93623_ = true;
      if (!this.isFocusOverlay()) {
         this.addCloseButton();
      }
   }

   public void onFocusLost() {
   }

   public void onFocused() {
      this.gui().setSelectedWindow(this.windowData);
   }

   protected void addCloseButton() {
      this.addChild(new GuiCloseButton(this.gui(), this.relativeX + 6, this.relativeY + 6, this));
   }

   public final GuiWindow.InteractionStrategy getInteractionStrategy() {
      return this.interactionStrategy;
   }

   @Nullable
   @Override
   public GuiElement mouseClickedNested(double mouseX, double mouseY, int button) {
      GuiElement ret = super.mouseClickedNested(mouseX, mouseY, button);
      if (this.m_5953_(mouseX, mouseY)) {
         if (mouseY < this.m_252907_() + 18) {
            this.dragging = true;
            this.dragX = mouseX;
            this.dragY = mouseY;
            this.prevDX = 0;
            this.prevDY = 0;
         }
      } else if (ret == null && this.interactionStrategy.allowContainer() && this.gui() instanceof GuiMekanism<?> gui) {
         AbstractContainerMenu c = gui.m_6262_();
         if (!(c instanceof IEmptyContainer)
            && mouseX >= this.getGuiLeft()
            && mouseX < this.getGuiLeft() + this.getGuiWidth()
            && mouseY >= this.getGuiTop() + this.getGuiHeight() - 90) {
            return null;
         }
      }

      if (ret == null) {
         return this.interactionStrategy.allowAll() ? null : this;
      } else {
         return ret;
      }
   }

   @Override
   public void m_7212_(double mouseX, double mouseY, double deltaX, double deltaY) {
      super.m_7212_(mouseX, mouseY, deltaX, deltaY);
      if (this.dragging) {
         int newDX = (int)Math.round(mouseX - this.dragX);
         int newDY = (int)Math.round(mouseY - this.dragY);
         int changeX = Mth.m_14045_(newDX - this.prevDX, -this.m_252754_(), minecraft.m_91268_().m_85445_() - (this.m_252754_() + this.f_93618_));
         int changeY = Mth.m_14045_(newDY - this.prevDY, -this.m_252907_(), minecraft.m_91268_().m_85446_() - (this.m_252907_() + this.f_93619_));
         this.prevDX = newDX;
         this.prevDY = newDY;
         this.move(changeX, changeY);
      }
   }

   @Override
   public void m_7691_(double mouseX, double mouseY) {
      super.m_7691_(mouseX, mouseY);
      this.dragging = false;
   }

   @Override
   public void renderBackgroundOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      if (this.isFocusOverlay()) {
         MekanismRenderer.renderColorOverlay(guiGraphics, -this.getGuiLeft(), -this.getGuiTop(), OVERLAY_COLOR.rgba());
      } else {
         guiGraphics.m_280246_(1.0F, 1.0F, 1.0F, 0.75F);
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         GuiUtils.renderBackgroundTexture(
            guiGraphics, GuiMekanism.SHADOW, 4, 4, this.relativeX - 3, this.relativeY - 3, this.f_93618_ + 6, this.f_93619_ + 6, 256, 256
         );
         MekanismRenderer.resetColor(guiGraphics);
      }

      this.renderBackgroundTexture(guiGraphics, this.getResource(), 4, 4);
   }

   @Override
   public boolean m_7933_(int keyCode, int scanCode, int modifiers) {
      if (super.m_7933_(keyCode, scanCode, modifiers)) {
         return true;
      } else if (keyCode == 256) {
         this.close();
         return true;
      } else {
         return false;
      }
   }

   public void setListenerTab(Supplier<? extends GuiElement> elementSupplier) {
      this.setTabListeners(window -> elementSupplier.get().f_93623_ = true, window -> elementSupplier.get().f_93623_ = false);
   }

   public void setTabListeners(Consumer<GuiWindow> closeListener, Consumer<GuiWindow> reattachListener) {
      this.closeListener = closeListener;
      this.reattachListener = reattachListener;
   }

   @Override
   public void resize(int prevLeft, int prevTop, int left, int top) {
      super.resize(prevLeft, prevTop, left, top);
      if (this.reattachListener != null) {
         this.reattachListener.accept(this);
      }
   }

   public void renderBlur(GuiGraphics guiGraphics) {
      guiGraphics.m_280246_(1.0F, 1.0F, 1.0F, 0.3F);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableDepthTest();
      GuiUtils.renderBackgroundTexture(guiGraphics, GuiMekanism.BLUR, 4, 4, this.relativeX, this.relativeY, this.f_93618_, this.f_93619_, 256, 256);
      MekanismRenderer.resetColor(guiGraphics);
      RenderSystem.enableDepthTest();
   }

   public void close() {
      this.gui().removeWindow(this);
      this.children.forEach(GuiElement::onWindowClose);
      if (this.closeListener != null) {
         this.closeListener.accept(this);
      }

      this.windowData.updateLastPosition(this.relativeX, this.relativeY);
   }

   protected boolean isFocusOverlay() {
      return false;
   }

   @Override
   public void drawTitleText(GuiGraphics guiGraphics, Component text, float y) {
      if (this.isFocusOverlay()) {
         super.drawTitleText(guiGraphics, text, y);
      } else {
         int leftShift = this.getTitlePadStart();
         int xSize = this.getXSize() - leftShift - this.getTitlePadEnd();
         int maxLength = xSize - 12;
         float textWidth = this.getStringWidth(text);
         float scale = Math.min(1.0F, maxLength / textWidth);
         float left = this.relativeX + xSize / 2.0F;
         this.drawScaledCenteredText(guiGraphics, text, left + leftShift, this.relativeY + y, this.titleTextColor(), scale);
      }
   }

   protected int getTitlePadStart() {
      return 12;
   }

   protected int getTitlePadEnd() {
      return 0;
   }

   public static enum InteractionStrategy {
      NONE,
      CONTAINER,
      ALL;

      public boolean allowContainer() {
         return this != NONE;
      }

      public boolean allowAll() {
         return this == ALL;
      }
   }
}
