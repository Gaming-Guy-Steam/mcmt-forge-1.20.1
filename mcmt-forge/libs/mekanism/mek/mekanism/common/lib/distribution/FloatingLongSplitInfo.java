package mekanism.common.lib.distribution;

import mekanism.api.math.FloatingLong;

public class FloatingLongSplitInfo extends SplitInfo<FloatingLong> {
   private FloatingLong amountToSplit;
   private FloatingLong amountPerTarget;
   private FloatingLong sentSoFar;

   public FloatingLongSplitInfo(FloatingLong amountToSplit, int totalTargets) {
      super(totalTargets);
      this.amountToSplit = amountToSplit.copy();
      this.amountPerTarget = this.toSplitAmong == 0 ? FloatingLong.ZERO : amountToSplit.divide((long)this.toSplitAmong);
      this.sentSoFar = FloatingLong.ZERO;
   }

   public void send(FloatingLong amountNeeded) {
      this.amountToSplit = this.amountToSplit.minusEqual(amountNeeded);
      this.sentSoFar = this.sentSoFar.plusEqual(amountNeeded);
      this.toSplitAmong--;
      if (!amountNeeded.equals(this.amountPerTarget) && this.toSplitAmong != 0) {
         FloatingLong amountPerLast = this.amountPerTarget;
         this.amountPerTarget = this.amountToSplit.divide((long)this.toSplitAmong);
         if (!this.amountPerChanged && !this.amountPerTarget.equals(amountPerLast)) {
            this.amountPerChanged = true;
         }
      }
   }

   public FloatingLong getShareAmount() {
      return this.amountPerTarget;
   }

   public FloatingLong getRemainderAmount() {
      return this.amountPerTarget;
   }

   public FloatingLong getTotalSent() {
      return this.sentSoFar;
   }
}
