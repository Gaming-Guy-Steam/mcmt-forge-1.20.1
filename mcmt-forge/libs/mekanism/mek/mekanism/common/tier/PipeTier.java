package mekanism.common.tier;

import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.util.EnumUtils;

public enum PipeTier implements ITier {
   BASIC(BaseTier.BASIC, 2000, 250),
   ADVANCED(BaseTier.ADVANCED, 8000, 1000),
   ELITE(BaseTier.ELITE, 32000, 8000),
   ULTIMATE(BaseTier.ULTIMATE, 128000, 32000);

   private final int baseCapacity;
   private final int basePull;
   private final BaseTier baseTier;
   private CachedIntValue capacityReference;
   private CachedIntValue pullReference;

   private PipeTier(BaseTier tier, int capacity, int pullAmount) {
      this.baseCapacity = capacity;
      this.basePull = pullAmount;
      this.baseTier = tier;
   }

   public static PipeTier get(BaseTier tier) {
      for (PipeTier transmitter : EnumUtils.PIPE_TIERS) {
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

   public int getPipeCapacity() {
      return this.capacityReference == null ? this.getBaseCapacity() : this.capacityReference.getOrDefault();
   }

   public int getPipePullAmount() {
      return this.pullReference == null ? this.getBasePull() : this.pullReference.getOrDefault();
   }

   public int getBaseCapacity() {
      return this.baseCapacity;
   }

   public int getBasePull() {
      return this.basePull;
   }

   public void setConfigReference(CachedIntValue capacityReference, CachedIntValue pullReference) {
      this.capacityReference = capacityReference;
      this.pullReference = pullReference;
   }
}
