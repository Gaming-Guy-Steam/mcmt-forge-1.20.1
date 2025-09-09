package mekanism.client.gui.element.window;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiColorPickerSlot;
import mekanism.client.gui.element.GuiScreenSwitch;
import mekanism.client.gui.element.GuiSlider;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.HUDElement;
import mekanism.common.inventory.container.SelectedWindowData;
import net.minecraft.client.gui.GuiGraphics;

public class GuiMekaSuitHelmetOptions extends GuiWindow {
   public GuiMekaSuitHelmetOptions(IGuiWrapper gui, int x, int y) {
      super(gui, x, y, 140, 115, SelectedWindowData.WindowType.MEKA_SUIT_HELMET);
      this.interactionStrategy = GuiWindow.InteractionStrategy.NONE;
      this.addChild(new GuiColorPickerSlot(gui, this.relativeX + 12, this.relativeY + 32, false, HUDElement.HUDColor.REGULAR::getColor, color -> {
         MekanismConfig.client.hudColor.set(color.rgb());
         MekanismConfig.client.save();
      }));
      this.addChild(new GuiColorPickerSlot(gui, this.relativeX + 61, this.relativeY + 32, false, HUDElement.HUDColor.WARNING::getColor, color -> {
         MekanismConfig.client.hudWarningColor.set(color.rgb());
         MekanismConfig.client.save();
      }));
      this.addChild(new GuiColorPickerSlot(gui, this.relativeX + 110, this.relativeY + 32, false, HUDElement.HUDColor.DANGER::getColor, color -> {
         MekanismConfig.client.hudDangerColor.set(color.rgb());
         MekanismConfig.client.save();
      }));
      GuiSlider slider = this.addChild(
         new GuiSlider(gui, this.relativeX + 10, this.relativeY + 62, 120, value -> MekanismConfig.client.hudOpacity.set((float)value))
      );
      slider.setValue(MekanismConfig.client.hudOpacity.get());
      this.addChild(
         new GuiScreenSwitch(
            gui, this.relativeX + 7, this.relativeY + 87, 126, MekanismLang.COMPASS.translate(new Object[0]), MekanismConfig.client.hudCompassEnabled, () -> {
               MekanismConfig.client.hudCompassEnabled.set(!MekanismConfig.client.hudCompassEnabled.get());
               MekanismConfig.client.save();
            }
         )
      );
   }

   @Override
   public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderForeground(guiGraphics, mouseX, mouseY);
      this.drawTitleText(guiGraphics, MekanismLang.HELMET_OPTIONS.translate(new Object[0]), 6.0F);
      this.drawTextExact(guiGraphics, MekanismLang.HUD_OVERLAY.translate(new Object[0]), this.relativeX + 7, this.relativeY + 20, this.headingTextColor());
      this.drawScaledCenteredText(
         guiGraphics, MekanismLang.DEFAULT.translate(new Object[0]), this.relativeX + 21, this.relativeY + 52, this.subheadingTextColor(), 0.8F
      );
      this.drawScaledCenteredText(
         guiGraphics, MekanismLang.WARNING.translate(new Object[0]), this.relativeX + 70, this.relativeY + 52, this.subheadingTextColor(), 0.8F
      );
      this.drawScaledCenteredText(
         guiGraphics, MekanismLang.DANGER.translate(new Object[0]), this.relativeX + 119, this.relativeY + 52, this.subheadingTextColor(), 0.8F
      );
      this.drawScaledCenteredText(
         guiGraphics, MekanismLang.OPACITY.translate(new Object[0]), this.relativeX + 70, this.relativeY + 75, this.subheadingTextColor(), 0.8F
      );
   }
}
