package mekanism.common.inventory.container.sync;

import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.network.to_client.container.property.FrequencyPropertyData;
import mekanism.common.network.to_client.container.property.PropertyData;
import org.jetbrains.annotations.Nullable;

public class SyncableFrequency<FREQUENCY extends Frequency> implements ISyncableData {
   private final Supplier<FREQUENCY> getter;
   private final Consumer<FREQUENCY> setter;
   private int lastKnownHashCode;

   public static <FREQUENCY extends Frequency> SyncableFrequency<FREQUENCY> create(Supplier<FREQUENCY> getter, Consumer<FREQUENCY> setter) {
      return new SyncableFrequency<>(getter, setter);
   }

   private SyncableFrequency(Supplier<FREQUENCY> getter, Consumer<FREQUENCY> setter) {
      this.getter = getter;
      this.setter = setter;
   }

   @Nullable
   public FREQUENCY get() {
      return this.getter.get();
   }

   public void set(@Nullable FREQUENCY value) {
      this.setter.accept(value);
   }

   @Override
   public ISyncableData.DirtyType isDirty() {
      FREQUENCY value = this.get();
      int valueHashCode = value == null ? 0 : value.getSyncHash();
      if (this.lastKnownHashCode == valueHashCode) {
         return ISyncableData.DirtyType.CLEAN;
      } else {
         this.lastKnownHashCode = valueHashCode;
         return ISyncableData.DirtyType.DIRTY;
      }
   }

   @Override
   public PropertyData getPropertyData(short property, ISyncableData.DirtyType dirtyType) {
      return new FrequencyPropertyData(property, this.get());
   }
}
