package mekanism.common.tier;

import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.config.value.CachedIntValue;

public enum FluidTankTier implements ITier {
   BASIC(BaseTier.BASIC, 32000, 1000),
   ADVANCED(BaseTier.ADVANCED, 64000, 4000),
   ELITE(BaseTier.ELITE, 128000, 16000),
   ULTIMATE(BaseTier.ULTIMATE, 256000, 64000),
   CREATIVE(BaseTier.CREATIVE, Integer.MAX_VALUE, 1073741823);

   private final int baseStorage;
   private final int baseOutput;
   private final BaseTier baseTier;
   private CachedIntValue storageReference;
   private CachedIntValue outputReference;

   private FluidTankTier(BaseTier tier, int s, int o) {
      this.baseStorage = s;
      this.baseOutput = o;
      this.baseTier = tier;
   }

   @Override
   public BaseTier getBaseTier() {
      return this.baseTier;
   }

   public int getStorage() {
      return this.storageReference == null ? this.getBaseStorage() : this.storageReference.getOrDefault();
   }

   public int getOutput() {
      return this.outputReference == null ? this.getBaseOutput() : this.outputReference.getOrDefault();
   }

   public int getBaseStorage() {
      return this.baseStorage;
   }

   public int getBaseOutput() {
      return this.baseOutput;
   }

   public void setConfigReference(CachedIntValue storageReference, CachedIntValue outputReference) {
      this.storageReference = storageReference;
      this.outputReference = outputReference;
   }
}
