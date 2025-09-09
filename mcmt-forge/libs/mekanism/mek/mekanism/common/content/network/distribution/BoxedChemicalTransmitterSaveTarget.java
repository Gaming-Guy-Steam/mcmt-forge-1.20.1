package mekanism.common.content.network.distribution;

import java.util.Collection;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.common.content.network.transmitter.BoxedPressurizedTube;
import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.Target;
import mekanism.common.util.ChemicalUtil;
import org.jetbrains.annotations.NotNull;

public class BoxedChemicalTransmitterSaveTarget<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>>
   extends Target<BoxedChemicalTransmitterSaveTarget<CHEMICAL, STACK>.SaveHandler, Long, STACK> {
   public BoxedChemicalTransmitterSaveTarget(@NotNull STACK empty, @NotNull STACK type, Collection<BoxedPressurizedTube> transmitters) {
      super(transmitters.size());
      this.extra = type;
      transmitters.forEach(transmitter -> this.addHandler(new BoxedChemicalTransmitterSaveTarget.SaveHandler(empty, transmitter)));
   }

   protected void acceptAmount(BoxedChemicalTransmitterSaveTarget<CHEMICAL, STACK>.SaveHandler handler, SplitInfo<Long> splitInfo, Long amount) {
      handler.acceptAmount(splitInfo, amount);
   }

   protected Long simulate(BoxedChemicalTransmitterSaveTarget<CHEMICAL, STACK>.SaveHandler handler, @NotNull STACK chemicalStack) {
      return handler.simulate(chemicalStack);
   }

   public void saveShare() {
      for (BoxedChemicalTransmitterSaveTarget<CHEMICAL, STACK>.SaveHandler handler : this.handlers) {
         handler.saveShare();
      }
   }

   public class SaveHandler {
      private STACK currentStored;
      private final BoxedPressurizedTube transmitter;

      public SaveHandler(@NotNull STACK empty, BoxedPressurizedTube transmitter) {
         this.currentStored = empty;
         this.transmitter = transmitter;
      }

      protected void acceptAmount(SplitInfo<Long> splitInfo, Long amount) {
         amount = Math.min(amount, this.transmitter.getCapacity() - this.currentStored.getAmount());
         if (this.currentStored.isEmpty()) {
            this.currentStored = ChemicalUtil.copyWithAmount(BoxedChemicalTransmitterSaveTarget.this.extra, amount);
         } else {
            this.currentStored.grow(amount);
         }

         splitInfo.send(amount);
      }

      protected Long simulate(@NotNull STACK chemicalStack) {
         return !this.currentStored.isEmpty() && !this.currentStored.isTypeEqual(chemicalStack)
            ? 0L
            : Math.min(chemicalStack.getAmount(), this.transmitter.getCapacity() - this.currentStored.getAmount());
      }

      protected void saveShare() {
         boolean shouldSave = false;
         if (this.currentStored.isEmpty() != this.transmitter.saveShare.isEmpty()) {
            shouldSave = true;
         } else if (!this.currentStored.isEmpty()) {
            ChemicalType chemicalType = ChemicalType.getTypeFor(this.currentStored);
            shouldSave = chemicalType != this.transmitter.saveShare.getChemicalType()
               || !this.currentStored.isStackIdentical((ChemicalStack<CHEMICAL>)this.transmitter.saveShare.getChemicalStack());
         }

         if (shouldSave) {
            this.transmitter.saveShare = this.currentStored.isEmpty() ? BoxedChemicalStack.EMPTY : BoxedChemicalStack.box(this.currentStored);
            this.transmitter.getTransmitterTile().markForSave();
         }
      }
   }
}
