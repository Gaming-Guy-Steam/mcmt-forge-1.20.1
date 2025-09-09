package mekanism.client.gui.element.custom.module;

import mekanism.client.gui.element.button.RadioButton;
import mekanism.common.MekanismLang;
import mekanism.common.content.gear.ModuleConfigItem;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;

class BooleanToggle extends MiniElement {
   private static final int RADIO_SIZE = 8;
   private final ModuleConfigItem<Boolean> data;

   BooleanToggle(GuiModuleScreen parent, ModuleConfigItem<Boolean> data, int xPos, int yPos, int dataIndex) {
      super(parent, xPos, yPos, dataIndex);
      this.data = data;
   }

   @Override
   protected int getNeededHeight() {
      return 20;
   }

   @Override
   protected void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.drawRadio(guiGraphics, mouseX, mouseY, this.data.get(), 4, 11, 0);
      this.drawRadio(guiGraphics, mouseX, mouseY, !this.data.get(), 50, 11, 8);
   }

   private void drawRadio(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean selected, int relativeX, int relativeY, int selectedU) {
      if (selected) {
         guiGraphics.m_280163_(RadioButton.RADIO, this.getRelativeX() + relativeX, this.getRelativeY() + relativeY, selectedU, 8.0F, 8, 8, 16, 16);
      } else {
         boolean hovered = this.mouseOver(mouseX, mouseY, relativeX, relativeY, 8, 8);
         guiGraphics.m_280163_(RadioButton.RADIO, this.getRelativeX() + relativeX, this.getRelativeY() + relativeY, hovered ? 8.0F : 0.0F, 0.0F, 8, 8, 16, 16);
      }
   }

   @Override
   protected void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      int textColor = this.parent.screenTextColor();
      this.parent.drawTextWithScale(guiGraphics, this.data.getDescription(), this.getRelativeX() + 3, this.getRelativeY(), textColor, 0.8F);
      this.parent
         .drawTextWithScale(guiGraphics, MekanismLang.TRUE.translate(new Object[0]), this.getRelativeX() + 16, this.getRelativeY() + 11, textColor, 0.8F);
      this.parent
         .drawTextWithScale(guiGraphics, MekanismLang.FALSE.translate(new Object[0]), this.getRelativeX() + 62, this.getRelativeY() + 11, textColor, 0.8F);
   }

   @Override
   protected void click(double mouseX, double mouseY) {
      if (this.data.get()) {
         if (this.mouseOver(mouseX, mouseY, 50, 11, 8, 8)) {
            this.setDataFromClick(false);
         }
      } else if (this.mouseOver(mouseX, mouseY, 4, 11, 8, 8)) {
         this.setDataFromClick(true);
      }
   }

   private void setDataFromClick(boolean value) {
      this.setData(this.data, value);
      Minecraft.m_91087_().m_91106_().m_120367_(SimpleSoundInstance.m_119752_(MekanismSounds.BEEP.get(), 1.0F));
   }
}
