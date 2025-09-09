package mekanism.common.inventory.container.sync;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.common.network.to_client.container.property.IntPropertyData;
import org.jetbrains.annotations.NotNull;

public class SyncableEnum<ENUM extends Enum<ENUM>> implements ISyncableData {
   private final Int2ObjectFunction<ENUM> decoder;
   private final Supplier<ENUM> getter;
   private final Consumer<ENUM> setter;
   @NotNull
   private ENUM lastKnownValue;

   public static <ENUM extends Enum<ENUM>> SyncableEnum<ENUM> create(
      Int2ObjectFunction<ENUM> decoder, @NotNull ENUM defaultValue, Supplier<ENUM> getter, Consumer<ENUM> setter
   ) {
      return new SyncableEnum<>(decoder, defaultValue, getter, setter);
   }

   private SyncableEnum(Int2ObjectFunction<ENUM> decoder, @NotNull ENUM defaultValue, Supplier<ENUM> getter, Consumer<ENUM> setter) {
      this.decoder = decoder;
      this.lastKnownValue = defaultValue;
      this.getter = getter;
      this.setter = setter;
   }

   @NotNull
   public ENUM get() {
      return this.getter.get();
   }

   public void set(int ordinal) {
      this.set((ENUM)this.decoder.apply(ordinal));
   }

   public void set(@NotNull ENUM value) {
      this.setter.accept(value);
   }

   @Override
   public ISyncableData.DirtyType isDirty() {
      ENUM oldValue = this.get();
      boolean dirty = oldValue != this.lastKnownValue;
      this.lastKnownValue = oldValue;
      return ISyncableData.DirtyType.get(dirty);
   }

   public IntPropertyData getPropertyData(short property, ISyncableData.DirtyType dirtyType) {
      return new IntPropertyData(property, this.get().ordinal());
   }
}
