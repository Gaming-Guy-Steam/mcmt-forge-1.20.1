package mekanism.common.tier;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.FloatingLong;
import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.config.value.CachedFloatingLongValue;
import mekanism.common.util.EnumUtils;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public enum CableTier implements ITier {
   BASIC(BaseTier.BASIC, FloatingLong.createConst(8000L)),
   ADVANCED(BaseTier.ADVANCED, FloatingLong.createConst(128000L)),
   ELITE(BaseTier.ELITE, FloatingLong.createConst(1024000L)),
   ULTIMATE(BaseTier.ULTIMATE, FloatingLong.createConst(8192000L));

   private final FloatingLong baseCapacity;
   private final BaseTier baseTier;
   @Nullable
   private CachedFloatingLongValue capacityReference;

   private CableTier(BaseTier tier, FloatingLong capacity) {
      this.baseCapacity = capacity;
      this.baseTier = tier;
   }

   public static CableTier get(BaseTier tier) {
      for (CableTier transmitter : EnumUtils.CABLE_TIERS) {
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

   public FloatingLong getCableCapacity() {
      return this.capacityReference == null ? this.getBaseCapacity() : this.capacityReference.getOrDefault();
   }

   public FloatingLong getBaseCapacity() {
      return this.baseCapacity;
   }

   public void setConfigReference(CachedFloatingLongValue capacityReference) {
      this.capacityReference = capacityReference;
   }
}
