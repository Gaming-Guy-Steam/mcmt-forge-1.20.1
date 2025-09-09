package mekanism.common.inventory.container.sync.chemical;

import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.api.chemical.slurry.IEmptySlurryProvider;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.inventory.container.sync.ISyncableData;
import mekanism.common.network.to_client.container.property.LongPropertyData;
import mekanism.common.network.to_client.container.property.PropertyData;
import mekanism.common.network.to_client.container.property.chemical.SlurryStackPropertyData;
import org.jetbrains.annotations.NotNull;

public class SyncableSlurryStack extends SyncableChemicalStack<Slurry, SlurryStack> implements IEmptySlurryProvider {
   public static SyncableSlurryStack create(ISlurryTank handler) {
      return create(handler, false);
   }

   public static SyncableSlurryStack create(ISlurryTank handler, boolean isClient) {
      return create(handler::getStack, isClient ? handler::setStackUnchecked : handler::setStack);
   }

   public static SyncableSlurryStack create(Supplier<SlurryStack> getter, Consumer<SlurryStack> setter) {
      return new SyncableSlurryStack(getter, setter);
   }

   private SyncableSlurryStack(Supplier<SlurryStack> getter, Consumer<SlurryStack> setter) {
      super(getter, setter);
   }

   @NotNull
   protected SlurryStack createStack(SlurryStack stored, long size) {
      return new SlurryStack(stored, size);
   }

   @Override
   public PropertyData getPropertyData(short property, ISyncableData.DirtyType dirtyType) {
      return (PropertyData)(dirtyType == ISyncableData.DirtyType.SIZE
         ? new LongPropertyData(property, this.get().getAmount())
         : new SlurryStackPropertyData(property, this.get()));
   }
}
