package mekanism.common.inventory.container.sync;

import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.common.network.to_client.container.property.RegistryEntryPropertyData;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;

public class SyncableRegistryEntry<V> implements ISyncableData {
   private final Supplier<V> getter;
   private final Consumer<V> setter;
   private final IForgeRegistry<V> registry;
   private V lastKnownValue;

   public static <V> SyncableRegistryEntry<V> create(IForgeRegistry<V> registry, Supplier<V> getter, Consumer<V> setter) {
      return new SyncableRegistryEntry<>(registry, getter, setter);
   }

   private SyncableRegistryEntry(IForgeRegistry<V> registry, Supplier<V> getter, Consumer<V> setter) {
      this.registry = registry;
      this.getter = getter;
      this.setter = setter;
   }

   @NotNull
   public V get() {
      return this.getter.get();
   }

   public void set(@NotNull V value) {
      this.setter.accept(value);
   }

   @Override
   public ISyncableData.DirtyType isDirty() {
      V oldValue = this.get();
      boolean dirty = oldValue != this.lastKnownValue;
      this.lastKnownValue = oldValue;
      return ISyncableData.DirtyType.get(dirty);
   }

   public RegistryEntryPropertyData<?> getPropertyData(short property, ISyncableData.DirtyType dirtyType) {
      return new RegistryEntryPropertyData(property, this.registry, this.get());
   }
}
