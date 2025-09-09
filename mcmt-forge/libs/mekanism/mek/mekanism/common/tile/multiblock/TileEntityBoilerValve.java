package mekanism.common.tile.multiblock;

import java.util.Collections;
import java.util.Set;
import mekanism.api.Action;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.AttributeStateBoilerValveMode;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.content.boiler.BoilerMultiblockData;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.lib.multiblock.IMultiblockEjector;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.util.ChemicalUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityBoilerValve extends TileEntityBoilerCasing implements IMultiblockEjector {
   private Set<Direction> outputDirections = Collections.emptySet();

   public TileEntityBoilerValve(BlockPos pos, BlockState state) {
      super(MekanismBlocks.BOILER_VALVE, pos, state);
   }

   @NotNull
   @Override
   public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks(IContentsListener listener) {
      return side -> this.getMultiblock().getGasTanks(side);
   }

   @NotNull
   @Override
   protected IFluidTankHolder getInitialFluidTanks(IContentsListener listener) {
      return side -> this.getMultiblock().getFluidTanks(side);
   }

   protected boolean onUpdateServer(BoilerMultiblockData multiblock) {
      boolean needsPacket = super.onUpdateServer(multiblock);
      if (multiblock.isFormed()) {
         AttributeStateBoilerValveMode.BoilerValveMode mode = this.getMode();
         if (mode == AttributeStateBoilerValveMode.BoilerValveMode.OUTPUT_STEAM) {
            ChemicalUtil.emit(this.outputDirections, multiblock.steamTank, this);
         } else if (mode == AttributeStateBoilerValveMode.BoilerValveMode.OUTPUT_COOLANT) {
            ChemicalUtil.emit(this.outputDirections, multiblock.cooledCoolantTank, this);
         }
      }

      return needsPacket;
   }

   @Override
   public boolean persists(SubstanceType type) {
      return type != SubstanceType.FLUID && type != SubstanceType.GAS ? super.persists(type) : false;
   }

   @Override
   public void setEjectSides(Set<Direction> sides) {
      this.outputDirections = sides;
   }

   @Override
   public int getRedstoneLevel() {
      return this.getMultiblock().getCurrentRedstoneLevel();
   }

   @ComputerMethod(
      methodDescription = "Get the current configuration of this valve"
   )
   AttributeStateBoilerValveMode.BoilerValveMode getMode() {
      return (AttributeStateBoilerValveMode.BoilerValveMode)this.m_58900_().m_61143_(AttributeStateBoilerValveMode.modeProperty);
   }

   @ComputerMethod(
      methodDescription = "Change the configuration of this valve"
   )
   void setMode(AttributeStateBoilerValveMode.BoilerValveMode mode) {
      if (mode != this.getMode()) {
         this.f_58857_.m_46597_(this.f_58858_, (BlockState)this.m_58900_().m_61124_(AttributeStateBoilerValveMode.modeProperty, mode));
      }
   }

   @Override
   public InteractionResult onSneakRightClick(Player player) {
      if (!this.isRemote()) {
         AttributeStateBoilerValveMode.BoilerValveMode mode = this.getMode().getNext();
         this.setMode(mode);
         player.m_5661_(MekanismLang.BOILER_VALVE_MODE_CHANGE.translateColored(EnumColor.GRAY, new Object[]{mode}), true);
      }

      return InteractionResult.SUCCESS;
   }

   @NotNull
   @Override
   public FluidStack insertFluid(@NotNull FluidStack stack, Direction side, @NotNull Action action) {
      FluidStack ret = super.insertFluid(stack, side, action);
      if (ret.getAmount() < stack.getAmount() && action.execute()) {
         this.getMultiblock().triggerValveTransfer(this);
      }

      return ret;
   }

   @Override
   public boolean insertGasCheck(int tank, @Nullable Direction side) {
      return this.getMode() != AttributeStateBoilerValveMode.BoilerValveMode.INPUT ? false : super.insertGasCheck(tank, side);
   }

   @Override
   public boolean extractGasCheck(int tank, @Nullable Direction side) {
      AttributeStateBoilerValveMode.BoilerValveMode mode = this.getMode();
      return mode != AttributeStateBoilerValveMode.BoilerValveMode.INPUT
            && (tank != 2 || mode != AttributeStateBoilerValveMode.BoilerValveMode.OUTPUT_STEAM)
            && (tank != 0 || mode != AttributeStateBoilerValveMode.BoilerValveMode.OUTPUT_COOLANT)
         ? super.extractGasCheck(tank, side)
         : false;
   }

   @ComputerMethod(
      methodDescription = "Toggle the current valve configuration to the next option in the list"
   )
   void incrementMode() {
      this.setMode(this.getMode().getNext());
   }

   @ComputerMethod(
      methodDescription = "Toggle the current valve configuration to the previous option in the list"
   )
   void decrementMode() {
      this.setMode(this.getMode().getPrevious());
   }
}
