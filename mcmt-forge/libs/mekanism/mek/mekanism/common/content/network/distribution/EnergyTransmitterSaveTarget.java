package mekanism.common.content.network.distribution;

import java.util.Collection;
import mekanism.api.math.FloatingLong;
import mekanism.common.content.network.transmitter.UniversalCable;
import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.Target;

public class EnergyTransmitterSaveTarget extends Target<EnergyTransmitterSaveTarget.SaveHandler, FloatingLong, FloatingLong> {
   public EnergyTransmitterSaveTarget(Collection<UniversalCable> transmitters) {
      super(transmitters.size());
      transmitters.forEach(transmitter -> this.addHandler(new EnergyTransmitterSaveTarget.SaveHandler(transmitter)));
   }

   protected void acceptAmount(EnergyTransmitterSaveTarget.SaveHandler transmitter, SplitInfo<FloatingLong> splitInfo, FloatingLong amount) {
      transmitter.acceptAmount(splitInfo, amount);
   }

   protected FloatingLong simulate(EnergyTransmitterSaveTarget.SaveHandler transmitter, FloatingLong energyToSend) {
      return transmitter.simulate(energyToSend);
   }

   public void saveShare() {
      for (EnergyTransmitterSaveTarget.SaveHandler cable : this.handlers) {
         cable.saveShare();
      }
   }

   public static class SaveHandler {
      private FloatingLong currentStored = FloatingLong.ZERO;
      private final UniversalCable transmitter;

      public SaveHandler(UniversalCable transmitter) {
         this.transmitter = transmitter;
      }

      protected void acceptAmount(SplitInfo<FloatingLong> splitInfo, FloatingLong amount) {
         amount = amount.min(this.transmitter.getCapacityAsFloatingLong().subtract(this.currentStored));
         this.currentStored = this.currentStored.plusEqual(amount);
         splitInfo.send(amount);
      }

      protected FloatingLong simulate(FloatingLong energyToSend) {
         return energyToSend.copy().min(this.transmitter.getCapacityAsFloatingLong().subtract(this.currentStored));
      }

      protected void saveShare() {
         if (!this.currentStored.isZero() || !this.transmitter.lastWrite.isZero()) {
            this.transmitter.lastWrite = this.currentStored;
            this.transmitter.getTransmitterTile().markForSave();
         }
      }
   }
}
