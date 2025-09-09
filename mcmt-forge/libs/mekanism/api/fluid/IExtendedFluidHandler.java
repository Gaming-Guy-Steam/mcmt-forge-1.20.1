package mekanism.api.fluid;

import mekanism.api.Action;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

@NothingNullByDefault
public interface IExtendedFluidHandler extends IFluidHandler {
   void setFluidInTank(int var1, FluidStack var2);

   FluidStack insertFluid(int var1, FluidStack var2, Action var3);

   FluidStack extractFluid(int var1, int var2, Action var3);

   default FluidStack insertFluid(FluidStack stack, Action action) {
      return ExtendedFluidHandlerUtils.insert(stack, action, this::getTanks, this::getFluidInTank, this::insertFluid);
   }

   default FluidStack extractFluid(int amount, Action action) {
      return ExtendedFluidHandlerUtils.extract(amount, action, this::getTanks, this::getFluidInTank, this::extractFluid);
   }

   default FluidStack extractFluid(FluidStack stack, Action action) {
      return ExtendedFluidHandlerUtils.extract(stack, action, this::getTanks, this::getFluidInTank, this::extractFluid);
   }

   @Deprecated
   default int fill(FluidStack stack, FluidAction action) {
      return stack.getAmount() - this.insertFluid(stack, Action.fromFluidAction(action)).getAmount();
   }

   @Deprecated
   default FluidStack drain(FluidStack stack, FluidAction action) {
      return this.extractFluid(stack, Action.fromFluidAction(action));
   }

   @Deprecated
   default FluidStack drain(int amount, FluidAction action) {
      return this.extractFluid(amount, Action.fromFluidAction(action));
   }
}
