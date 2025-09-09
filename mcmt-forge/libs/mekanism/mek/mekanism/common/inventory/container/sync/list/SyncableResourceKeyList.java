package mekanism.common.inventory.container.sync.list;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.common.inventory.container.sync.ISyncableData;
import mekanism.common.network.to_client.container.property.list.ListPropertyData;
import mekanism.common.network.to_client.container.property.list.ResourceKeyListPropertyData;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class SyncableResourceKeyList<V> extends SyncableList<ResourceKey<V>> {
   private final ResourceKey<? extends Registry<V>> registry;

   public static <V> SyncableResourceKeyList<V> create(
      ResourceKey<? extends Registry<V>> registry, Supplier<List<ResourceKey<V>>> getter, Consumer<List<ResourceKey<V>>> setter
   ) {
      return new SyncableResourceKeyList<>(registry, getter, setter);
   }

   private SyncableResourceKeyList(ResourceKey<? extends Registry<V>> registry, Supplier<List<ResourceKey<V>>> getter, Consumer<List<ResourceKey<V>>> setter) {
      super(getter, setter);
      this.registry = registry;
   }

   @Override
   public ListPropertyData<ResourceKey<V>> getPropertyData(short property, ISyncableData.DirtyType dirtyType) {
      return new ResourceKeyListPropertyData<>(property, this.registry, this.get());
   }
}
