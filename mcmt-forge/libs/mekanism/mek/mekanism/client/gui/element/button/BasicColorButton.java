package mekanism.client.gui.element.button;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.lib.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BasicColorButton extends MekanismButton {
   private final Supplier<EnumColor> colorSupplier;

   public static BasicColorButton toggle(
      IGuiWrapper gui, int x, int y, int size, EnumColor color, BooleanSupplier toggled, @NotNull Runnable onLeftClick, @Nullable GuiElement.IHoverable onHover
   ) {
      return new BasicColorButton(gui, x, y, size, () -> toggled.getAsBoolean() ? color : null, onLeftClick, onLeftClick, onHover);
   }

   public static BasicColorButton renderActive(
      IGuiWrapper gui,
      int x,
      int y,
      int size,
      EnumColor color,
      @NotNull Runnable onLeftClick,
      @NotNull Runnable onRightClick,
      @Nullable GuiElement.IHoverable onHover
   ) {
      return new BasicColorButton(gui, x, y, size, () -> color, onLeftClick, onRightClick, onHover);
   }

   public BasicColorButton(
      IGuiWrapper gui,
      int x,
      int y,
      int size,
      Supplier<EnumColor> color,
      @NotNull Runnable onLeftClick,
      @Nullable Runnable onRightClick,
      @Nullable GuiElement.IHoverable onHover
   ) {
      super(gui, x, y, size, size, Component.m_237119_(), onLeftClick, onRightClick, onHover);
      this.colorSupplier = color;
   }

   @Override
   public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      EnumColor color = this.getColor();
      boolean doColor = color != null && color != EnumColor.GRAY;
      if (doColor) {
         Color c = Color.rgb(color.getRgbCode());
         double[] hsv = c.hsvArray();
         hsv[1] = Math.max(0.0, hsv[1] - 0.25);
         hsv[2] = Math.min(1.0, hsv[2] + 0.4F);
         MekanismRenderer.color(guiGraphics, Color.hsv(hsv[0], hsv[1], hsv[2]));
      } else {
         MekanismRenderer.resetColor(guiGraphics);
      }

      super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
      if (doColor) {
         MekanismRenderer.resetColor(guiGraphics);
      }
   }

   @Override
   protected boolean resetColorBeforeRender() {
      return false;
   }

   public EnumColor getColor() {
      return this.colorSupplier.get();
   }
}
