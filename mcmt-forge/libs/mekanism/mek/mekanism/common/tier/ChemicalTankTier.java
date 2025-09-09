package mekanism.common.tier;

import java.util.Locale;
import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.config.value.CachedLongValue;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum ChemicalTankTier implements ITier, StringRepresentable {
   BASIC(BaseTier.BASIC, 64000L, 1000L),
   ADVANCED(BaseTier.ADVANCED, 256000L, 16000L),
   ELITE(BaseTier.ELITE, 1024000L, 128000L),
   ULTIMATE(BaseTier.ULTIMATE, 8192000L, 512000L),
   CREATIVE(BaseTier.CREATIVE, Long.MAX_VALUE, 4611686018427387903L);

   private final long baseStorage;
   private final long baseOutput;
   private final BaseTier baseTier;
   private CachedLongValue storageReference;
   private CachedLongValue outputReference;

   private ChemicalTankTier(BaseTier tier, long s, long o) {
      this.baseStorage = s;
      this.baseOutput = o;
      this.baseTier = tier;
   }

   @Override
   public BaseTier getBaseTier() {
      return this.baseTier;
   }

   @NotNull
   public String m_7912_() {
      return this.name().toLowerCase(Locale.ROOT);
   }

   public long getStorage() {
      return this.storageReference == null ? this.getBaseStorage() : this.storageReference.getOrDefault();
   }

   public long getOutput() {
      return this.outputReference == null ? this.getBaseOutput() : this.outputReference.getOrDefault();
   }

   public long getBaseStorage() {
      return this.baseStorage;
   }

   public long getBaseOutput() {
      return this.baseOutput;
   }

   public void setConfigReference(CachedLongValue storageReference, CachedLongValue outputReference) {
      this.storageReference = storageReference;
      this.outputReference = outputReference;
   }
}
