package mekanism.common.tile.multiblock;

import java.util.Collections;
import java.util.Set;
import mekanism.api.IContentsListener;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.energy.ProxiedEnergyContainerHolder;
import mekanism.common.content.matrix.MatrixMultiblockData;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.lib.multiblock.IMultiblockEjector;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.util.CableUtils;
import mekanism.common.util.text.BooleanStateDisplay;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TileEntityInductionPort extends TileEntityInductionCasing implements IMultiblockEjector {
   private Set<Direction> outputDirections = Collections.emptySet();

   public TileEntityInductionPort(BlockPos pos, BlockState state) {
      super(MekanismBlocks.INDUCTION_PORT, pos, state);
      this.delaySupplier = NO_DELAY;
   }

   @NotNull
   @Override
   protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
      return ProxiedEnergyContainerHolder.create(side -> !this.getActive(), side -> this.getActive(), side -> this.getMultiblock().getEnergyContainers(side));
   }

   protected boolean onUpdateServer(MatrixMultiblockData multiblock) {
      boolean needsPacket = super.onUpdateServer(multiblock);
      if (multiblock.isFormed() && this.getActive()) {
         CableUtils.emit(this.outputDirections, multiblock.getEnergyContainer(), this);
      }

      return needsPacket;
   }

   @Override
   public boolean persists(SubstanceType type) {
      return type == SubstanceType.ENERGY ? false : super.persists(type);
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
         player.m_5661_(
            MekanismLang.INDUCTION_PORT_MODE.translateColored(EnumColor.GRAY, new Object[]{BooleanStateDisplay.InputOutput.of(oldMode, true)}), true
         );
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
      methodDescription = "true -> output, false -> input"
   )
   void setMode(boolean output) {
      this.setActive(output);
   }
}
