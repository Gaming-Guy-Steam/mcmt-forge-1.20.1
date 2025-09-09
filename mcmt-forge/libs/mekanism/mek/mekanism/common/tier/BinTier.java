package mekanism.common.tier;

import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.config.value.CachedIntValue;

public enum BinTier implements ITier {
   BASIC(BaseTier.BASIC, 4096),
   ADVANCED(BaseTier.ADVANCED, 8192),
   ELITE(BaseTier.ELITE, 32768),
   ULTIMATE(BaseTier.ULTIMATE, 262144),
   CREATIVE(BaseTier.CREATIVE, Integer.MAX_VALUE);

   private final int baseStorage;
   private final BaseTier baseTier;
   private CachedIntValue storageReference;

   private BinTier(BaseTier tier, int s) {
      this.baseTier = tier;
      this.baseStorage = s;
   }

   @Override
   public BaseTier getBaseTier() {
      return this.baseTier;
   }

   public int getStorage() {
      return this.storageReference == null ? this.getBaseStorage() : this.storageReference.getOrDefault();
   }

   public int getBaseStorage() {
      return this.baseStorage;
   }

   public void setConfigReference(CachedIntValue storageReference) {
      this.storageReference = storageReference;
   }
}
