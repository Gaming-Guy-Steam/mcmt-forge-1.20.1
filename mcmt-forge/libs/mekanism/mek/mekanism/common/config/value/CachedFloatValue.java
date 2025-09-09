package mekanism.common.config.value;

import mekanism.api.functions.FloatSupplier;
import mekanism.common.config.IMekanismConfig;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class CachedFloatValue extends CachedValue<Double> implements FloatSupplier {
   private boolean resolved;
   private float cachedValue;

   private CachedFloatValue(IMekanismConfig config, ConfigValue<Double> internal) {
      super(config, internal);
   }

   public static CachedFloatValue wrap(IMekanismConfig config, ConfigValue<Double> internal) {
      return new CachedFloatValue(config, internal);
   }

   public float getOrDefault() {
      return !this.resolved && !this.isLoaded() ? this.clampInternal((Double)this.internal.getDefault()) : this.get();
   }

   public float get() {
      if (!this.resolved) {
         this.cachedValue = this.clampInternal((Double)this.internal.get());
         this.resolved = true;
      }

      return this.cachedValue;
   }

   private float clampInternal(Double val) {
      if (val == null) {
         return 0.0F;
      } else if (val > Float.MAX_VALUE) {
         return Float.MAX_VALUE;
      } else {
         return val < -Float.MAX_VALUE ? -Float.MAX_VALUE : val.floatValue();
      }
   }

   @Override
   public float getAsFloat() {
      return this.get();
   }

   public void set(float value) {
      this.internal.set((double)value);
      this.cachedValue = value;
   }

   @Override
   protected boolean clearCachedValue(boolean checkChanged) {
      if (!this.resolved) {
         return false;
      } else {
         float oldCachedValue = this.cachedValue;
         this.resolved = false;
         return checkChanged && oldCachedValue != this.get();
      }
   }
}
