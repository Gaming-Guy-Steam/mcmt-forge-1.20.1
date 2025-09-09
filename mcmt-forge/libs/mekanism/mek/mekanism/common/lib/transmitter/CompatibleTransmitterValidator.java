package mekanism.common.lib.transmitter;

import mekanism.api.chemical.merged.BoxedChemical;
import mekanism.common.capabilities.chemical.BoxedChemicalHandler;
import mekanism.common.content.network.BoxedChemicalNetwork;
import mekanism.common.content.network.FluidNetwork;
import mekanism.common.content.network.transmitter.BoxedPressurizedTube;
import mekanism.common.content.network.transmitter.MechanicalPipe;
import mekanism.common.content.network.transmitter.Transmitter;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class CompatibleTransmitterValidator<ACCEPTOR, NETWORK extends DynamicNetwork<ACCEPTOR, NETWORK, TRANSMITTER>, TRANSMITTER extends Transmitter<ACCEPTOR, NETWORK, TRANSMITTER>> {
   public boolean isNetworkCompatible(NETWORK net) {
      return true;
   }

   public boolean isTransmitterCompatible(Transmitter<?, ?, ?> transmitter) {
      return true;
   }

   public static class CompatibleChemicalTransmitterValidator
      extends CompatibleTransmitterValidator<BoxedChemicalHandler, BoxedChemicalNetwork, BoxedPressurizedTube> {
      private BoxedChemical buffer;

      public CompatibleChemicalTransmitterValidator(BoxedPressurizedTube transmitter) {
         this.buffer = transmitter.getBufferWithFallback().getType();
      }

      private boolean compareBuffers(BoxedChemical otherBuffer) {
         if (this.buffer.isEmpty()) {
            this.buffer = otherBuffer;
            return true;
         } else {
            return otherBuffer.isEmpty() || this.buffer.equals(otherBuffer);
         }
      }

      public boolean isNetworkCompatible(BoxedChemicalNetwork network) {
         if (super.isNetworkCompatible((NETWORK)network)) {
            BoxedChemical otherBuffer;
            if (network.getTransmitterValidator() instanceof CompatibleTransmitterValidator.CompatibleChemicalTransmitterValidator validator) {
               otherBuffer = validator.buffer;
            } else {
               otherBuffer = network.getBuffer().getType();
               if (otherBuffer.isEmpty() && network.getPrevTransferAmount() > 0L) {
                  otherBuffer = network.lastChemical;
               }
            }

            return this.compareBuffers(otherBuffer);
         } else {
            return false;
         }
      }

      @Override
      public boolean isTransmitterCompatible(Transmitter<?, ?, ?> transmitter) {
         return super.isTransmitterCompatible(transmitter)
            && transmitter instanceof BoxedPressurizedTube tube
            && this.compareBuffers(tube.getBufferWithFallback().getType());
      }
   }

   public static class CompatibleFluidTransmitterValidator extends CompatibleTransmitterValidator<IFluidHandler, FluidNetwork, MechanicalPipe> {
      private FluidStack buffer;

      public CompatibleFluidTransmitterValidator(MechanicalPipe transmitter) {
         this.buffer = transmitter.getBufferWithFallback();
      }

      private boolean compareBuffers(FluidStack otherBuffer) {
         if (this.buffer.isEmpty()) {
            this.buffer = otherBuffer;
            return true;
         } else {
            return otherBuffer.isEmpty() || this.buffer.isFluidEqual(otherBuffer);
         }
      }

      public boolean isNetworkCompatible(FluidNetwork network) {
         if (super.isNetworkCompatible((NETWORK)network)) {
            FluidStack otherBuffer;
            if (network.getTransmitterValidator() instanceof CompatibleTransmitterValidator.CompatibleFluidTransmitterValidator validator) {
               otherBuffer = validator.buffer;
            } else {
               otherBuffer = network.getBuffer();
               if (otherBuffer.isEmpty() && network.getPrevTransferAmount() > 0) {
                  otherBuffer = network.lastFluid;
               }
            }

            return this.compareBuffers(otherBuffer);
         } else {
            return false;
         }
      }

      @Override
      public boolean isTransmitterCompatible(Transmitter<?, ?, ?> transmitter) {
         return super.isTransmitterCompatible(transmitter) && transmitter instanceof MechanicalPipe pipe && this.compareBuffers(pipe.getBufferWithFallback());
      }
   }
}
