package mekanism.common.tier;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.FloatingLong;
import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.config.value.CachedFloatingLongValue;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public enum InductionCellTier implements ITier {
   BASIC(BaseTier.BASIC, FloatingLong.createConst(8000000000L)),
   ADVANCED(BaseTier.ADVANCED, FloatingLong.createConst(64000000000L)),
   ELITE(BaseTier.ELITE, FloatingLong.createConst(512000000000L)),
   ULTIMATE(BaseTier.ULTIMATE, FloatingLong.createConst(4000000000000L));

   private final FloatingLong baseMaxEnergy;
   private final BaseTier baseTier;
   @Nullable
   private CachedFloatingLongValue storageReference;

   private InductionCellTier(BaseTier tier, FloatingLong max) {
      this.baseMaxEnergy = max;
      this.baseTier = tier;
   }

   @Override
   public BaseTier getBaseTier() {
      return this.baseTier;
   }

   public FloatingLong getMaxEnergy() {
      return this.storageReference == null ? this.getBaseMaxEnergy() : this.storageReference.getOrDefault();
   }

   public FloatingLong getBaseMaxEnergy() {
      return this.baseMaxEnergy;
   }

   public void setConfigReference(CachedFloatingLongValue storageReference) {
      this.storageReference = storageReference;
   }
}
