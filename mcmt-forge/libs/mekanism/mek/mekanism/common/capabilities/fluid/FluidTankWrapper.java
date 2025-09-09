package mekanism.common.capabilities.fluid;

import java.util.function.BooleanSupplier;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.common.capabilities.merged.MergedTank;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class FluidTankWrapper implements IExtendedFluidTank {
   private final IExtendedFluidTank internal;
   private final BooleanSupplier insertCheck;
   private final MergedTank mergedTank;

   public FluidTankWrapper(MergedTank mergedTank, IExtendedFluidTank internal, BooleanSupplier insertCheck) {
      this.mergedTank = mergedTank;
      this.internal = internal;
      this.insertCheck = insertCheck;
   }

   public MergedTank getMergedTank() {
      return this.mergedTank;
   }

   @Override
   public void setStack(FluidStack stack) {
      this.internal.setStack(stack);
   }

   @Override
   public void setStackUnchecked(FluidStack stack) {
      this.internal.setStackUnchecked(stack);
   }

   @Override
   public FluidStack insert(FluidStack stack, Action action, AutomationType automationType) {
      return this.insertCheck.getAsBoolean() ? this.internal.insert(stack, action, automationType) : stack;
   }

   @Override
   public FluidStack extract(int amount, Action action, AutomationType automationType) {
      return this.internal.extract(amount, action, automationType);
   }

   @Override
   public void onContentsChanged() {
      this.internal.onContentsChanged();
   }

   @Override
   public int setStackSize(int amount, Action action) {
      return this.internal.setStackSize(amount, action);
   }

   @Override
   public int growStack(int amount, Action action) {
      return this.internal.growStack(amount, action);
   }

   @Override
   public int shrinkStack(int amount, Action action) {
      return this.internal.shrinkStack(amount, action);
   }

   @Override
   public boolean isEmpty() {
      return this.internal.isEmpty();
   }

   @Override
   public void setEmpty() {
      this.internal.setEmpty();
   }

   @Override
   public boolean isFluidEqual(FluidStack other) {
      return this.internal.isFluidEqual(other);
   }

   @Override
   public int getNeeded() {
      return this.internal.getNeeded();
   }

   @Override
   public CompoundTag serializeNBT() {
      return this.internal.serializeNBT();
   }

   public void deserializeNBT(CompoundTag nbt) {
      this.internal.deserializeNBT(nbt);
   }

   @NotNull
   public FluidStack getFluid() {
      return this.internal.getFluid();
   }

   public int getFluidAmount() {
      return this.internal.getFluidAmount();
   }

   public int getCapacity() {
      return this.internal.getCapacity();
   }

   public boolean isFluidValid(FluidStack stack) {
      return this.internal.isFluidValid(stack);
   }
}
