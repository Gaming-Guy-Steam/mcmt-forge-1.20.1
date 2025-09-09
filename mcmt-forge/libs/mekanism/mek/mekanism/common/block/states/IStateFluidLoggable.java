package mekanism.common.block.states;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public interface IStateFluidLoggable extends BucketPickup, LiquidBlockContainer {
   default boolean isValidFluid(@NotNull Fluid fluid) {
      return this.getFluidLoggedProperty().m_6908_().stream().anyMatch(possibleValue -> ((IFluidLogType)possibleValue).getFluid() == fluid);
   }

   @NotNull
   default EnumProperty<? extends IFluidLogType> getFluidLoggedProperty() {
      return BlockStateHelper.FLUID_LOGGED;
   }

   @NotNull
   default FluidState getFluid(@NotNull BlockState state) {
      IFluidLogType fluidLogged = (IFluidLogType)state.m_61143_(this.getFluidLoggedProperty());
      if (!fluidLogged.isEmpty()) {
         Fluid fluid = fluidLogged.getFluid();
         return fluid instanceof FlowingFluid ? ((FlowingFluid)fluid).m_76068_(false) : fluid.m_76145_();
      } else {
         return Fluids.f_76191_.m_76145_();
      }
   }

   default int getFluidLightLevel(@NotNull BlockState state) {
      FluidState fluid = this.getFluid(state);
      return fluid.m_76152_() == Fluids.f_76195_ ? 15 : 0;
   }

   default void updateFluids(@NotNull BlockState state, @NotNull LevelAccessor world, @NotNull BlockPos currentPos) {
      IFluidLogType fluidLogged = (IFluidLogType)state.m_61143_(this.getFluidLoggedProperty());
      if (!fluidLogged.isEmpty()) {
         Fluid fluid = fluidLogged.getFluid();
         world.m_186469_(currentPos, fluid, fluid.m_6718_(world));
      }
   }

   default boolean m_6044_(@NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Fluid fluid) {
      return ((IFluidLogType)((Enum)state.m_61143_(this.getFluidLoggedProperty()))).isEmpty() && this.isValidFluid(fluid);
   }

   default BlockState setState(BlockState state, Fluid fluid) {
      return setState(state, fluid, this.getFluidLoggedProperty());
   }

   private static <T extends Enum<T> & StringRepresentable & IFluidLogType> BlockState setState(BlockState state, Fluid fluid, EnumProperty<T> property) {
      for (T possibleValue : property.m_6908_()) {
         if (possibleValue.getFluid() == fluid) {
            return (BlockState)state.m_61124_(property, possibleValue);
         }
      }

      return state;
   }

   default boolean m_7361_(@NotNull LevelAccessor world, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull FluidState fluidState) {
      Fluid fluid = fluidState.m_76152_();
      if (this.m_6044_(world, pos, state, fluid)) {
         if (!world.m_5776_()) {
            world.m_7731_(pos, this.setState(state, fluid), 3);
            world.m_186469_(pos, fluid, fluid.m_6718_(world));
         }

         return true;
      } else {
         return false;
      }
   }

   @NotNull
   default ItemStack m_142598_(@NotNull LevelAccessor world, @NotNull BlockPos pos, @NotNull BlockState state) {
      IFluidLogType fluidLogged = (IFluidLogType)state.m_61143_(this.getFluidLoggedProperty());
      if (!fluidLogged.isEmpty()) {
         Fluid fluid = fluidLogged.getFluid();
         ItemStack bucket = fluid.getFluidType().getBucket(new FluidStack(fluid, 1000));
         if (!bucket.m_41619_()) {
            world.m_7731_(pos, this.setState(state, Fluids.f_76191_), 3);
            return bucket;
         }
      }

      return ItemStack.f_41583_;
   }

   @NotNull
   default Optional<SoundEvent> m_142298_() {
      return Optional.empty();
   }

   @NotNull
   default Optional<SoundEvent> getPickupSound(BlockState state) {
      return this.getFluid(state).m_76152_().m_142520_();
   }
}
