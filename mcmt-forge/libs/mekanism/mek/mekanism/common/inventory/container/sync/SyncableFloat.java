package mekanism.common.inventory.container.sync;

import it.unimi.dsi.fastutil.floats.FloatConsumer;
import mekanism.api.functions.FloatSupplier;
import mekanism.common.network.to_client.container.property.FloatPropertyData;

public abstract class SyncableFloat implements ISyncableData {
   private float lastKnownValue;

   public abstract float get();

   public abstract void set(float value);

   @Override
   public ISyncableData.DirtyType isDirty() {
      float oldValue = this.get();
      boolean dirty = oldValue != this.lastKnownValue;
      this.lastKnownValue = oldValue;
      return ISyncableData.DirtyType.get(dirty);
   }

   public FloatPropertyData getPropertyData(short property, ISyncableData.DirtyType dirtyType) {
      return new FloatPropertyData(property, this.get());
   }

   public static SyncableFloat create(float[] floatArray, int idx) {
      return new SyncableFloat() {
         @Override
         public float get() {
            return floatArray[idx];
         }

         @Override
         public void set(float value) {
            floatArray[idx] = value;
         }
      };
   }

   public static SyncableFloat create(FloatSupplier getter, FloatConsumer setter) {
      return new SyncableFloat() {
         @Override
         public float get() {
            return getter.getAsFloat();
         }

         @Override
         public void set(float value) {
            setter.accept(value);
         }
      };
   }
}
