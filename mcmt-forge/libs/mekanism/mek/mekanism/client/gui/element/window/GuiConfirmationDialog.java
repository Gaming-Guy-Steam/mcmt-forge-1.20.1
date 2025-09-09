package mekanism.client.gui.element.window;

import java.util.function.Supplier;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.render.IFancyFontRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.SelectedWindowData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class GuiConfirmationDialog extends GuiWindow {
   private final IFancyFontRenderer.WrappedTextRenderer wrappedTextRenderer;

   private GuiConfirmationDialog(
      IGuiWrapper gui, int x, int y, int width, int height, Component title, Runnable onConfirm, GuiConfirmationDialog.DialogType type
   ) {
      super(gui, x, y, width, height, SelectedWindowData.WindowType.CONFIRMATION);
      this.wrappedTextRenderer = new IFancyFontRenderer.WrappedTextRenderer(this, title);
      this.f_93623_ = true;
      this.addChild(new TranslationButton(gui, this.relativeX + width / 2 - 51, this.relativeY + height - 24, 50, 18, MekanismLang.BUTTON_CANCEL, this::close));
      this.addChild(new TranslationButton(gui, this.relativeX + width / 2 + 1, this.relativeY + height - 24, 50, 18, MekanismLang.BUTTON_CONFIRM, () -> {
         onConfirm.run();
         this.close();
      }, null, type.getColorSupplier()));
   }

   public static void show(IGuiWrapper gui, Component title, Runnable onConfirm, GuiConfirmationDialog.DialogType type) {
      int width = 140;
      int height = 33 + IFancyFontRenderer.WrappedTextRenderer.calculateHeightRequired(gui.getFont(), title, width, (float)(width - 10));
      gui.addWindow(new GuiConfirmationDialog(gui, (gui.getWidth() - width) / 2, (gui.getHeight() - height) / 2, width, height, title, onConfirm, type));
   }

   @Override
   public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderForeground(guiGraphics, mouseX, mouseY);
      this.wrappedTextRenderer
         .renderCentered(guiGraphics, this.relativeX + this.f_93618_ / 2.0F, this.relativeY + 6, this.titleTextColor(), this.f_93618_ - 10);
   }

   @Override
   public boolean m_5953_(double mouseX, double mouseY) {
      return true;
   }

   @Override
   protected boolean isFocusOverlay() {
      return true;
   }

   public static enum DialogType {
      NORMAL(() -> null),
      DANGER(() -> EnumColor.RED);

      private final Supplier<EnumColor> colorSupplier;

      private DialogType(Supplier<EnumColor> colorSupplier) {
         this.colorSupplier = colorSupplier;
      }

      public Supplier<EnumColor> getColorSupplier() {
         return this.colorSupplier;
      }
   }
}
