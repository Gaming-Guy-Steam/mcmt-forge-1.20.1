package mekanism.common.tier;

import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;

public enum QIODriveTier implements ITier {
   BASE(BaseTier.BASIC, 16000L, 128),
   HYPER_DENSE(BaseTier.ADVANCED, 128000L, 256),
   TIME_DILATING(BaseTier.ELITE, 1048000L, 1024),
   SUPERMASSIVE(BaseTier.ULTIMATE, 16000000000L, 8192);

   private final BaseTier baseTier;
   private final long count;
   private final int types;

   private QIODriveTier(BaseTier tier, long count, int types) {
      this.baseTier = tier;
      this.count = count;
      this.types = types;
   }

   @Override
   public BaseTier getBaseTier() {
      return this.baseTier;
   }

   public long getMaxCount() {
      return this.count;
   }

   public int getMaxTypes() {
      return this.types;
   }
}
