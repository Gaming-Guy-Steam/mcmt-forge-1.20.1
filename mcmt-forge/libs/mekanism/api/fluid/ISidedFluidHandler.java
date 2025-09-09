package mekanism.api.fluid;

import mekanism.api.Action;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.core.Direction;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public interface ISidedFluidHandler extends IExtendedFluidHandler {
   @Nullable
   default Direction getFluidSideFor() {
      return null;
   }

   int getTanks(@Nullable Direction var1);

   default int getTanks() {
      return this.getTanks(this.getFluidSideFor());
   }

   FluidStack getFluidInTank(int var1, @Nullable Direction var2);

   default FluidStack getFluidInTank(int tank) {
      return this.getFluidInTank(tank, this.getFluidSideFor());
   }

   void setFluidInTank(int var1, FluidStack var2, @Nullable Direction var3);

   @Override
   default void setFluidInTank(int tank, FluidStack stack) {
      this.setFluidInTank(tank, stack, this.getFluidSideFor());
   }

   int getTankCapacity(int var1, @Nullable Direction var2);

   default int getTankCapacity(int tank) {
      return this.getTankCapacity(tank, this.getFluidSideFor());
   }

   boolean isFluidValid(int var1, FluidStack var2, @Nullable Direction var3);

   default boolean isFluidValid(int tank, FluidStack stack) {
      return this.isFluidValid(tank, stack, this.getFluidSideFor());
   }

   FluidStack insertFluid(int var1, FluidStack var2, @Nullable Direction var3, Action var4);

   @Override
   default FluidStack insertFluid(int tank, FluidStack stack, Action action) {
      return this.insertFluid(tank, stack, this.getFluidSideFor(), action);
   }

   FluidStack extractFluid(int var1, int var2, @Nullable Direction var3, Action var4);

   @Override
   default FluidStack extractFluid(int tank, int amount, Action action) {
      return this.extractFluid(tank, amount, this.getFluidSideFor(), action);
   }

   default FluidStack insertFluid(FluidStack stack, @Nullable Direction side, Action action) {
      return ExtendedFluidHandlerUtils.insert(
         stack, action, () -> this.getTanks(side), tank -> this.getFluidInTank(tank, side), (tank, s, a) -> this.insertFluid(tank, s, side, a)
      );
   }

   default FluidStack extractFluid(int amount, @Nullable Direction side, Action action) {
      return ExtendedFluidHandlerUtils.extract(
         amount, action, () -> this.getTanks(side), tank -> this.getFluidInTank(tank, side), (tank, a, act) -> this.extractFluid(tank, a, side, act)
      );
   }

   default FluidStack extractFluid(FluidStack stack, @Nullable Direction side, Action action) {
      return ExtendedFluidHandlerUtils.extract(
         stack, action, () -> this.getTanks(side), tank -> this.getFluidInTank(tank, side), (tank, a, act) -> this.extractFluid(tank, a, side, act)
      );
   }
}
