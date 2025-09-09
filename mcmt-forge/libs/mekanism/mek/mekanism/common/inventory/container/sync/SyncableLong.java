package mekanism.common.inventory.container.sync;

import java.util.function.LongConsumer;
import java.util.function.LongSupplier;
import mekanism.common.network.to_client.container.property.LongPropertyData;

public abstract class SyncableLong implements ISyncableData {
   private long lastKnownValue;

   public abstract long get();

   public abstract void set(long value);

   @Override
   public ISyncableData.DirtyType isDirty() {
      long oldValue = this.get();
      boolean dirty = oldValue != this.lastKnownValue;
      this.lastKnownValue = oldValue;
      return ISyncableData.DirtyType.get(dirty);
   }

   public LongPropertyData getPropertyData(short property, ISyncableData.DirtyType dirtyType) {
      return new LongPropertyData(property, this.get());
   }

   public static SyncableLong create(long[] longArray, int idx) {
      return new SyncableLong() {
         @Override
         public long get() {
            return longArray[idx];
         }

         @Override
         public void set(long value) {
            longArray[idx] = value;
         }
      };
   }

   public static SyncableLong create(LongSupplier getter, LongConsumer setter) {
      return new SyncableLong() {
         @Override
         public long get() {
            return getter.getAsLong();
         }

         @Override
         public void set(long value) {
            setter.accept(value);
         }
      };
   }
}
