package mekanism.client.gui.element.custom;

import java.util.function.Consumer;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiSideHolder;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class GuiResizeControls extends GuiSideHolder {
   private final MekanismImageButton expandButton;
   private final MekanismImageButton shrinkButton;
   private final Consumer<GuiResizeControls.ResizeType> resizeHandler;
   private int tooltipTicks;
   private static final ResourceLocation MINUS = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_BUTTON, "minus.png");
   private static final ResourceLocation PLUS = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_BUTTON, "plus.png");

   public GuiResizeControls(IGuiWrapper gui, int y, Consumer<GuiResizeControls.ResizeType> resizeHandler) {
      super(gui, -26, y, 39, true, false);
      this.resizeHandler = resizeHandler;
      this.expandButton = this.addChild(
         new MekanismImageButton(
            gui, this.relativeX + 4, this.relativeY + 5, 19, 9, 19, 9, PLUS, () -> this.handleResize(GuiResizeControls.ResizeType.EXPAND_Y)
         )
      );
      this.shrinkButton = this.addChild(
         new MekanismImageButton(
            gui, this.relativeX + 4, this.relativeY + 25, 19, 9, 19, 9, MINUS, () -> this.handleResize(GuiResizeControls.ResizeType.SHRINK_Y)
         )
      );
      this.updateButtonState();
      this.f_93623_ = true;
   }

   @Override
   public void tick() {
      super.tick();
      this.tooltipTicks = Math.max(0, this.tooltipTicks - 1);
   }

   @Override
   public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderToolTip(guiGraphics, mouseX, mouseY);
      if (this.tooltipTicks > 0 && !this.expandButton.f_93623_) {
         this.displayTooltips(guiGraphics, mouseX, mouseY, new Component[]{MekanismLang.QIO_COMPENSATE_TOOLTIP.translate(new Object[0])});
      }
   }

   @Override
   public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderForeground(guiGraphics, mouseX, mouseY);
      this.drawScaledCenteredTextScaledBound(
         guiGraphics,
         MekanismLang.HEIGHT.translate(new Object[0]),
         this.relativeX + 13.5F,
         this.relativeY + 15.5F,
         this.titleTextColor(),
         this.f_93618_ - 4,
         0.7F
      );
   }

   @Override
   public void onClick(double mouseX, double mouseY, int button) {
      super.onClick(mouseX, mouseY, button);
      if (!this.expandButton.f_93623_
         && mouseX >= this.expandButton.m_252754_()
         && mouseX < this.expandButton.m_252754_() + this.expandButton.m_5711_()
         && mouseY >= this.expandButton.m_252907_()
         && mouseY < this.expandButton.m_252907_() + this.expandButton.m_93694_()) {
         this.tooltipTicks = 100;
      }
   }

   @Override
   protected void colorTab(GuiGraphics guiGraphics) {
      MekanismRenderer.color(guiGraphics, SpecialColors.TAB_RESIZE_CONTROLS);
   }

   private void handleResize(GuiResizeControls.ResizeType type) {
      this.resizeHandler.accept(type);
      this.updateButtonState();
   }

   private void updateButtonState() {
      int index = this.getIndex();
      this.expandButton.f_93623_ = index < QIOItemViewerContainer.getSlotsYMax();
      this.shrinkButton.f_93623_ = index > 2;
   }

   private int getIndex() {
      return MekanismConfig.client.qioItemViewerSlotsY.get();
   }

   public static enum ResizeType {
      EXPAND_X,
      EXPAND_Y,
      SHRINK_X,
      SHRINK_Y;
   }
}
