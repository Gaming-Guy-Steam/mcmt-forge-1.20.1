package mekanism.client.gui.element;

import java.util.Collections;
import java.util.function.BooleanSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.MekanismLang;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class GuiScreenSwitch extends GuiInnerScreen {
   private final BooleanSupplier stateSupplier;
   private final Runnable onToggle;

   public GuiScreenSwitch(IGuiWrapper gui, int x, int y, int width, Component buttonName, BooleanSupplier stateSupplier, Runnable onToggle) {
      super(gui, x, y, width, 21, () -> Collections.singletonList(buttonName));
      this.stateSupplier = stateSupplier;
      this.onToggle = onToggle;
      this.f_93623_ = true;
      this.clickSound = MekanismSounds.BEEP;
   }

   @Override
   public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
      int buttonSizeX = 15;
      int buttonSizeY = 8;
      guiGraphics.m_280163_(
         GuiDigitalSwitch.SWITCH,
         this.relativeX + this.f_93618_ - 2 - buttonSizeX,
         this.relativeY + 2,
         0.0F,
         this.stateSupplier.getAsBoolean() ? 0.0F : buttonSizeY,
         buttonSizeX,
         buttonSizeY,
         buttonSizeX,
         buttonSizeY * 2
      );
      guiGraphics.m_280163_(
         GuiDigitalSwitch.SWITCH,
         this.relativeX + this.f_93618_ - 2 - buttonSizeX,
         this.relativeY + 2 + buttonSizeY + 1,
         0.0F,
         this.stateSupplier.getAsBoolean() ? buttonSizeY : 0.0F,
         buttonSizeX,
         buttonSizeY,
         buttonSizeX,
         buttonSizeY * 2
      );
   }

   @Override
   public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderForeground(guiGraphics, mouseX, mouseY);
      this.drawScaledCenteredText(guiGraphics, MekanismLang.ON.translate(new Object[0]), this.relativeX + this.f_93618_ - 9, this.relativeY + 2, 1052688, 0.5F);
      this.drawScaledCenteredText(
         guiGraphics, MekanismLang.OFF.translate(new Object[0]), this.relativeX + this.f_93618_ - 9, this.relativeY + 11, 1052688, 0.5F
      );
   }

   @Override
   public void onClick(double mouseX, double mouseY, int button) {
      this.onToggle.run();
   }
}
