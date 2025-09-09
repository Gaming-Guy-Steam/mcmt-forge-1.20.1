package mekanism.common.tile.multiblock;

import java.util.Collections;
import java.util.Set;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.content.sps.SPSMultiblockData;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.lib.multiblock.IMultiblockEjector;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.text.BooleanStateDisplay;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TileEntitySPSPort extends TileEntitySPSCasing implements IMultiblockEjector {
   private MachineEnergyContainer<TileEntitySPSPort> energyContainer;
   private Set<Direction> outputDirections = Collections.emptySet();

   public TileEntitySPSPort(BlockPos pos, BlockState state) {
      super(MekanismBlocks.SPS_PORT, pos, state);
      this.delaySupplier = NO_DELAY;
   }

   @Override
   protected boolean onUpdateServer(SPSMultiblockData multiblock) {
      boolean needsPacket = super.onUpdateServer(multiblock);
      if (multiblock.isFormed()) {
         if (this.getActive()) {
            ChemicalUtil.emit(this.outputDirections, multiblock.outputTank, this);
         }

         if (!this.energyContainer.isEmpty() && multiblock.canSupplyCoilEnergy(this)) {
            multiblock.supplyCoilEnergy(this, this.energyContainer.extract(this.energyContainer.getEnergy(), Action.EXECUTE, AutomationType.INTERNAL));
         }
      }

      return needsPacket;
   }

   @NotNull
   @Override
   protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
      EnergyContainerHelper builder = EnergyContainerHelper.forSide(this::getDirection);
      builder.addContainer(this.energyContainer = MachineEnergyContainer.input(this, listener));
      return builder.build();
   }

   @NotNull
   @Override
   public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks(IContentsListener listener) {
      return side -> this.getMultiblock().getGasTanks(side);
   }

   @Override
   public boolean persists(SubstanceType type) {
      return type == SubstanceType.GAS ? false : super.persists(type);
   }

   @Override
   public void setEjectSides(Set<Direction> sides) {
      this.outputDirections = sides;
   }

   @Override
   public InteractionResult onSneakRightClick(Player player) {
      if (!this.isRemote()) {
         boolean oldMode = this.getActive();
         this.setActive(!oldMode);
         player.m_5661_(MekanismLang.SPS_PORT_MODE.translateColored(EnumColor.GRAY, new Object[]{BooleanStateDisplay.InputOutput.of(oldMode, true)}), true);
      }

      return InteractionResult.SUCCESS;
   }

   @Override
   public int getRedstoneLevel() {
      return this.getMultiblock().getCurrentRedstoneLevel();
   }

   @ComputerMethod(
      methodDescription = "true -> output, false -> input."
   )
   boolean getMode() {
      return this.getActive();
   }

   @ComputerMethod(
      methodDescription = "true -> output, false -> input."
   )
   void setMode(boolean output) {
      this.setActive(output);
   }
}
