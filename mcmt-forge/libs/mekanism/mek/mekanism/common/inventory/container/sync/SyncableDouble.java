package mekanism.common.inventory.container.sync;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import mekanism.common.network.to_client.container.property.DoublePropertyData;

public abstract class SyncableDouble implements ISyncableData {
   private double lastKnownValue;

   public abstract double get();

   public abstract void set(double value);

   @Override
   public ISyncableData.DirtyType isDirty() {
      double oldValue = this.get();
      boolean dirty = oldValue != this.lastKnownValue;
      this.lastKnownValue = oldValue;
      return ISyncableData.DirtyType.get(dirty);
   }

   public DoublePropertyData getPropertyData(short property, ISyncableData.DirtyType dirtyType) {
      return new DoublePropertyData(property, this.get());
   }

   public static SyncableDouble create(double[] doubleArray, int idx) {
      return new SyncableDouble() {
         @Override
         public double get() {
            return doubleArray[idx];
         }

         @Override
         public void set(double value) {
            doubleArray[idx] = value;
         }
      };
   }

   public static SyncableDouble create(DoubleSupplier getter, DoubleConsumer setter) {
      return new SyncableDouble() {
         @Override
         public double get() {
            return getter.getAsDouble();
         }

         @Override
         public void set(double value) {
            setter.accept(value);
         }
      };
   }
}
