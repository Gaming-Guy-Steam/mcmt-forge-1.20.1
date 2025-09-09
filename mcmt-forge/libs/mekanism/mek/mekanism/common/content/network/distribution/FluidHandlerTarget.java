package mekanism.common.content.network.distribution;

import java.util.Collection;
import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.Target;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import org.jetbrains.annotations.NotNull;

public class FluidHandlerTarget extends Target<IFluidHandler, Integer, FluidStack> {
   public FluidHandlerTarget(@NotNull FluidStack type) {
      this.extra = type;
   }

   public FluidHandlerTarget(@NotNull FluidStack type, Collection<IFluidHandler> allHandlers) {
      super(allHandlers);
      this.extra = type;
   }

   public FluidHandlerTarget(@NotNull FluidStack type, int expectedSize) {
      super(expectedSize);
      this.extra = type;
   }

   protected void acceptAmount(IFluidHandler handler, SplitInfo<Integer> splitInfo, Integer amount) {
      splitInfo.send(handler.fill(new FluidStack(this.extra, amount), FluidAction.EXECUTE));
   }

   protected Integer simulate(IFluidHandler handler, @NotNull FluidStack fluidStack) {
      return handler.fill(fluidStack.copy(), FluidAction.SIMULATE);
   }
}
