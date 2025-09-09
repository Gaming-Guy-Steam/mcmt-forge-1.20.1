package mekanism.common.inventory.container.sync;

import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.api.math.FloatingLong;
import mekanism.common.network.to_client.container.property.FloatingLongPropertyData;
import mekanism.common.network.to_client.container.property.PropertyData;
import mekanism.common.network.to_client.container.property.ShortPropertyData;
import org.jetbrains.annotations.NotNull;

public class SyncableFloatingLong implements ISyncableData {
   private final Supplier<FloatingLong> getter;
   private final Consumer<FloatingLong> setter;
   private long lastKnownValue;
   private short lastKnownDecimal;

   public static SyncableFloatingLong create(Supplier<FloatingLong> getter, Consumer<FloatingLong> setter) {
      return new SyncableFloatingLong(getter, setter);
   }

   private SyncableFloatingLong(Supplier<FloatingLong> getter, Consumer<FloatingLong> setter) {
      this.getter = getter;
      this.setter = setter;
   }

   @NotNull
   public FloatingLong get() {
      return this.getter.get();
   }

   public void set(@NotNull FloatingLong value) {
      this.setter.accept(value);
   }

   public void setDecimal(short decimal) {
      this.set(FloatingLong.create(this.get().getValue(), decimal));
   }

   @Override
   public ISyncableData.DirtyType isDirty() {
      FloatingLong val = this.get();
      long value = val.getValue();
      short decimal = val.getDecimal();
      if (value == this.lastKnownValue && decimal == this.lastKnownDecimal) {
         return ISyncableData.DirtyType.CLEAN;
      } else {
         ISyncableData.DirtyType type = ISyncableData.DirtyType.DIRTY;
         if (value == this.lastKnownValue) {
            type = ISyncableData.DirtyType.SIZE;
         }

         this.lastKnownValue = value;
         this.lastKnownDecimal = decimal;
         return type;
      }
   }

   @Override
   public PropertyData getPropertyData(short property, ISyncableData.DirtyType dirtyType) {
      return (PropertyData)(dirtyType == ISyncableData.DirtyType.SIZE
         ? new ShortPropertyData(property, this.get().getDecimal())
         : new FloatingLongPropertyData(property, this.get()));
   }
}
