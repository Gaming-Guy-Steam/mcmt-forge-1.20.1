package mekanism.common.inventory.container.sync.chemical;

import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.api.chemical.pigment.IEmptyPigmentProvider;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.common.inventory.container.sync.ISyncableData;
import mekanism.common.network.to_client.container.property.LongPropertyData;
import mekanism.common.network.to_client.container.property.PropertyData;
import mekanism.common.network.to_client.container.property.chemical.PigmentStackPropertyData;
import org.jetbrains.annotations.NotNull;

public class SyncablePigmentStack extends SyncableChemicalStack<Pigment, PigmentStack> implements IEmptyPigmentProvider {
   public static SyncablePigmentStack create(IPigmentTank handler) {
      return create(handler, false);
   }

   public static SyncablePigmentStack create(IPigmentTank handler, boolean isClient) {
      return create(handler::getStack, isClient ? handler::setStackUnchecked : handler::setStack);
   }

   public static SyncablePigmentStack create(Supplier<PigmentStack> getter, Consumer<PigmentStack> setter) {
      return new SyncablePigmentStack(getter, setter);
   }

   private SyncablePigmentStack(Supplier<PigmentStack> getter, Consumer<PigmentStack> setter) {
      super(getter, setter);
   }

   @NotNull
   protected PigmentStack createStack(PigmentStack stored, long size) {
      return new PigmentStack(stored, size);
   }

   @Override
   public PropertyData getPropertyData(short property, ISyncableData.DirtyType dirtyType) {
      return (PropertyData)(dirtyType == ISyncableData.DirtyType.SIZE
         ? new LongPropertyData(property, this.get().getAmount())
         : new PigmentStackPropertyData(property, this.get()));
   }
}
