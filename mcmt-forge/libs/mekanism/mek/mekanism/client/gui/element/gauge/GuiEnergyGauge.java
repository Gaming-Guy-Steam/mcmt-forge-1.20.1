package mekanism.client.gui.element.gauge;

import java.util.Collections;
import java.util.List;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;

public class GuiEnergyGauge extends GuiGauge<Void> {
   private final GuiEnergyGauge.IEnergyInfoHandler infoHandler;

   public GuiEnergyGauge(IEnergyContainer container, GaugeType type, IGuiWrapper gui, int x, int y) {
      this(new GuiEnergyGauge.IEnergyInfoHandler() {
         @Override
         public FloatingLong getEnergy() {
            return container.getEnergy();
         }

         @Override
         public FloatingLong getMaxEnergy() {
            return container.getMaxEnergy();
         }
      }, type, gui, x, y);
   }

   public GuiEnergyGauge(GuiEnergyGauge.IEnergyInfoHandler handler, GaugeType type, IGuiWrapper gui, int x, int y) {
      super(type, gui, x, y);
      this.infoHandler = handler;
   }

   public GuiEnergyGauge(GuiEnergyGauge.IEnergyInfoHandler handler, GaugeType type, IGuiWrapper gui, int x, int y, int sizeX, int sizeY) {
      super(type, gui, x, y, sizeX, sizeY);
      this.infoHandler = handler;
   }

   public static GuiEnergyGauge getDummy(GaugeType type, IGuiWrapper gui, int x, int y) {
      GuiEnergyGauge gauge = new GuiEnergyGauge(null, type, gui, x, y, type.getGaugeOverlay().getWidth() + 2, type.getGaugeOverlay().getHeight() + 2);
      gauge.dummy = true;
      return gauge;
   }

   @Override
   public TransmissionType getTransmission() {
      return TransmissionType.ENERGY;
   }

   @Override
   public int getScaledLevel() {
      if (this.dummy) {
         return this.f_93619_ - 2;
      } else if (this.infoHandler.getEnergy().equals(FloatingLong.ZERO)) {
         return 0;
      } else {
         return this.infoHandler.getEnergy().equals(FloatingLong.MAX_VALUE)
            ? this.f_93619_ - 2
            : (int)((this.f_93619_ - 2) * this.infoHandler.getEnergy().divideToLevel(this.infoHandler.getMaxEnergy()));
      }
   }

   @Override
   public TextureAtlasSprite getIcon() {
      return MekanismRenderer.energyIcon;
   }

   @Override
   public Component getLabel() {
      return null;
   }

   @Override
   public List<Component> getTooltipText() {
      if (this.dummy) {
         return Collections.emptyList();
      } else {
         return this.infoHandler.getEnergy().isZero()
            ? Collections.singletonList(MekanismLang.EMPTY.translate(new Object[0]))
            : Collections.singletonList(EnergyDisplay.of(this.infoHandler.getEnergy(), this.infoHandler.getMaxEnergy()).getTextComponent());
      }
   }

   public interface IEnergyInfoHandler {
      FloatingLong getEnergy();

      FloatingLong getMaxEnergy();
   }
}
