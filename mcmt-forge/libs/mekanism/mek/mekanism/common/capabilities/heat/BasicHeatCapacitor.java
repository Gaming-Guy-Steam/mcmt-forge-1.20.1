package mekanism.common.capabilities.heat;

import java.util.function.DoubleSupplier;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class BasicHeatCapacitor implements IHeatCapacitor {
   @Nullable
   private final IContentsListener listener;
   private double heatCapacity;
   @Nullable
   private final DoubleSupplier ambientTempSupplier;
   private final double inverseConductionCoefficient;
   private final double inverseInsulationCoefficient;
   private double storedHeat = -1.0;
   private double heatToHandle;

   public static BasicHeatCapacitor create(double heatCapacity, @Nullable DoubleSupplier ambientTempSupplier, @Nullable IContentsListener listener) {
      return create(heatCapacity, 1.0, 0.0, ambientTempSupplier, listener);
   }

   public static BasicHeatCapacitor create(
      double heatCapacity,
      double inverseConductionCoefficient,
      double inverseInsulationCoefficient,
      @Nullable DoubleSupplier ambientTempSupplier,
      @Nullable IContentsListener listener
   ) {
      if (heatCapacity < 1.0) {
         throw new IllegalArgumentException("Heat capacity must be at least one");
      } else if (inverseConductionCoefficient < 1.0) {
         throw new IllegalArgumentException("Inverse conduction coefficient must be at least one");
      } else {
         return new BasicHeatCapacitor(heatCapacity, inverseConductionCoefficient, inverseInsulationCoefficient, ambientTempSupplier, listener);
      }
   }

   protected BasicHeatCapacitor(
      double heatCapacity,
      double inverseConductionCoefficient,
      double inverseInsulationCoefficient,
      @Nullable DoubleSupplier ambientTempSupplier,
      @Nullable IContentsListener listener
   ) {
      this.heatCapacity = heatCapacity;
      this.inverseConductionCoefficient = inverseConductionCoefficient;
      this.inverseInsulationCoefficient = inverseInsulationCoefficient;
      this.ambientTempSupplier = ambientTempSupplier;
      this.listener = listener;
   }

   private void initStoredHeat() {
      if (this.storedHeat == -1.0) {
         this.storedHeat = this.heatCapacity * this.getAmbientTemperature();
      }
   }

   protected double getAmbientTemperature() {
      return this.ambientTempSupplier == null ? 300.0 : this.ambientTempSupplier.getAsDouble();
   }

   @Override
   public double getTemperature() {
      return this.getHeat() / this.getHeatCapacity();
   }

   @Override
   public double getInverseConduction() {
      return this.inverseConductionCoefficient;
   }

   @Override
   public double getInverseInsulation() {
      return this.inverseInsulationCoefficient;
   }

   @Override
   public double getHeatCapacity() {
      return this.heatCapacity;
   }

   @Override
   public void onContentsChanged() {
      if (this.listener != null) {
         this.listener.onContentsChanged();
      }
   }

   @Override
   public void handleHeat(double transfer) {
      this.heatToHandle += transfer;
   }

   public void update() {
      if (this.heatToHandle != 0.0 && Math.abs(this.heatToHandle) > 1.0E-6F) {
         this.initStoredHeat();
         this.storedHeat = this.storedHeat + this.heatToHandle;
         this.onContentsChanged();
         this.heatToHandle = 0.0;
      }
   }

   public void deserializeNBT(CompoundTag nbt) {
      NBTUtils.setDoubleIfPresent(nbt, "stored", heat -> this.storedHeat = heat);
      NBTUtils.setDoubleIfPresent(nbt, "heatCapacity", capacity -> this.setHeatCapacity(capacity, false));
   }

   @Override
   public CompoundTag serializeNBT() {
      CompoundTag nbt = new CompoundTag();
      nbt.m_128347_("stored", this.getHeat());
      nbt.m_128347_("heatCapacity", this.getHeatCapacity());
      return nbt;
   }

   @Override
   public double getHeat() {
      this.initStoredHeat();
      return this.storedHeat;
   }

   @Override
   public void setHeat(double heat) {
      if (this.getHeat() != heat) {
         this.storedHeat = heat;
         this.onContentsChanged();
      }
   }

   public void setHeatCapacity(double newCapacity, boolean updateHeat) {
      if (updateHeat && this.storedHeat != -1.0) {
         this.setHeat(this.getHeat() + (newCapacity - this.getHeatCapacity()) * this.getAmbientTemperature());
      }

      this.heatCapacity = newCapacity;
   }

   public void setHeatCapacityFromPacket(double newCapacity) {
      this.heatCapacity = newCapacity;
   }
}
