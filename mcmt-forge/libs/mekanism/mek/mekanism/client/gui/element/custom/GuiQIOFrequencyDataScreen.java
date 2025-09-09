package mekanism.client.gui.element.custom;

import java.util.function.Supplier;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiBar;
import mekanism.client.gui.element.bar.GuiDigitalBar;
import mekanism.common.MekanismLang;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class GuiQIOFrequencyDataScreen extends GuiInnerScreen {
   private final Supplier<QIOFrequency> frequencySupplier;

   public GuiQIOFrequencyDataScreen(IGuiWrapper gui, int x, int y, int width, int height, Supplier<QIOFrequency> frequencySupplier) {
      super(gui, x, y, width, height);
      this.frequencySupplier = frequencySupplier;
      this.f_93623_ = true;
      this.addChild(
         new GuiDigitalBar(
            gui,
            new GuiBar.IBarInfoHandler() {
               @Override
               public double getLevel() {
                  QIOFrequency freq = frequencySupplier.get();
                  return freq == null ? 0.0 : (double)freq.getTotalItemCount() / freq.getTotalItemCountCapacity();
               }

               @Override
               public Component getTooltip() {
                  QIOFrequency freq = frequencySupplier.get();
                  return freq == null
                     ? null
                     : MekanismLang.QIO_ITEMS_DETAIL
                        .translateColored(
                           EnumColor.GRAY,
                           new Object[]{EnumColor.INDIGO, TextUtils.format(freq.getTotalItemCount()), TextUtils.format(freq.getTotalItemCountCapacity())}
                        );
               }
            },
            this.relativeX + width / 4 - 25,
            this.relativeY + 20,
            50
         )
      );
      this.addChild(
         new GuiDigitalBar(
            gui,
            new GuiBar.IBarInfoHandler() {
               @Override
               public double getLevel() {
                  QIOFrequency freq = frequencySupplier.get();
                  return freq == null ? 0.0 : (double)freq.getTotalItemTypes(true) / freq.getTotalItemTypeCapacity();
               }

               @Override
               public Component getTooltip() {
                  QIOFrequency freq = frequencySupplier.get();
                  return freq == null
                     ? null
                     : MekanismLang.QIO_ITEMS_DETAIL
                        .translateColored(
                           EnumColor.GRAY,
                           new Object[]{
                              EnumColor.INDIGO, TextUtils.format((long)freq.getTotalItemTypes(true)), TextUtils.format((long)freq.getTotalItemTypeCapacity())
                           }
                        );
               }
            },
            this.relativeX + 3 * width / 4 - 25,
            this.relativeY + 20,
            50
         )
      );
   }

   @Override
   public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderForeground(guiGraphics, mouseX, mouseY);
      QIOFrequency freq = this.frequencySupplier.get();
      if (freq != null) {
         this.drawScaledTextScaledBound(
            guiGraphics,
            MekanismLang.FREQUENCY.translate(new Object[]{freq.getName()}),
            this.relativeX + 5,
            this.relativeY + 5,
            this.screenTextColor(),
            this.f_93618_ - 10,
            0.8F
         );
      }

      this.drawScaledCenteredText(
         guiGraphics, MekanismLang.QIO_ITEMS.translate(new Object[0]), this.relativeX + this.f_93618_ / 4, this.relativeY + 32, this.screenTextColor(), 0.8F
      );
      this.drawScaledCenteredText(
         guiGraphics,
         MekanismLang.QIO_TYPES.translate(new Object[0]),
         this.relativeX + 3 * this.f_93618_ / 4,
         this.relativeY + 32,
         this.screenTextColor(),
         0.8F
      );
   }
}
