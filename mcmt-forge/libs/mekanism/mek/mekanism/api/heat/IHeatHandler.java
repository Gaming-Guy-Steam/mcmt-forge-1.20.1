package mekanism.api.heat;

import mekanism.api.annotations.NothingNullByDefault;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@AutoRegisterCapability
@NothingNullByDefault
public interface IHeatHandler {
   int getHeatCapacitorCount();

   double getTemperature(int var1);

   double getInverseConduction(int var1);

   double getHeatCapacity(int var1);

   void handleHeat(int var1, double var2);

   default double getTotalTemperature() {
      int heatCapacitorCount = this.getHeatCapacitorCount();
      if (heatCapacitorCount == 1) {
         return this.getTemperature(0);
      } else {
         double sum = 0.0;
         double totalCapacity = this.getTotalHeatCapacity();

         for (int capacitor = 0; capacitor < heatCapacitorCount; capacitor++) {
            sum += this.getTemperature(capacitor) * (this.getHeatCapacity(capacitor) / totalCapacity);
         }

         return sum;
      }
   }

   default double getTotalInverseConduction() {
      int heatCapacitorCount = this.getHeatCapacitorCount();
      if (heatCapacitorCount == 0) {
         return 1.0;
      } else if (heatCapacitorCount == 1) {
         return this.getInverseConduction(0);
      } else {
         double sum = 0.0;
         double totalCapacity = this.getTotalHeatCapacity();

         for (int capacitor = 0; capacitor < heatCapacitorCount; capacitor++) {
            sum += this.getInverseConduction(capacitor) * (this.getHeatCapacity(capacitor) / totalCapacity);
         }

         return sum;
      }
   }

   default double getTotalHeatCapacity() {
      int heatCapacitorCount = this.getHeatCapacitorCount();
      if (heatCapacitorCount == 1) {
         return this.getHeatCapacity(0);
      } else {
         double sum = 0.0;

         for (int capacitor = 0; capacitor < heatCapacitorCount; capacitor++) {
            sum += this.getHeatCapacity(capacitor);
         }

         return sum;
      }
   }

   default void handleHeat(double transfer) {
      int heatCapacitorCount = this.getHeatCapacitorCount();
      if (heatCapacitorCount == 1) {
         this.handleHeat(0, transfer);
      } else {
         double totalHeatCapacity = this.getTotalHeatCapacity();

         for (int capacitor = 0; capacitor < heatCapacitorCount; capacitor++) {
            this.handleHeat(capacitor, transfer * (this.getHeatCapacity(capacitor) / totalHeatCapacity));
         }
      }
   }
}
