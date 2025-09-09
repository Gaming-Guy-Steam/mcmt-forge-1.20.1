package mekanism.common.capabilities.heat;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IHeatHandler;
import mekanism.api.heat.IMekanismHeatHandler;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public interface ITileHeatHandler extends IMekanismHeatHandler {
   default void updateHeatCapacitors(@Nullable Direction side) {
      for (IHeatCapacitor capacitor : this.getHeatCapacitors(side)) {
         if (capacitor instanceof BasicHeatCapacitor heatCapacitor) {
            heatCapacitor.update();
         }
      }
   }

   @Nullable
   default IHeatHandler getAdjacent(Direction side) {
      return null;
   }

   default HeatAPI.HeatTransfer simulate() {
      return new HeatAPI.HeatTransfer(this.simulateAdjacent(), this.simulateEnvironment());
   }

   default double getAmbientTemperature(Direction side) {
      return 300.0;
   }

   default double simulateEnvironment() {
      double environmentTransfer = 0.0;

      for (Direction side : EnumUtils.DIRECTIONS) {
         double heatCapacity = this.getTotalHeatCapacity(side);
         double invConduction = 10000.0 + this.getTotalInverseInsulation(side) + this.getTotalInverseConductionCoefficient(side);
         double tempToTransfer = (this.getTotalTemperature(side) - this.getAmbientTemperature(side)) / invConduction;
         this.handleHeat(-tempToTransfer * heatCapacity, side);
         if (tempToTransfer > 0.0) {
            environmentTransfer += tempToTransfer;
         }
      }

      return environmentTransfer;
   }

   default double simulateAdjacent() {
      double adjacentTransfer = 0.0;

      for (Direction side : EnumUtils.DIRECTIONS) {
         IHeatHandler sink = this.getAdjacent(side);
         if (sink != null) {
            double heatCapacity = this.getTotalHeatCapacity(side);
            double invConduction = sink.getTotalInverseConduction() + this.getTotalInverseConductionCoefficient(side);
            double tempToTransfer = (this.getTotalTemperature(side) - this.getAmbientTemperature(side)) / invConduction;
            double heatToTransfer = tempToTransfer * heatCapacity;
            this.handleHeat(-heatToTransfer, side);
            sink.handleHeat(heatToTransfer);
            adjacentTransfer = this.incrementAdjacentTransfer(adjacentTransfer, tempToTransfer, side);
         }
      }

      return adjacentTransfer;
   }

   default double incrementAdjacentTransfer(double currentAdjacentTransfer, double tempToTransfer, Direction side) {
      return currentAdjacentTransfer + tempToTransfer;
   }
}
