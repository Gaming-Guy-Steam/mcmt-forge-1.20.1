package mekanism.api.heat;

import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public interface IMekanismHeatHandler extends ISidedHeatHandler, IContentsListener {
   default boolean canHandleHeat() {
      return true;
   }

   @Override
   default int getHeatCapacitorCount(@Nullable Direction side) {
      return this.getHeatCapacitors(side).size();
   }

   List<IHeatCapacitor> getHeatCapacitors(@Nullable Direction var1);

   @Nullable
   default IHeatCapacitor getHeatCapacitor(int capacitor, @Nullable Direction side) {
      List<IHeatCapacitor> capacitors = this.getHeatCapacitors(side);
      return capacitor >= 0 && capacitor < capacitors.size() ? capacitors.get(capacitor) : null;
   }

   @Override
   default double getTemperature(int capacitor, @Nullable Direction side) {
      IHeatCapacitor heatCapacitor = this.getHeatCapacitor(capacitor, side);
      return heatCapacitor == null ? 300.0 : heatCapacitor.getTemperature();
   }

   @Override
   default double getInverseConduction(int capacitor, @Nullable Direction side) {
      IHeatCapacitor heatCapacitor = this.getHeatCapacitor(capacitor, side);
      return heatCapacitor == null ? 1.0 : heatCapacitor.getInverseConduction();
   }

   @Override
   default double getHeatCapacity(int capacitor, @Nullable Direction side) {
      IHeatCapacitor heatCapacitor = this.getHeatCapacitor(capacitor, side);
      return heatCapacitor == null ? 1.0 : heatCapacitor.getHeatCapacity();
   }

   @Override
   default void handleHeat(int capacitor, double transfer, @Nullable Direction side) {
      IHeatCapacitor heatCapacitor = this.getHeatCapacitor(capacitor, side);
      if (heatCapacitor != null) {
         heatCapacitor.handleHeat(transfer);
      }
   }

   default double getInverseInsulation(int capacitor, @Nullable Direction side) {
      IHeatCapacitor heatCapacitor = this.getHeatCapacitor(capacitor, side);
      return heatCapacitor == null ? 0.0 : heatCapacitor.getInverseInsulation();
   }

   default double getTotalInverseInsulation(@Nullable Direction side) {
      int heatCapacitorCount = this.getHeatCapacitorCount(side);
      if (heatCapacitorCount == 1) {
         return this.getInverseInsulation(0, side);
      } else {
         double sum = 0.0;
         double totalCapacity = this.getTotalHeatCapacity(side);

         for (int capacitor = 0; capacitor < heatCapacitorCount; capacitor++) {
            sum += this.getInverseInsulation(capacitor, side) * (this.getHeatCapacity(capacitor, side) / totalCapacity);
         }

         return sum;
      }
   }
}
