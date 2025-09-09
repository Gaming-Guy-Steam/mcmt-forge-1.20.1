package mekanism.api.heat;

import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public interface ISidedHeatHandler extends IHeatHandler {
   @Nullable
   default Direction getHeatSideFor() {
      return null;
   }

   int getHeatCapacitorCount(@Nullable Direction var1);

   @Override
   default int getHeatCapacitorCount() {
      return this.getHeatCapacitorCount(this.getHeatSideFor());
   }

   double getTemperature(int var1, @Nullable Direction var2);

   @Override
   default double getTemperature(int capacitor) {
      return this.getTemperature(capacitor, this.getHeatSideFor());
   }

   double getInverseConduction(int var1, @Nullable Direction var2);

   @Override
   default double getInverseConduction(int capacitor) {
      return this.getInverseConduction(capacitor, this.getHeatSideFor());
   }

   double getHeatCapacity(int var1, @Nullable Direction var2);

   @Override
   default double getHeatCapacity(int capacitor) {
      return this.getHeatCapacity(capacitor, this.getHeatSideFor());
   }

   void handleHeat(int var1, double var2, @Nullable Direction var4);

   @Override
   default void handleHeat(int capacitor, double transfer) {
      this.handleHeat(capacitor, transfer, this.getHeatSideFor());
   }

   default double getTotalTemperature(@Nullable Direction side) {
      int heatCapacitorCount = this.getHeatCapacitorCount(side);
      if (heatCapacitorCount == 1) {
         return this.getTemperature(0, side);
      } else {
         double sum = 0.0;
         double totalCapacity = this.getTotalHeatCapacity(side);

         for (int capacitor = 0; capacitor < heatCapacitorCount; capacitor++) {
            sum += this.getTemperature(capacitor, side) * (this.getHeatCapacity(capacitor, side) / totalCapacity);
         }

         return sum;
      }
   }

   default double getTotalInverseConductionCoefficient(@Nullable Direction side) {
      int heatCapacitorCount = this.getHeatCapacitorCount(side);
      if (heatCapacitorCount == 0) {
         return 1.0;
      } else if (heatCapacitorCount == 1) {
         return this.getInverseConduction(0, side);
      } else {
         double sum = 0.0;
         double totalCapacity = this.getTotalHeatCapacity(side);

         for (int capacitor = 0; capacitor < heatCapacitorCount; capacitor++) {
            sum += this.getInverseConduction(capacitor, side) * (this.getHeatCapacity(capacitor, side) / totalCapacity);
         }

         return sum;
      }
   }

   default double getTotalHeatCapacity(@Nullable Direction side) {
      int heatCapacitorCount = this.getHeatCapacitorCount(side);
      if (heatCapacitorCount == 1) {
         return this.getHeatCapacity(0, side);
      } else {
         double sum = 0.0;

         for (int capacitor = 0; capacitor < heatCapacitorCount; capacitor++) {
            sum += this.getHeatCapacity(capacitor, side);
         }

         return sum;
      }
   }

   default void handleHeat(double transfer, @Nullable Direction side) {
      int heatCapacitorCount = this.getHeatCapacitorCount(side);
      if (heatCapacitorCount == 1) {
         this.handleHeat(0, transfer, side);
      } else {
         double totalHeatCapacity = this.getTotalHeatCapacity(side);

         for (int capacitor = 0; capacitor < heatCapacitorCount; capacitor++) {
            this.handleHeat(capacitor, transfer * (this.getHeatCapacity(capacitor, side) / totalHeatCapacity), side);
         }
      }
   }
}
