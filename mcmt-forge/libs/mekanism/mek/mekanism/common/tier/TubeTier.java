package mekanism.common.tier;

import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.config.value.CachedLongValue;
import mekanism.common.util.EnumUtils;

public enum TubeTier implements ITier {
   BASIC(BaseTier.BASIC, 4000L, 750L),
   ADVANCED(BaseTier.ADVANCED, 16000L, 2000L),
   ELITE(BaseTier.ELITE, 256000L, 64000L),
   ULTIMATE(BaseTier.ULTIMATE, 1024000L, 256000L);

   private final long baseCapacity;
   private final long basePull;
   private final BaseTier baseTier;
   private CachedLongValue capacityReference;
   private CachedLongValue pullReference;

   private TubeTier(BaseTier tier, long capacity, long pullAmount) {
      this.baseCapacity = capacity;
      this.basePull = pullAmount;
      this.baseTier = tier;
   }

   public static TubeTier get(BaseTier tier) {
      for (TubeTier transmitter : EnumUtils.TUBE_TIERS) {
         if (transmitter.getBaseTier() == tier) {
            return transmitter;
         }
      }

      return BASIC;
   }

   @Override
   public BaseTier getBaseTier() {
      return this.baseTier;
   }

   public long getTubeCapacity() {
      return this.capacityReference == null ? this.getBaseCapacity() : this.capacityReference.getOrDefault();
   }

   public long getTubePullAmount() {
      return this.pullReference == null ? this.getBasePull() : this.pullReference.getOrDefault();
   }

   public long getBaseCapacity() {
      return this.baseCapacity;
   }

   public long getBasePull() {
      return this.basePull;
   }

   public void setConfigReference(CachedLongValue capacityReference, CachedLongValue pullReference) {
      this.capacityReference = capacityReference;
      this.pullReference = pullReference;
   }
}
