package mekanism.common.tier;

import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.util.EnumUtils;

public enum TransporterTier implements ITier {
   BASIC(BaseTier.BASIC, 1, 5),
   ADVANCED(BaseTier.ADVANCED, 16, 10),
   ELITE(BaseTier.ELITE, 32, 20),
   ULTIMATE(BaseTier.ULTIMATE, 64, 50);

   private final int basePull;
   private final int baseSpeed;
   private final BaseTier baseTier;
   private CachedIntValue pullReference;
   private CachedIntValue speedReference;

   private TransporterTier(BaseTier tier, int pull, int s) {
      this.basePull = pull;
      this.baseSpeed = s;
      this.baseTier = tier;
   }

   public static TransporterTier get(BaseTier tier) {
      for (TransporterTier transmitter : EnumUtils.TRANSPORTER_TIERS) {
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

   public int getPullAmount() {
      return this.pullReference == null ? this.getBasePull() : this.pullReference.getOrDefault();
   }

   public int getSpeed() {
      return this.speedReference == null ? this.getBaseSpeed() : this.speedReference.getOrDefault();
   }

   public int getBasePull() {
      return this.basePull;
   }

   public int getBaseSpeed() {
      return this.baseSpeed;
   }

   public void setConfigReference(CachedIntValue pullReference, CachedIntValue speedReference) {
      this.pullReference = pullReference;
      this.speedReference = speedReference;
   }
}
