package mekanism.client.gui.element;

import java.util.function.BooleanSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.MekanismLang;
import mekanism.common.registries.MekanismSounds;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class GuiDigitalSwitch extends GuiTexturedElement {
   public static final ResourceLocation SWITCH = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "switch/switch.png");
   public static final int BUTTON_SIZE_X = 15;
   public static final int BUTTON_SIZE_Y = 8;
   private final GuiDigitalSwitch.SwitchType type;
   private final ResourceLocation icon;
   private final BooleanSupplier stateSupplier;
   private final Component tooltip;
   private final Runnable onToggle;

   public GuiDigitalSwitch(
      IGuiWrapper gui,
      int x,
      int y,
      ResourceLocation icon,
      BooleanSupplier stateSupplier,
      Component tooltip,
      Runnable onToggle,
      GuiDigitalSwitch.SwitchType type
   ) {
      super(SWITCH, gui, x, y, type.width, type.height);
      this.type = type;
      this.icon = icon;
      this.stateSupplier = stateSupplier;
      this.tooltip = tooltip;
      this.onToggle = onToggle;
      this.clickSound = MekanismSounds.BEEP;
   }

   @Override
   public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderToolTip(guiGraphics, mouseX, mouseY);
      this.displayTooltips(guiGraphics, mouseX, mouseY, new Component[]{this.tooltip});
   }

   @Override
   public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
      boolean state = this.stateSupplier.getAsBoolean();
      guiGraphics.m_280163_(
         this.getResource(), this.relativeX + this.type.switchX, this.relativeY + this.type.switchY, 0.0F, state ? 0.0F : 8.0F, 15, 8, 15, 16
      );
      guiGraphics.m_280163_(
         this.getResource(), this.relativeX + this.type.switchX, this.relativeY + this.type.switchY + 8 + 1, 0.0F, state ? 8.0F : 0.0F, 15, 8, 15, 16
      );
      guiGraphics.m_280163_(this.icon, this.relativeX + this.type.iconX, this.relativeY + this.type.iconY, 0.0F, 0.0F, 5, 5, 5, 5);
   }

   @Override
   public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderForeground(guiGraphics, mouseX, mouseY);
      this.drawScaledCenteredText(
         guiGraphics, MekanismLang.ON.translate(new Object[0]), this.relativeX + this.type.switchX + 8, this.relativeY + this.type.switchY, 1052688, 0.5F
      );
      this.drawScaledCenteredText(
         guiGraphics, MekanismLang.OFF.translate(new Object[0]), this.relativeX + this.type.switchX + 8, this.relativeY + this.type.switchY + 9, 1052688, 0.5F
      );
   }

   @Override
   public void onClick(double mouseX, double mouseY, int button) {
      this.onToggle.run();
   }

   public static enum SwitchType {
      LOWER_ICON(15, 31, 0, 0, 5, 21),
      LEFT_ICON(30, 16, 15, 0, 5, 6);

      private final int iconX;
      private final int iconY;
      private final int width;
      private final int height;
      private final int switchX;
      private final int switchY;

      private SwitchType(int width, int height, int switchX, int switchY, int iconX, int iconY) {
         this.width = width;
         this.height = height;
         this.iconX = iconX;
         this.iconY = iconY;
         this.switchX = switchX;
         this.switchY = switchY;
      }
   }
}
