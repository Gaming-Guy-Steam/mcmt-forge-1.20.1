package mekanism.common.inventory.container.sync;

import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.common.network.to_client.container.property.FluidStackPropertyData;
import mekanism.common.network.to_client.container.property.IntPropertyData;
import mekanism.common.network.to_client.container.property.PropertyData;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class SyncableFluidStack implements ISyncableData {
   @NotNull
   private FluidStack lastKnownValue = FluidStack.EMPTY;
   private final Supplier<FluidStack> getter;
   private final Consumer<FluidStack> setter;

   public static SyncableFluidStack create(@NotNull IExtendedFluidTank handler) {
      return create(handler, false);
   }

   public static SyncableFluidStack create(IExtendedFluidTank handler, boolean isClient) {
      return create(handler::getFluid, isClient ? handler::setStackUnchecked : handler::setStack);
   }

   public static SyncableFluidStack create(Supplier<FluidStack> getter, Consumer<FluidStack> setter) {
      return new SyncableFluidStack(getter, setter);
   }

   private SyncableFluidStack(Supplier<FluidStack> getter, Consumer<FluidStack> setter) {
      this.getter = getter;
      this.setter = setter;
   }

   @NotNull
   public FluidStack get() {
      return this.getter.get();
   }

   public void set(@NotNull FluidStack value) {
      this.setter.accept(value);
   }

   public void set(int amount) {
      FluidStack fluid = this.get();
      if (!fluid.isEmpty()) {
         this.set(new FluidStack(fluid.getFluid(), amount));
      }
   }

   @Override
   public ISyncableData.DirtyType isDirty() {
      FluidStack value = this.get();
      boolean sameFluid = value.isFluidEqual(this.lastKnownValue);
      if (sameFluid && value.getAmount() == this.lastKnownValue.getAmount()) {
         return ISyncableData.DirtyType.CLEAN;
      } else {
         this.lastKnownValue = value.copy();
         return sameFluid ? ISyncableData.DirtyType.SIZE : ISyncableData.DirtyType.DIRTY;
      }
   }

   @Override
   public PropertyData getPropertyData(short property, ISyncableData.DirtyType dirtyType) {
      return (PropertyData)(dirtyType == ISyncableData.DirtyType.SIZE
         ? new IntPropertyData(property, this.get().getAmount())
         : new FluidStackPropertyData(property, this.get()));
   }
}
