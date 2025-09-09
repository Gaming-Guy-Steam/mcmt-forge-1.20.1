package mekanism.common.tier;

import java.util.Locale;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.FloatingLong;
import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.config.value.CachedFloatingLongValue;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public enum EnergyCubeTier implements ITier, StringRepresentable {
   BASIC(BaseTier.BASIC, FloatingLong.createConst(4000000L), FloatingLong.createConst(4000L)),
   ADVANCED(BaseTier.ADVANCED, FloatingLong.createConst(16000000L), FloatingLong.createConst(16000L)),
   ELITE(BaseTier.ELITE, FloatingLong.createConst(64000000L), FloatingLong.createConst(64000L)),
   ULTIMATE(BaseTier.ULTIMATE, FloatingLong.createConst(256000000L), FloatingLong.createConst(256000L)),
   CREATIVE(BaseTier.CREATIVE, FloatingLong.MAX_VALUE, FloatingLong.MAX_VALUE);

   private final FloatingLong baseMaxEnergy;
   private final FloatingLong baseOutput;
   private final BaseTier baseTier;
   @Nullable
   private CachedFloatingLongValue storageReference;
   @Nullable
   private CachedFloatingLongValue outputReference;

   private EnergyCubeTier(BaseTier tier, FloatingLong max, FloatingLong out) {
      this.baseMaxEnergy = max;
      this.baseOutput = out;
      this.baseTier = tier;
   }

   @Override
   public BaseTier getBaseTier() {
      return this.baseTier;
   }

   public String m_7912_() {
      return this.name().toLowerCase(Locale.ROOT);
   }

   public FloatingLong getMaxEnergy() {
      return this.storageReference == null ? this.getBaseMaxEnergy() : this.storageReference.getOrDefault();
   }

   public FloatingLong getOutput() {
      return this.outputReference == null ? this.getBaseOutput() : this.outputReference.getOrDefault();
   }

   public FloatingLong getBaseMaxEnergy() {
      return this.baseMaxEnergy;
   }

   public FloatingLong getBaseOutput() {
      return this.baseOutput;
   }

   public void setConfigReference(CachedFloatingLongValue storageReference, CachedFloatingLongValue outputReference) {
      this.storageReference = storageReference;
      this.outputReference = outputReference;
   }
}
