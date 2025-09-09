package mekanism.common.inventory.container.sync.list;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.common.content.filter.IFilter;
import mekanism.common.inventory.container.sync.ISyncableData;
import mekanism.common.network.to_client.container.property.list.FilterListPropertyData;
import mekanism.common.network.to_client.container.property.list.ListPropertyData;

public class SyncableFilterList<FILTER extends IFilter<?>> extends SyncableList<FILTER> {
   public static <FILTER extends IFilter<?>> SyncableFilterList<FILTER> create(Supplier<List<FILTER>> getter, Consumer<List<FILTER>> setter) {
      return new SyncableFilterList<>(getter, setter);
   }

   private SyncableFilterList(Supplier<List<FILTER>> getter, Consumer<List<FILTER>> setter) {
      super(getter, setter);
   }

   @Override
   public ListPropertyData<FILTER> getPropertyData(short property, ISyncableData.DirtyType dirtyType) {
      return new FilterListPropertyData<>(property, this.get());
   }
}
