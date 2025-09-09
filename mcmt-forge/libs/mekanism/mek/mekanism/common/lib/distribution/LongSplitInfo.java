package mekanism.common.lib.distribution;

public class LongSplitInfo extends SplitInfo<Long> {
   private long amountToSplit;
   private long amountPerTarget;
   private long sentSoFar;

   public LongSplitInfo(long amountToSplit, int totalTargets) {
      super(totalTargets);
      this.amountToSplit = amountToSplit;
      this.amountPerTarget = this.toSplitAmong == 0 ? 0L : amountToSplit / this.toSplitAmong;
   }

   public void send(Long amountNeeded) {
      this.amountToSplit = this.amountToSplit - amountNeeded;
      this.sentSoFar = this.sentSoFar + amountNeeded;
      this.toSplitAmong--;
      if (amountNeeded != this.amountPerTarget && this.toSplitAmong != 0) {
         long amountPerLast = this.amountPerTarget;
         this.amountPerTarget = this.amountToSplit / this.toSplitAmong;
         if (!this.amountPerChanged && this.amountPerTarget != amountPerLast) {
            this.amountPerChanged = true;
         }
      }
   }

   public Long getShareAmount() {
      return this.amountPerTarget;
   }

   public Long getRemainderAmount() {
      return this.toSplitAmong == 0 ? this.amountPerTarget : this.amountPerTarget + this.amountToSplit % this.toSplitAmong;
   }

   public Long getTotalSent() {
      return this.sentSoFar;
   }
}
