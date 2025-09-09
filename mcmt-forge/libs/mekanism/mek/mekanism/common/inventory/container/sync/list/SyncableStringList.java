package mekanism.common.inventory.container.sync.list;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.common.inventory.container.sync.ISyncableData;
import mekanism.common.network.to_client.container.property.list.ListPropertyData;
import mekanism.common.network.to_client.container.property.list.StringListPropertyData;

public class SyncableStringList extends SyncableList<String> {
   public static SyncableStringList create(Supplier<List<String>> getter, Consumer<List<String>> setter) {
      return new SyncableStringList(getter, setter);
   }

   private SyncableStringList(Supplier<List<String>> getter, Consumer<List<String>> setter) {
      super(getter, setter);
   }

   @Override
   public ListPropertyData<String> getPropertyData(short property, ISyncableData.DirtyType dirtyType) {
      return new StringListPropertyData(property, this.get());
   }
}
