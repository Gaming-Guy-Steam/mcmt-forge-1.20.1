package mekanism.client.gui.element.bar;

import mekanism.api.energy.IEnergyContainer;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class GuiHorizontalPowerBar extends GuiBar<GuiBar.IBarInfoHandler> {
   private static final ResourceLocation ENERGY_BAR = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_BAR, "horizontal_power.png");
   private static final int texWidth = 52;
   private static final int texHeight = 4;
   private final double widthScale;

   public GuiHorizontalPowerBar(IGuiWrapper gui, IEnergyContainer container, int x, int y) {
      this(gui, container, x, y, 52);
   }

   public GuiHorizontalPowerBar(IGuiWrapper gui, IEnergyContainer container, int x, int y, int desiredWidth) {
      this(gui, new GuiBar.IBarInfoHandler() {
         @Override
         public Component getTooltip() {
            return EnergyDisplay.of(container).getTextComponent();
         }

         @Override
         public double getLevel() {
            return container.getEnergy().divideToLevel(container.getMaxEnergy());
         }
      }, x, y, desiredWidth);
   }

   public GuiHorizontalPowerBar(IGuiWrapper gui, GuiBar.IBarInfoHandler handler, int x, int y) {
      this(gui, handler, x, y, 52);
   }

   public GuiHorizontalPowerBar(IGuiWrapper gui, GuiBar.IBarInfoHandler handler, int x, int y, int desiredWidth) {
      super(ENERGY_BAR, gui, handler, x, y, desiredWidth, 4, true);
      this.widthScale = desiredWidth / 52.0;
   }

   @Override
   protected void renderBarOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, double handlerLevel) {
      int displayInt = (int)(handlerLevel * 52.0);
      if (displayInt > 0) {
         guiGraphics.m_280411_(
            this.getResource(), this.relativeX + 1, this.relativeY + 1, calculateScaled(this.widthScale, displayInt), 4, 0.0F, 0.0F, displayInt, 4, 52, 4
         );
      }
   }
}
