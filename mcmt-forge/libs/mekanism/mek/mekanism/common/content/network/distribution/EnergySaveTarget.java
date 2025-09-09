package mekanism.common.content.network.distribution;

import java.util.Collection;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.Target;

public class EnergySaveTarget extends Target<EnergySaveTarget.SaveHandler, FloatingLong, FloatingLong> {
   public EnergySaveTarget() {
   }

   public EnergySaveTarget(Collection<EnergySaveTarget.SaveHandler> allHandlers) {
      super(allHandlers);
   }

   public EnergySaveTarget(int expectedSize) {
      super(expectedSize);
   }

   protected void acceptAmount(EnergySaveTarget.SaveHandler handler, SplitInfo<FloatingLong> splitInfo, FloatingLong amount) {
      handler.acceptAmount(splitInfo, amount);
   }

   protected FloatingLong simulate(EnergySaveTarget.SaveHandler handler, FloatingLong energyToSend) {
      return handler.simulate(energyToSend);
   }

   public void save() {
      for (EnergySaveTarget.SaveHandler handler : this.handlers) {
         handler.save();
      }
   }

   public void addDelegate(IEnergyContainer delegate) {
      this.addHandler(new EnergySaveTarget.SaveHandler(delegate));
   }

   @NothingNullByDefault
   public static class SaveHandler {
      private final IEnergyContainer delegate;
      private FloatingLong currentStored = FloatingLong.ZERO;

      public SaveHandler(IEnergyContainer delegate) {
         this.delegate = delegate;
      }

      protected void acceptAmount(SplitInfo<FloatingLong> splitInfo, FloatingLong amount) {
         amount = amount.min(this.delegate.getMaxEnergy().subtract(this.currentStored));
         this.currentStored = this.currentStored.plusEqual(amount);
         splitInfo.send(amount);
      }

      protected FloatingLong simulate(FloatingLong energyToSend) {
         return energyToSend.copy().min(this.delegate.getMaxEnergy().subtract(this.currentStored));
      }

      protected void save() {
         this.delegate.setEnergy(this.currentStored);
      }
   }
}
