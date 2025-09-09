package mekanism.common.capabilities.proxy;

import mekanism.api.Action;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.fluid.IExtendedFluidHandler;
import mekanism.api.fluid.ISidedFluidHandler;
import mekanism.common.capabilities.holder.IHolder;
import net.minecraft.core.Direction;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ProxyFluidHandler extends ProxyHandler implements IExtendedFluidHandler {
   private final ISidedFluidHandler fluidHandler;

   public ProxyFluidHandler(ISidedFluidHandler fluidHandler, @Nullable Direction side, @Nullable IHolder holder) {
      super(side, holder);
      this.fluidHandler = fluidHandler;
   }

   public int getTanks() {
      return this.fluidHandler.getTanks(this.side);
   }

   public FluidStack getFluidInTank(int tank) {
      return this.fluidHandler.getFluidInTank(tank, this.side);
   }

   @Override
   public void setFluidInTank(int tank, FluidStack stack) {
      if (!this.readOnly) {
         this.fluidHandler.setFluidInTank(tank, stack, this.side);
      }
   }

   public int getTankCapacity(int tank) {
      return this.fluidHandler.getTankCapacity(tank, this.side);
   }

   public boolean isFluidValid(int tank, FluidStack stack) {
      return !this.readOnly || this.fluidHandler.isFluidValid(tank, stack, this.side);
   }

   @Override
   public FluidStack insertFluid(int tank, FluidStack stack, Action action) {
      return !this.readOnly && !this.readOnlyInsert.getAsBoolean() ? this.fluidHandler.insertFluid(tank, stack, this.side, action) : stack;
   }

   @Override
   public FluidStack extractFluid(int tank, int amount, Action action) {
      return !this.readOnly && !this.readOnlyExtract.getAsBoolean() ? this.fluidHandler.extractFluid(tank, amount, this.side, action) : FluidStack.EMPTY;
   }

   @Override
   public FluidStack insertFluid(FluidStack stack, Action action) {
      return !this.readOnly && !this.readOnlyInsert.getAsBoolean() ? this.fluidHandler.insertFluid(stack, this.side, action) : stack;
   }

   @Override
   public FluidStack extractFluid(int amount, Action action) {
      return !this.readOnly && !this.readOnlyExtract.getAsBoolean() ? this.fluidHandler.extractFluid(amount, this.side, action) : FluidStack.EMPTY;
   }

   @Override
   public FluidStack extractFluid(FluidStack stack, Action action) {
      return !this.readOnly && !this.readOnlyExtract.getAsBoolean() ? this.fluidHandler.extractFluid(stack, this.side, action) : FluidStack.EMPTY;
   }
}
