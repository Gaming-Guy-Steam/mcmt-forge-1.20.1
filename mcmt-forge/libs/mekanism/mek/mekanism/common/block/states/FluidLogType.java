package mekanism.common.block.states;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;

public enum FluidLogType implements IFluidLogType, StringRepresentable {
   EMPTY("empty", Fluids.f_76191_),
   WATER("water", Fluids.f_76193_),
   LAVA("lava", Fluids.f_76195_);

   private final String name;
   private final Fluid fluid;

   private FluidLogType(String name, Fluid fluid) {
      this.name = name;
      this.fluid = fluid;
   }

   @Override
   public Fluid getFluid() {
      return this.fluid;
   }

   @NotNull
   public String m_7912_() {
      return this.name;
   }
}
