package mekanism.common.capabilities.proxy;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.heat.IHeatHandler;
import mekanism.api.heat.ISidedHeatHandler;
import mekanism.common.capabilities.holder.IHolder;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ProxyHeatHandler extends ProxyHandler implements IHeatHandler {
   private final ISidedHeatHandler heatHandler;

   public ProxyHeatHandler(ISidedHeatHandler heatHandler, @Nullable Direction side, @Nullable IHolder holder) {
      super(side, holder);
      this.heatHandler = heatHandler;
   }

   @Override
   public int getHeatCapacitorCount() {
      return this.heatHandler.getHeatCapacitorCount(this.side);
   }

   @Override
   public double getTemperature(int capacitor) {
      return this.heatHandler.getTemperature(capacitor, this.side);
   }

   @Override
   public double getInverseConduction(int capacitor) {
      return this.heatHandler.getInverseConduction(capacitor, this.side);
   }

   @Override
   public double getHeatCapacity(int capacitor) {
      return this.heatHandler.getHeatCapacity(capacitor, this.side);
   }

   @Override
   public void handleHeat(int capacitor, double transfer) {
      if (!this.readOnly) {
         this.heatHandler.handleHeat(capacitor, transfer, this.side);
      }
   }

   @Override
   public double getTotalTemperature() {
      return this.heatHandler.getTotalTemperature(this.side);
   }

   @Override
   public double getTotalInverseConduction() {
      return this.heatHandler.getTotalInverseConductionCoefficient(this.side);
   }

   @Override
   public double getTotalHeatCapacity() {
      return this.heatHandler.getTotalHeatCapacity(this.side);
   }

   @Override
   public void handleHeat(double transfer) {
      if (!this.readOnly) {
         this.heatHandler.handleHeat(transfer, this.side);
      }
   }
}
