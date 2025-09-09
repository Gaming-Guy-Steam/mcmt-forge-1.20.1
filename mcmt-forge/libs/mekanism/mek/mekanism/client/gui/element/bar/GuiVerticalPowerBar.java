package mekanism.client.gui.element.bar;

import mekanism.api.energy.IEnergyContainer;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class GuiVerticalPowerBar extends GuiBar<GuiBar.IBarInfoHandler> {
   private static final ResourceLocation ENERGY_BAR = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_BAR, "vertical_power.png");
   private static final int texWidth = 4;
   private static final int texHeight = 52;
   private final double heightScale;

   public GuiVerticalPowerBar(IGuiWrapper gui, IEnergyContainer container, int x, int y) {
      this(gui, container, x, y, 52);
   }

   public GuiVerticalPowerBar(IGuiWrapper gui, IEnergyContainer container, int x, int y, int desiredHeight) {
      this(gui, new GuiBar.IBarInfoHandler() {
         @Override
         public Component getTooltip() {
            return EnergyDisplay.of(container).getTextComponent();
         }

         @Override
         public double getLevel() {
            return container.getEnergy().divideToLevel(container.getMaxEnergy());
         }
      }, x, y, desiredHeight);
   }

   public GuiVerticalPowerBar(IGuiWrapper gui, GuiBar.IBarInfoHandler handler, int x, int y) {
      this(gui, handler, x, y, 52);
   }

   public GuiVerticalPowerBar(IGuiWrapper gui, GuiBar.IBarInfoHandler handler, int x, int y, int desiredHeight) {
      super(ENERGY_BAR, gui, handler, x, y, 4, desiredHeight, false);
      this.heightScale = desiredHeight / 52.0;
   }

   @Override
   protected void renderBarOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, double handlerLevel) {
      int displayInt = (int)(handlerLevel * 52.0);
      if (displayInt > 0) {
         int scaled = calculateScaled(this.heightScale, displayInt);
         guiGraphics.m_280411_(this.getResource(), this.relativeX + 1, this.relativeY + this.f_93619_ - 1 - scaled, 4, scaled, 0.0F, 0.0F, 4, displayInt, 4, 52);
      }
   }
}
