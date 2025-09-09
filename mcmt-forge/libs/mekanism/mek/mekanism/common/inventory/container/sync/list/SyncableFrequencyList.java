package mekanism.common.inventory.container.sync.list;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.common.inventory.container.sync.ISyncableData;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.network.to_client.container.property.list.FrequencyListPropertyData;
import mekanism.common.network.to_client.container.property.list.ListPropertyData;

public class SyncableFrequencyList<FREQUENCY extends Frequency> extends SyncableList<FREQUENCY> {
   public static <FREQUENCY extends Frequency> SyncableFrequencyList<FREQUENCY> create(
      Supplier<? extends Collection<FREQUENCY>> getter, Consumer<List<FREQUENCY>> setter
   ) {
      return new SyncableFrequencyList<>(getter, setter);
   }

   private SyncableFrequencyList(Supplier<? extends Collection<FREQUENCY>> getter, Consumer<List<FREQUENCY>> setter) {
      super(getter, setter);
   }

   @Override
   protected int getValueHashCode() {
      int hashCode = 1;

      for (FREQUENCY frequency : this.getRaw()) {
         hashCode = 31 * hashCode + frequency.hashCode();
      }

      return hashCode;
   }

   @Override
   public ListPropertyData<FREQUENCY> getPropertyData(short property, ISyncableData.DirtyType dirtyType) {
      return new FrequencyListPropertyData<>(property, this.get());
   }
}
