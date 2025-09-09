package mekanism.api;

import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public enum Action {
   EXECUTE(FluidAction.EXECUTE),
   SIMULATE(FluidAction.SIMULATE);

   private final FluidAction fluidAction;

   private Action(FluidAction fluidAction) {
      this.fluidAction = fluidAction;
   }

   public boolean execute() {
      return this == EXECUTE;
   }

   public boolean simulate() {
      return this == SIMULATE;
   }

   public FluidAction toFluidAction() {
      return this.fluidAction;
   }

   public Action combine(boolean execute) {
      return get(execute && this.execute());
   }

   public static Action get(boolean execute) {
      return execute ? EXECUTE : SIMULATE;
   }

   public static Action fromFluidAction(FluidAction action) {
      return action == FluidAction.EXECUTE ? EXECUTE : SIMULATE;
   }
}
