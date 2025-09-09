package mekanism.common.tier;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.FloatingLong;
import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.config.value.CachedFloatingLongValue;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public enum InductionProviderTier implements ITier {
   BASIC(BaseTier.BASIC, FloatingLong.createConst(256000L)),
   ADVANCED(BaseTier.ADVANCED, FloatingLong.createConst(2048000L)),
   ELITE(BaseTier.ELITE, FloatingLong.createConst(16384000L)),
   ULTIMATE(BaseTier.ULTIMATE, FloatingLong.createConst(131072000L));

   private final FloatingLong baseOutput;
   private final BaseTier baseTier;
   @Nullable
   private CachedFloatingLongValue outputReference;

   private InductionProviderTier(BaseTier tier, FloatingLong out) {
      this.baseOutput = out;
      this.baseTier = tier;
   }

   @Override
   public BaseTier getBaseTier() {
      return this.baseTier;
   }

   public FloatingLong getOutput() {
      return this.outputReference == null ? this.getBaseOutput() : this.outputReference.getOrDefault();
   }

   public FloatingLong getBaseOutput() {
      return this.baseOutput;
   }

   public void setConfigReference(CachedFloatingLongValue outputReference) {
      this.outputReference = outputReference;
   }
}
