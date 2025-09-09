package mekanism.client.gui.element;

import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.GuiColorWindow;
import mekanism.common.MekanismLang;
import mekanism.common.lib.Color;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class GuiColorPickerSlot extends GuiElement {
   private final Supplier<Color> supplier;
   private final Consumer<Color> consumer;
   private final boolean handlesAlpha;

   public GuiColorPickerSlot(IGuiWrapper gui, int x, int y, boolean handlesAlpha, Supplier<Color> supplier, Consumer<Color> consumer) {
      super(gui, x, y, 18, 18);
      this.handlesAlpha = handlesAlpha;
      this.supplier = supplier;
      this.consumer = consumer;
      this.addChild(new GuiElementHolder(gui, this.relativeX, this.relativeY, 18, 18));
   }

   @Override
   public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderToolTip(guiGraphics, mouseX, mouseY);
      Component hex = MekanismLang.GENERIC_HEX.translateColored(EnumColor.GRAY, new Object[]{TextUtils.hex(false, 3, this.supplier.get().rgb())});
      this.displayTooltips(guiGraphics, mouseX, mouseY, new Component[]{hex});
   }

   @Override
   public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderForeground(guiGraphics, mouseX, mouseY);
      GuiUtils.fill(guiGraphics, this.relativeX + 1, this.relativeY + 1, this.f_93618_ - 2, this.f_93619_ - 2, this.supplier.get().argb());
   }

   @Override
   public void onClick(double mouseX, double mouseY, int button) {
      GuiColorWindow window = new GuiColorWindow(this.gui(), this.getGuiWidth() / 2 - 80, this.getGuiHeight() / 2 - 60, this.handlesAlpha, this.consumer);
      window.setColor(this.supplier.get());
      this.gui().addWindow(window);
   }
}
