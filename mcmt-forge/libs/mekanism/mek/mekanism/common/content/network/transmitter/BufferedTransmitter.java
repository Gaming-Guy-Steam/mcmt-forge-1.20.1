package mekanism.common.content.network.transmitter;

import java.util.List;
import mekanism.common.lib.transmitter.DynamicBufferedNetwork;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

public abstract class BufferedTransmitter<ACCEPTOR, NETWORK extends DynamicBufferedNetwork<ACCEPTOR, NETWORK, BUFFER, TRANSMITTER>, BUFFER, TRANSMITTER extends BufferedTransmitter<ACCEPTOR, NETWORK, BUFFER, TRANSMITTER>>
   extends Transmitter<ACCEPTOR, NETWORK, TRANSMITTER> {
   public BufferedTransmitter(TileEntityTransmitter tile, TransmissionType... transmissionTypes) {
      super(tile, transmissionTypes);
   }

   protected abstract void pullFromAcceptors();

   public abstract long getCapacity();

   @NotNull
   public abstract BUFFER getBufferWithFallback();

   public abstract boolean noBufferOrFallback();

   protected boolean canHaveIncompatibleNetworks() {
      return false;
   }

   @Override
   public boolean isValidTransmitter(TileEntityTransmitter transmitter, Direction side) {
      return !this.canHaveIncompatibleNetworks()
            || !(
               transmitter.getTransmitter() instanceof BufferedTransmitter<?, ?, ?, ?> other
                  && other.canHaveIncompatibleNetworks()
                  && (this.hasTransmitterNetwork() && other.isOrphan() || other.hasTransmitterNetwork() && this.isOrphan())
            )
         ? super.isValidTransmitter(transmitter, side)
         : false;
   }

   @Override
   public void requestsUpdate() {
      if (this.canHaveIncompatibleNetworks()) {
         byte possibleTransmitters = this.getPossibleTransmitterConnections();
         byte possibleAcceptors = this.getPossibleAcceptorConnections();
         byte allPossibleConnections = (byte)(possibleTransmitters | possibleAcceptors);
         byte allCurrentConnections = this.getAllCurrentConnections();
         this.currentTransmitterConnections = possibleTransmitters;
         this.getAcceptorCache().currentAcceptorConnections = possibleAcceptors;
         if (allPossibleConnections != allCurrentConnections) {
            byte changedTransmitters = (byte)(allPossibleConnections ^ allCurrentConnections);

            for (Direction side : EnumUtils.DIRECTIONS) {
               if (connectionMapContainsSide(changedTransmitters, side)) {
                  TileEntityTransmitter tile = WorldUtils.getTileEntity(TileEntityTransmitter.class, this.getTileWorld(), this.getTilePos().m_121945_(side));
                  if (tile != null) {
                     tile.getTransmitter().refreshConnections(side.m_122424_());
                  }
               }
            }
         }
      }

      super.requestsUpdate();
   }

   @Override
   protected void recheckConnections(byte newlyEnabledTransmitters) {
      if (this.hasTransmitterNetwork()) {
         if (this.canHaveIncompatibleNetworks()) {
            for (Direction side : EnumUtils.DIRECTIONS) {
               if (connectionMapContainsSide(newlyEnabledTransmitters, side)) {
                  this.recheckConnectionPrechecked(side);
               }
            }
         }
      } else {
         super.recheckConnections(newlyEnabledTransmitters);
      }
   }

   @Override
   protected void recheckConnection(Direction side) {
      if (this.canHaveIncompatibleNetworks() && this.hasTransmitterNetwork()) {
         this.recheckConnectionPrechecked(side);
      }
   }

   private void recheckConnectionPrechecked(Direction side) {
      TileEntityTransmitter otherTile = WorldUtils.getTileEntity(TileEntityTransmitter.class, this.getTileWorld(), this.getTilePos().m_121945_(side));
      if (otherTile != null) {
         NETWORK network = this.getTransmitterNetwork();
         Transmitter<?, ?, ?> other = otherTile.getTransmitter();
         if (other instanceof BufferedTransmitter && ((BufferedTransmitter)other).canHaveIncompatibleNetworks() && other.hasTransmitterNetwork()) {
            NETWORK otherNetwork = (NETWORK)other.getTransmitterNetwork();
            if (network != otherNetwork && network.isCompatibleWith(otherNetwork)) {
               if (this.noBufferOrFallback()) {
                  NETWORK tempNetwork = network;
                  network = otherNetwork;
                  otherNetwork = tempNetwork;
               }

               List<TRANSMITTER> otherTransmitters = network.adoptTransmittersAndAcceptorsFrom(otherNetwork);
               otherNetwork.deregister();
               network.commit();
               network.clampBuffer();
               other.refreshConnections(side.m_122424_());

               for (TRANSMITTER otherTransmitter : otherTransmitters) {
                  otherTransmitter.requestsUpdate();
               }
            }
         }
      }
   }

   protected void handleContentsUpdateTag(@NotNull NETWORK network, @NotNull CompoundTag tag) {
      network.updateCapacity();
   }

   protected void updateClientNetwork(@NotNull NETWORK network) {
      super.updateClientNetwork(network);
      network.updateCapacity();
   }

   public abstract BUFFER releaseShare();

   @NotNull
   public abstract BUFFER getShare();

   @Override
   public void validateAndTakeShare() {
      if (this.hasTransmitterNetwork()) {
         this.getTransmitterNetwork().validateSaveShares(this.getTransmitter());
      }

      super.validateAndTakeShare();
   }
}
