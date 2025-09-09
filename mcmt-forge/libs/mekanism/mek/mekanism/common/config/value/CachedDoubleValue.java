package mekanism.common.config.value;

import java.util.function.DoubleSupplier;
import mekanism.common.config.IMekanismConfig;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class CachedDoubleValue extends CachedValue<Double> implements DoubleSupplier {
   private boolean resolved;
   private double cachedValue;

   private CachedDoubleValue(IMekanismConfig config, ConfigValue<Double> internal) {
      super(config, internal);
   }

   public static CachedDoubleValue wrap(IMekanismConfig config, ConfigValue<Double> internal) {
      return new CachedDoubleValue(config, internal);
   }

   public double getOrDefault() {
      return !this.resolved && !this.isLoaded() ? (Double)this.internal.getDefault() : this.get();
   }

   public double get() {
      if (!this.resolved) {
         this.cachedValue = (Double)this.internal.get();
         this.resolved = true;
      }

      return this.cachedValue;
   }

   @Override
   public double getAsDouble() {
      return this.get();
   }

   public void set(double value) {
      this.internal.set(value);
      this.cachedValue = value;
   }

   @Override
   protected boolean clearCachedValue(boolean checkChanged) {
      if (!this.resolved) {
         return false;
      } else {
         double oldCachedValue = this.cachedValue;
         this.resolved = false;
         return checkChanged && oldCachedValue != this.get();
      }
   }
}
