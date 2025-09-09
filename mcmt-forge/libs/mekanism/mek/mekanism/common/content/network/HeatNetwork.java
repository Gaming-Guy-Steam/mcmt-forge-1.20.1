package mekanism.common.content.network;

import java.util.Collection;
import java.util.UUID;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.IHeatHandler;
import mekanism.common.MekanismLang;
import mekanism.common.content.network.transmitter.ThermodynamicConductor;
import mekanism.common.lib.transmitter.DynamicNetwork;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class HeatNetwork extends DynamicNetwork<IHeatHandler, HeatNetwork, ThermodynamicConductor> {
   private double meanTemp = 300.0;
   private double heatLost;
   private double heatTransferred;

   public HeatNetwork(UUID networkID) {
      super(networkID);
   }

   public HeatNetwork(Collection<HeatNetwork> networks) {
      this(UUID.randomUUID());
      this.adoptAllAndRegister(networks);
   }

   @Override
   public Component getStoredInfo() {
      return MekanismLang.HEAT_NETWORK_STORED
         .translate(new Object[]{MekanismUtils.getTemperatureDisplay(this.meanTemp, UnitDisplayUtils.TemperatureUnit.KELVIN, true)});
   }

   @Override
   public Component getFlowInfo() {
      Component transferred = MekanismUtils.getTemperatureDisplay(this.heatTransferred, UnitDisplayUtils.TemperatureUnit.KELVIN, false);
      Component lost = MekanismUtils.getTemperatureDisplay(this.heatLost, UnitDisplayUtils.TemperatureUnit.KELVIN, false);
      return this.heatTransferred + this.heatLost == 0.0
         ? MekanismLang.HEAT_NETWORK_FLOW.translate(new Object[]{transferred, lost})
         : MekanismLang.HEAT_NETWORK_FLOW_EFFICIENCY
            .translate(
               new Object[]{
                  transferred,
                  lost,
                  MekanismLang.GENERIC_PERCENT
                     .translate(new Object[]{(float)Math.round(this.heatTransferred / (this.heatTransferred + this.heatLost) * 10000.0) / 100.0F})
               }
            );
   }

   @Override
   public void onUpdate() {
      super.onUpdate();
      double newSumTemp = 0.0;
      double newHeatLost = 0.0;
      double newHeatTransferred = 0.0;

      for (ThermodynamicConductor transmitter : this.transmitters) {
         HeatAPI.HeatTransfer transfer = transmitter.simulate();
         newHeatTransferred += transfer.adjacentTransfer();
         newHeatLost += transfer.environmentTransfer();
      }

      for (ThermodynamicConductor transmitter : this.transmitters) {
         transmitter.updateHeatCapacitors(null);
         newSumTemp += transmitter.getTotalTemperature();
      }

      this.heatLost = newHeatLost;
      this.heatTransferred = newHeatTransferred;
      this.meanTemp = newSumTemp / this.transmittersSize();
   }

   @Override
   public String toString() {
      return "[HeatNetwork] " + this.transmittersSize() + " transmitters, " + this.getAcceptorCount() + " acceptors.";
   }

   @NotNull
   @Override
   public Component getTextComponent() {
      return MekanismLang.NETWORK_DESCRIPTION.translate(new Object[]{MekanismLang.HEAT_NETWORK, this.transmittersSize(), this.getAcceptorCount()});
   }
}
