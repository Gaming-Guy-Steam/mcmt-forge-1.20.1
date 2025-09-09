package mekanism.common.inventory.container.sync;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import mekanism.common.network.to_client.container.property.IntPropertyData;

public abstract class SyncableInt implements ISyncableData {
   private int lastKnownValue;

   public abstract int get();

   public abstract void set(int value);

   @Override
   public ISyncableData.DirtyType isDirty() {
      int oldValue = this.get();
      boolean dirty = oldValue != this.lastKnownValue;
      this.lastKnownValue = oldValue;
      return ISyncableData.DirtyType.get(dirty);
   }

   public IntPropertyData getPropertyData(short property, ISyncableData.DirtyType dirtyType) {
      return new IntPropertyData(property, this.get());
   }

   public static SyncableInt create(int[] intArray, int idx) {
      return new SyncableInt() {
         @Override
         public int get() {
            return intArray[idx];
         }

         @Override
         public void set(int value) {
            intArray[idx] = value;
         }
      };
   }

   public static SyncableInt create(IntSupplier getter, IntConsumer setter) {
      return new SyncableInt() {
         @Override
         public int get() {
            return getter.getAsInt();
         }

         @Override
         public void set(int value) {
            setter.accept(value);
         }
      };
   }
}
