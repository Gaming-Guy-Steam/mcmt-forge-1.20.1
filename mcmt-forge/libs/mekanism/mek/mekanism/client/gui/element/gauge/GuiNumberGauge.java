package mekanism.client.gui.element.gauge;

import java.util.Collections;
import java.util.List;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.lib.transmitter.TransmissionType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;

public class GuiNumberGauge extends GuiGauge<Void> {
   private final GuiNumberGauge.INumberInfoHandler infoHandler;

   public GuiNumberGauge(GuiNumberGauge.INumberInfoHandler handler, GaugeType type, IGuiWrapper gui, int x, int y) {
      super(type, gui, x, y);
      this.infoHandler = handler;
   }

   @Override
   public TransmissionType getTransmission() {
      return null;
   }

   @Override
   public int getScaledLevel() {
      return (int)((this.f_93619_ - 2) * this.infoHandler.getScaledLevel());
   }

   @Override
   public TextureAtlasSprite getIcon() {
      return this.infoHandler.getIcon();
   }

   @Override
   public Component getLabel() {
      return null;
   }

   @Override
   public List<Component> getTooltipText() {
      return Collections.singletonList(this.infoHandler.getText());
   }

   public interface INumberInfoHandler {
      TextureAtlasSprite getIcon();

      double getLevel();

      double getScaledLevel();

      Component getText();
   }
}
