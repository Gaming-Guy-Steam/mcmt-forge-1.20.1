package mekanism.common.content.network.distribution;

import java.util.Collection;
import mekanism.api.math.MathUtils;
import mekanism.common.content.network.transmitter.MechanicalPipe;
import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.Target;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class FluidTransmitterSaveTarget extends Target<FluidTransmitterSaveTarget.SaveHandler, Integer, FluidStack> {
   public FluidTransmitterSaveTarget(@NotNull FluidStack type, Collection<MechanicalPipe> transmitters) {
      super(transmitters.size());
      this.extra = type;
      transmitters.forEach(transmitter -> this.addHandler(new FluidTransmitterSaveTarget.SaveHandler(transmitter)));
   }

   protected void acceptAmount(FluidTransmitterSaveTarget.SaveHandler handler, SplitInfo<Integer> splitInfo, Integer amount) {
      handler.acceptAmount(splitInfo, amount);
   }

   protected Integer simulate(FluidTransmitterSaveTarget.SaveHandler handler, @NotNull FluidStack fluidStack) {
      return handler.simulate(fluidStack);
   }

   public void saveShare() {
      for (FluidTransmitterSaveTarget.SaveHandler handler : this.handlers) {
         handler.saveShare();
      }
   }

   public class SaveHandler {
      private FluidStack currentStored = FluidStack.EMPTY;
      private final MechanicalPipe transmitter;

      public SaveHandler(MechanicalPipe transmitter) {
         this.transmitter = transmitter;
      }

      protected void acceptAmount(SplitInfo<Integer> splitInfo, Integer amount) {
         amount = Math.min(amount, MathUtils.clampToInt(this.transmitter.getCapacity() - this.currentStored.getAmount()));
         if (this.currentStored.isEmpty()) {
            this.currentStored = new FluidStack(FluidTransmitterSaveTarget.this.extra, amount);
         } else {
            this.currentStored.grow(amount);
         }

         splitInfo.send(amount);
      }

      protected Integer simulate(@NotNull FluidStack fluidStack) {
         return !this.currentStored.isEmpty() && !this.currentStored.isFluidEqual(fluidStack)
            ? 0
            : Math.min(fluidStack.getAmount(), MathUtils.clampToInt(this.transmitter.getCapacity() - this.currentStored.getAmount()));
      }

      protected void saveShare() {
         if (this.currentStored.isEmpty() != this.transmitter.saveShare.isEmpty()
            || !this.currentStored.isEmpty() && !this.currentStored.isFluidStackIdentical(this.transmitter.saveShare)) {
            this.transmitter.saveShare = this.currentStored;
            this.transmitter.getTransmitterTile().markForSave();
         }
      }
   }
}
