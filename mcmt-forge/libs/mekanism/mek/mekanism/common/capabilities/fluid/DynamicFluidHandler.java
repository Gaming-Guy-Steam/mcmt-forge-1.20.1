package mekanism.common.capabilities.fluid;

import java.util.List;
import java.util.function.Function;
import mekanism.api.Action;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.common.capabilities.DynamicHandler;
import net.minecraft.core.Direction;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class DynamicFluidHandler extends DynamicHandler<IExtendedFluidTank> implements IMekanismFluidHandler {
   public DynamicFluidHandler(
      Function<Direction, List<IExtendedFluidTank>> tankSupplier,
      DynamicHandler.InteractPredicate canExtract,
      DynamicHandler.InteractPredicate canInsert,
      @Nullable IContentsListener listener
   ) {
      super(tankSupplier, canExtract, canInsert, listener);
   }

   @Override
   public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
      return this.containerSupplier.apply(side);
   }

   @Override
   public FluidStack insertFluid(int tank, FluidStack stack, @Nullable Direction side, Action action) {
      return this.canInsert.test(tank, side) ? IMekanismFluidHandler.super.insertFluid(tank, stack, side, action) : stack;
   }

   @Override
   public FluidStack extractFluid(int tank, int amount, @Nullable Direction side, Action action) {
      return this.canExtract.test(tank, side) ? IMekanismFluidHandler.super.extractFluid(tank, amount, side, action) : FluidStack.EMPTY;
   }
}
