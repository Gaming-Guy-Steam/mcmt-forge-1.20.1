package mekanism.client.gui.element.text;

import java.util.function.BiFunction;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.common.util.MekanismUtils;

public enum ButtonType {
   NORMAL(
      (field, callback) -> new MekanismImageButton(
         field.gui(),
         field.getRelativeX() + field.m_5711_() - field.m_93694_(),
         field.getRelativeY(),
         field.m_93694_(),
         12,
         MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_BUTTON, "checkmark.png"),
         callback
      )
   ),
   DIGITAL(
      (field, callback) -> {
         MekanismImageButton ret = new MekanismImageButton(
            field.gui(),
            field.getRelativeX() + field.m_5711_() - field.m_93694_(),
            field.getRelativeY(),
            field.m_93694_(),
            12,
            MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_BUTTON, "checkmark_digital.png"),
            callback
         );
         ret.setButtonBackground(GuiElement.ButtonBackground.DIGITAL);
         return ret;
      }
   );

   private final BiFunction<GuiTextField, Runnable, MekanismImageButton> buttonCreator;

   private ButtonType(BiFunction<GuiTextField, Runnable, MekanismImageButton> buttonCreator) {
      this.buttonCreator = buttonCreator;
   }

   public MekanismImageButton getButton(GuiTextField field, Runnable callback) {
      return this.buttonCreator.apply(field, callback);
   }
}
