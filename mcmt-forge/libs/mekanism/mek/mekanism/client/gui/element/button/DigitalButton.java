package mekanism.client.gui.element.button;

import mekanism.api.text.ILangEntry;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.common.lib.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DigitalButton extends TranslationButton {
   public DigitalButton(
      IGuiWrapper gui, int x, int y, int width, int height, ILangEntry translationHelper, @NotNull Runnable onPress, @Nullable GuiElement.IHoverable onHover
   ) {
      super(gui, x, y, width, height, translationHelper, onPress, onHover, null);
      this.setButtonBackground(GuiElement.ButtonBackground.DIGITAL);
   }

   @Override
   protected int getButtonTextColor(int mouseX, int mouseY) {
      if (this.f_93623_) {
         return this.isMouseOverCheckWindows(mouseX, mouseY) ? this.screenTextColor() : Color.argb(this.screenTextColor()).darken(0.2).argb();
      } else {
         return Color.argb(this.screenTextColor()).darken(0.4).argb();
      }
   }
}
