package mekanism.common.lib.distribution;

public class IntegerSplitInfo extends SplitInfo<Integer> {
   private int amountToSplit;
   private int amountPerTarget;
   private int sentSoFar;

   public IntegerSplitInfo(int amountToSplit, int totalTargets) {
      super(totalTargets);
      this.amountToSplit = amountToSplit;
      this.amountPerTarget = this.toSplitAmong == 0 ? 0 : amountToSplit / this.toSplitAmong;
   }

   public void send(Integer amountNeeded) {
      this.amountToSplit = this.amountToSplit - amountNeeded;
      this.sentSoFar = this.sentSoFar + amountNeeded;
      this.toSplitAmong--;
      if (amountNeeded != this.amountPerTarget && this.toSplitAmong != 0) {
         int amountPerLast = this.amountPerTarget;
         this.amountPerTarget = this.amountToSplit / this.toSplitAmong;
         if (!this.amountPerChanged && this.amountPerTarget != amountPerLast) {
            this.amountPerChanged = true;
         }
      }
   }

   public Integer getShareAmount() {
      return this.amountPerTarget;
   }

   public Integer getRemainderAmount() {
      return this.toSplitAmong == 0 ? this.amountPerTarget : this.amountPerTarget + this.amountToSplit % this.toSplitAmong;
   }

   public Integer getTotalSent() {
      return this.sentSoFar;
   }
}
