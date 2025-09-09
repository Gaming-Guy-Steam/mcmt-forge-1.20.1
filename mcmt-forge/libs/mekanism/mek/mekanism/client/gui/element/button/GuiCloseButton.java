package mekanism.client.gui.element.button;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.common.MekanismLang;
import mekanism.common.util.MekanismUtils;

public class GuiCloseButton extends MekanismImageButton {
   public GuiCloseButton(IGuiWrapper gui, int x, int y, GuiWindow window) {
      super(
         gui,
         x,
         y,
         8,
         MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_BUTTON, "close.png"),
         window::close,
         (onHover, guiGraphics, mouseX, mouseY) -> gui.displayTooltips(guiGraphics, mouseX, mouseY, MekanismLang.CLOSE.translate(new Object[0]))
      );
   }

   @Override
   public boolean resetColorBeforeRender() {
      return false;
   }
}
