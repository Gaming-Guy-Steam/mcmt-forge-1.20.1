package mekanism.common.tile.multiblock;

import mekanism.api.Action;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.SubstanceType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class TileEntityDynamicValve extends TileEntityDynamicTank {
   public TileEntityDynamicValve(BlockPos pos, BlockState state) {
      super(MekanismBlocks.DYNAMIC_VALVE, pos, state);
   }

   @NotNull
   @Override
   protected IFluidTankHolder getInitialFluidTanks(IContentsListener listener) {
      return side -> this.getMultiblock().getFluidTanks(side);
   }

   @NotNull
   @Override
   public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks(IContentsListener listener) {
      return side -> this.getMultiblock().getGasTanks(side);
   }

   @NotNull
   @Override
   public IChemicalTankHolder<InfuseType, InfusionStack, IInfusionTank> getInitialInfusionTanks(IContentsListener listener) {
      return side -> this.getMultiblock().getInfusionTanks(side);
   }

   @NotNull
   @Override
   public IChemicalTankHolder<Pigment, PigmentStack, IPigmentTank> getInitialPigmentTanks(IContentsListener listener) {
      return side -> this.getMultiblock().getPigmentTanks(side);
   }

   @NotNull
   @Override
   public IChemicalTankHolder<Slurry, SlurryStack, ISlurryTank> getInitialSlurryTanks(IContentsListener listener) {
      return side -> this.getMultiblock().getSlurryTanks(side);
   }

   @Override
   public boolean persists(SubstanceType type) {
      return type != SubstanceType.FLUID
            && type != SubstanceType.GAS
            && type != SubstanceType.INFUSION
            && type != SubstanceType.PIGMENT
            && type != SubstanceType.SLURRY
         ? super.persists(type)
         : false;
   }

   @NotNull
   @Override
   public FluidStack insertFluid(@NotNull FluidStack stack, Direction side, @NotNull Action action) {
      FluidStack ret = super.insertFluid(stack, side, action);
      if (action.execute() && ret.getAmount() < stack.getAmount()) {
         this.getMultiblock().triggerValveTransfer(this);
      }

      return ret;
   }

   @Override
   public int getRedstoneLevel() {
      return this.getMultiblock().getCurrentRedstoneLevel();
   }
}
