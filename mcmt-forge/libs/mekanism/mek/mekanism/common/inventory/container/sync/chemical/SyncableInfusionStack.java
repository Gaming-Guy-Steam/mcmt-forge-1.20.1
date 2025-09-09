package mekanism.common.inventory.container.sync.chemical;

import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.api.chemical.infuse.IEmptyInfusionProvider;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.common.inventory.container.sync.ISyncableData;
import mekanism.common.network.to_client.container.property.LongPropertyData;
import mekanism.common.network.to_client.container.property.PropertyData;
import mekanism.common.network.to_client.container.property.chemical.InfusionStackPropertyData;
import org.jetbrains.annotations.NotNull;

public class SyncableInfusionStack extends SyncableChemicalStack<InfuseType, InfusionStack> implements IEmptyInfusionProvider {
   public static SyncableInfusionStack create(IInfusionTank handler) {
      return create(handler, false);
   }

   public static SyncableInfusionStack create(IInfusionTank handler, boolean isClient) {
      return create(handler::getStack, isClient ? handler::setStackUnchecked : handler::setStack);
   }

   public static SyncableInfusionStack create(Supplier<InfusionStack> getter, Consumer<InfusionStack> setter) {
      return new SyncableInfusionStack(getter, setter);
   }

   private SyncableInfusionStack(Supplier<InfusionStack> getter, Consumer<InfusionStack> setter) {
      super(getter, setter);
   }

   @NotNull
   protected InfusionStack createStack(InfusionStack stored, long size) {
      return new InfusionStack(stored, size);
   }

   @Override
   public PropertyData getPropertyData(short property, ISyncableData.DirtyType dirtyType) {
      return (PropertyData)(dirtyType == ISyncableData.DirtyType.SIZE
         ? new LongPropertyData(property, this.get().getAmount())
         : new InfusionStackPropertyData(property, this.get()));
   }
}
