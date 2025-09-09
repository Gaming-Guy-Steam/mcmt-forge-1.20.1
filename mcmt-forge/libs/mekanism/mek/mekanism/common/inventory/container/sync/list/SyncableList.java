package mekanism.common.inventory.container.sync.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.common.inventory.container.sync.ISyncableData;
import mekanism.common.network.to_client.container.property.list.ListPropertyData;
import org.jetbrains.annotations.NotNull;

public abstract class SyncableList<TYPE> implements ISyncableData {
   private final Supplier<? extends Collection<TYPE>> getter;
   private final Consumer<List<TYPE>> setter;
   private int lastKnownHashCode;

   protected SyncableList(Supplier<? extends Collection<TYPE>> getter, Consumer<List<TYPE>> setter) {
      this.getter = getter;
      this.setter = setter;
   }

   @NotNull
   public List<TYPE> get() {
      Collection<TYPE> collection = this.getRaw();
      return (List<TYPE>)(collection instanceof List<TYPE> list ? list : new ArrayList<>(collection));
   }

   @NotNull
   protected Collection<TYPE> getRaw() {
      return (Collection<TYPE>)this.getter.get();
   }

   protected int getValueHashCode() {
      return this.getRaw().hashCode();
   }

   public void set(@NotNull List<TYPE> value) {
      this.setter.accept(value);
   }

   public abstract ListPropertyData<TYPE> getPropertyData(short property, ISyncableData.DirtyType dirtyType);

   @Override
   public ISyncableData.DirtyType isDirty() {
      int valuesHashCode = this.getValueHashCode();
      if (this.lastKnownHashCode == valuesHashCode) {
         return ISyncableData.DirtyType.CLEAN;
      } else {
         this.lastKnownHashCode = valuesHashCode;
         return ISyncableData.DirtyType.DIRTY;
      }
   }
}
