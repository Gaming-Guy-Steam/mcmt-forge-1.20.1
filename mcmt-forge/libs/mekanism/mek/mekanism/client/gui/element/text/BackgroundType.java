package mekanism.client.gui.element.text;

import java.util.function.BiConsumer;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.GuiInnerScreen;
import net.minecraft.client.gui.GuiGraphics;

public enum BackgroundType {
   INNER_SCREEN(
      (field, guiGraphics) -> GuiUtils.renderBackgroundTexture(
         guiGraphics,
         GuiInnerScreen.SCREEN,
         GuiInnerScreen.SCREEN_SIZE,
         GuiInnerScreen.SCREEN_SIZE,
         field.getRelativeX() - 1,
         field.getRelativeY() - 1,
         field.m_5711_() + 2,
         field.m_93694_() + 2,
         256,
         256
      )
   ),
   ELEMENT_HOLDER(
      (field, guiGraphics) -> GuiUtils.renderBackgroundTexture(
         guiGraphics, GuiElementHolder.HOLDER, 32, 32, field.getRelativeX() - 1, field.getRelativeY() - 1, field.m_5711_() + 2, field.m_93694_() + 2, 256, 256
      )
   ),
   DEFAULT((field, guiGraphics) -> {
      GuiUtils.fill(guiGraphics, field.getRelativeX() - 1, field.getRelativeY() - 1, field.m_5711_() + 2, field.m_93694_() + 2, -6250336);
      GuiUtils.fill(guiGraphics, field.getRelativeX(), field.getRelativeY(), field.m_5711_(), field.m_93694_(), -16777216);
   }),
   DIGITAL(
      (field, guiGraphics) -> {
         GuiUtils.fill(
            guiGraphics,
            field.getRelativeX() - 1,
            field.getRelativeY() - 1,
            field.m_5711_() + 2,
            field.m_93694_() + 2,
            field.isTextFieldFocused() ? GuiTextField.SCREEN_COLOR.getAsInt() : GuiTextField.DARK_SCREEN_COLOR.getAsInt()
         );
         GuiUtils.fill(guiGraphics, field.getRelativeX(), field.getRelativeY(), field.m_5711_(), field.m_93694_(), -16777216);
      }
   ),
   NONE((field, guiGraphics) -> {});

   private final BiConsumer<GuiTextField, GuiGraphics> renderFunction;

   private BackgroundType(BiConsumer<GuiTextField, GuiGraphics> renderFunction) {
      this.renderFunction = renderFunction;
   }

   public void render(GuiTextField field, GuiGraphics guiGraphics) {
      this.renderFunction.accept(field, guiGraphics);
   }
}
