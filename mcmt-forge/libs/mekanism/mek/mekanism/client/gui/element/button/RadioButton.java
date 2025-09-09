package mekanism.client.gui.element.button;

import java.util.function.BooleanSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.common.registries.MekanismSounds;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RadioButton extends MekanismButton {
   public static final ResourceLocation RADIO = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "radio_button.png");
   public static final int RADIO_SIZE = 8;
   private final BooleanSupplier toggled;

   public RadioButton(IGuiWrapper gui, int x, int y, BooleanSupplier toggled, @NotNull Runnable onPress, @Nullable GuiElement.IHoverable onHover) {
      super(gui, x, y, 8, 8, Component.m_237119_(), onPress, onHover);
      this.toggled = toggled;
      this.clickSound = MekanismSounds.BEEP;
   }

   @Override
   public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
      if (this.toggled.getAsBoolean()) {
         guiGraphics.m_280163_(RADIO, this.getButtonX(), this.getButtonY(), 0.0F, 8.0F, this.getButtonWidth(), this.getButtonHeight(), 16, 16);
      } else {
         int uOffset = this.checkWindows(mouseX, mouseY, this.m_198029_()) ? 8 : 0;
         guiGraphics.m_280163_(RADIO, this.getButtonX(), this.getButtonY(), uOffset, 0.0F, this.getButtonWidth(), this.getButtonHeight(), 16, 16);
      }
   }
}
