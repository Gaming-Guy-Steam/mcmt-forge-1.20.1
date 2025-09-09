package mekanism.common.inventory.container.sync.list;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.common.inventory.container.sync.ISyncableData;
import mekanism.common.network.to_client.container.property.list.ListPropertyData;
import mekanism.common.network.to_client.container.property.list.RegistryEntryListPropertyData;
import net.minecraftforge.registries.IForgeRegistry;

public class SyncableRegistryEntryList<V> extends SyncableList<V> {
   private final IForgeRegistry<V> registry;

   public static <V> SyncableRegistryEntryList<V> create(IForgeRegistry<V> registry, Supplier<List<V>> getter, Consumer<List<V>> setter) {
      return new SyncableRegistryEntryList<>(registry, getter, setter);
   }

   private SyncableRegistryEntryList(IForgeRegistry<V> registry, Supplier<List<V>> getter, Consumer<List<V>> setter) {
      super(getter, setter);
      this.registry = registry;
   }

   @Override
   public ListPropertyData<V> getPropertyData(short property, ISyncableData.DirtyType dirtyType) {
      return new RegistryEntryListPropertyData<>(property, this.registry, this.get());
   }
}
