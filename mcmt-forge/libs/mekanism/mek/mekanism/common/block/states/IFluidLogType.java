package mekanism.common.block.states;

import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public interface IFluidLogType {
   default boolean isEmpty() {
      return this.getFluid() == Fluids.f_76191_;
   }

   Fluid getFluid();
}
