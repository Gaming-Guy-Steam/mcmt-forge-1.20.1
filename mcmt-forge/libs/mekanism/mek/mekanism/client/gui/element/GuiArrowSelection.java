package mekanism.client.gui.element;

import java.util.function.Supplier;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class GuiArrowSelection extends GuiTexturedElement {
   private static final ResourceLocation ARROW = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "arrow_selection.png");
   private final Supplier<Component> textComponentSupplier;

   public GuiArrowSelection(IGuiWrapper gui, int x, int y, Supplier<Component> textComponentSupplier) {
      super(ARROW, gui, x, y, 33, 19);
      this.textComponentSupplier = textComponentSupplier;
   }

   @Override
   public boolean m_5953_(double xAxis, double yAxis) {
      return this.f_93623_
         && this.f_93624_
         && xAxis >= this.m_252754_() + 16
         && xAxis < this.m_252754_() + this.f_93618_ - 1
         && yAxis >= this.m_252907_() + 1
         && yAxis < this.m_252907_() + this.f_93619_ - 1;
   }

   @Override
   public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderToolTip(guiGraphics, mouseX, mouseY);
      Component component = this.textComponentSupplier.get();
      if (component != null) {
         int tooltipX = mouseX + 5;
         int tooltipY = mouseY - 5;
         GuiUtils.renderBackgroundTexture(
            guiGraphics,
            GuiInnerScreen.SCREEN,
            GuiInnerScreen.SCREEN_SIZE,
            GuiInnerScreen.SCREEN_SIZE,
            tooltipX - 3,
            tooltipY - 4,
            this.getStringWidth(component) + 6,
            16,
            256,
            256
         );
         this.drawString(guiGraphics, component, tooltipX, tooltipY, this.screenTextColor());
      }
   }

   @Override
   public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
      guiGraphics.m_280163_(this.getResource(), this.relativeX, this.relativeY, 0.0F, 0.0F, this.f_93618_, this.f_93619_, this.f_93618_, this.f_93619_);
   }
}
