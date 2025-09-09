package mekanism.common.inventory.container.sync.chemical;

import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IEmptyGasProvider;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.common.inventory.container.sync.ISyncableData;
import mekanism.common.network.to_client.container.property.LongPropertyData;
import mekanism.common.network.to_client.container.property.PropertyData;
import mekanism.common.network.to_client.container.property.chemical.GasStackPropertyData;
import org.jetbrains.annotations.NotNull;

public class SyncableGasStack extends SyncableChemicalStack<Gas, GasStack> implements IEmptyGasProvider {
   public static SyncableGasStack create(IGasTank handler) {
      return create(handler, false);
   }

   public static SyncableGasStack create(IGasTank handler, boolean isClient) {
      return create(handler::getStack, isClient ? handler::setStackUnchecked : handler::setStack);
   }

   public static SyncableGasStack create(Supplier<GasStack> getter, Consumer<GasStack> setter) {
      return new SyncableGasStack(getter, setter);
   }

   private SyncableGasStack(Supplier<GasStack> getter, Consumer<GasStack> setter) {
      super(getter, setter);
   }

   @NotNull
   protected GasStack createStack(GasStack stored, long size) {
      return new GasStack(stored, size);
   }

   @Override
   public PropertyData getPropertyData(short property, ISyncableData.DirtyType dirtyType) {
      return (PropertyData)(dirtyType == ISyncableData.DirtyType.SIZE
         ? new LongPropertyData(property, this.get().getAmount())
         : new GasStackPropertyData(property, this.get()));
   }
}
