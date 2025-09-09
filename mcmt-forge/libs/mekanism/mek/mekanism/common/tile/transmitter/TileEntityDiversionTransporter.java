package mekanism.common.tile.transmitter;

import mekanism.api.providers.IBlockProvider;
import mekanism.client.model.data.TransmitterModelData;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.content.network.transmitter.DiversionTransporter;
import mekanism.common.integration.computer.ComputerCapabilityHelper;
import mekanism.common.integration.computer.IComputerTile;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TileEntityDiversionTransporter extends TileEntityLogisticalTransporterBase implements IComputerTile {
   public TileEntityDiversionTransporter(BlockPos pos, BlockState state) {
      super(MekanismBlocks.DIVERSION_TRANSPORTER, pos, state);
      ComputerCapabilityHelper.addComputerCapabilities(this, x$0 -> this.addCapabilityResolver(x$0));
   }

   protected DiversionTransporter createTransmitter(IBlockProvider blockProvider) {
      return new DiversionTransporter(this);
   }

   public DiversionTransporter getTransmitter() {
      return (DiversionTransporter)super.getTransmitter();
   }

   @Override
   public TransmitterType getTransmitterType() {
      return TransmitterType.DIVERSION_TRANSPORTER;
   }

   @NotNull
   @Override
   protected TransmitterModelData initModelData() {
      return new TransmitterModelData.Diversion();
   }

   @Override
   public String getComputerName() {
      return "diversionTransporter";
   }

   @ComputerMethod
   DiversionTransporter.DiversionControl getMode(Direction side) {
      return this.getTransmitter().modes[side.ordinal()];
   }

   @ComputerMethod
   void setMode(Direction side, DiversionTransporter.DiversionControl mode) {
      this.getTransmitter().updateMode(side, mode);
   }

   @ComputerMethod
   void incrementMode(Direction side) {
      DiversionTransporter transmitter = this.getTransmitter();
      transmitter.updateMode(side, transmitter.modes[side.ordinal()].getNext());
   }

   @ComputerMethod
   void decrementMode(Direction side) {
      DiversionTransporter transmitter = this.getTransmitter();
      transmitter.updateMode(side, transmitter.modes[side.ordinal()].getPrevious());
   }
}
