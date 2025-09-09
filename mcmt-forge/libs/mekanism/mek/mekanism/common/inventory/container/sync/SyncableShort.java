package mekanism.common.inventory.container.sync;

import it.unimi.dsi.fastutil.shorts.ShortConsumer;
import mekanism.api.functions.ShortSupplier;
import mekanism.common.network.to_client.container.property.ShortPropertyData;

public abstract class SyncableShort implements ISyncableData {
   private short lastKnownValue;

   public abstract short get();

   public abstract void set(short value);

   @Override
   public ISyncableData.DirtyType isDirty() {
      short oldValue = this.get();
      boolean dirty = oldValue != this.lastKnownValue;
      this.lastKnownValue = oldValue;
      return ISyncableData.DirtyType.get(dirty);
   }

   public ShortPropertyData getPropertyData(short property, ISyncableData.DirtyType dirtyType) {
      return new ShortPropertyData(property, this.get());
   }

   public static SyncableShort create(short[] shortArray, int idx) {
      return new SyncableShort() {
         @Override
         public short get() {
            return shortArray[idx];
         }

         @Override
         public void set(short value) {
            shortArray[idx] = value;
         }
      };
   }

   public static SyncableShort create(ShortSupplier getter, ShortConsumer setter) {
      return new SyncableShort() {
         @Override
         public short get() {
            return getter.getAsShort();
         }

         @Override
         public void set(short value) {
            setter.accept(value);
         }
      };
   }
}
