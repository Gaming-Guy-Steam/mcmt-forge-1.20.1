package mekanism.api.fluid;

import java.util.List;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.core.Direction;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public interface IMekanismFluidHandler extends ISidedFluidHandler, IContentsListener {
   default boolean canHandleFluid() {
      return true;
   }

   List<IExtendedFluidTank> getFluidTanks(@Nullable Direction var1);

   @Nullable
   default IExtendedFluidTank getFluidTank(int tank, @Nullable Direction side) {
      List<IExtendedFluidTank> tanks = this.getFluidTanks(side);
      return tank >= 0 && tank < tanks.size() ? tanks.get(tank) : null;
   }

   @Override
   default int getTanks(@Nullable Direction side) {
      return this.getFluidTanks(side).size();
   }

   @Override
   default FluidStack getFluidInTank(int tank, @Nullable Direction side) {
      IExtendedFluidTank fluidTank = this.getFluidTank(tank, side);
      return fluidTank == null ? FluidStack.EMPTY : fluidTank.getFluid();
   }

   @Override
   default void setFluidInTank(int tank, FluidStack stack, @Nullable Direction side) {
      IExtendedFluidTank fluidTank = this.getFluidTank(tank, side);
      if (fluidTank != null) {
         fluidTank.setStack(stack);
      }
   }

   @Override
   default int getTankCapacity(int tank, @Nullable Direction side) {
      IExtendedFluidTank fluidTank = this.getFluidTank(tank, side);
      return fluidTank == null ? 0 : fluidTank.getCapacity();
   }

   @Override
   default boolean isFluidValid(int tank, FluidStack stack, @Nullable Direction side) {
      IExtendedFluidTank fluidTank = this.getFluidTank(tank, side);
      return fluidTank != null && fluidTank.isFluidValid(stack);
   }

   @Override
   default FluidStack insertFluid(int tank, FluidStack stack, @Nullable Direction side, Action action) {
      IExtendedFluidTank fluidTank = this.getFluidTank(tank, side);
      return fluidTank == null ? stack : fluidTank.insert(stack, action, side == null ? AutomationType.INTERNAL : AutomationType.EXTERNAL);
   }

   @Override
   default FluidStack extractFluid(int tank, int amount, @Nullable Direction side, Action action) {
      IExtendedFluidTank fluidTank = this.getFluidTank(tank, side);
      return fluidTank == null ? FluidStack.EMPTY : fluidTank.extract(amount, action, side == null ? AutomationType.INTERNAL : AutomationType.EXTERNAL);
   }
}
