package mekanism.common.config.value;

import java.util.function.LongSupplier;
import mekanism.common.config.IMekanismConfig;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class CachedLongValue extends CachedValue<Long> implements LongSupplier {
   private boolean resolved;
   private long cachedValue;

   private CachedLongValue(IMekanismConfig config, ConfigValue<Long> internal) {
      super(config, internal);
   }

   public static CachedLongValue wrap(IMekanismConfig config, ConfigValue<Long> internal) {
      return new CachedLongValue(config, internal);
   }

   public long getOrDefault() {
      return !this.resolved && !this.isLoaded() ? (Long)this.internal.getDefault() : this.get();
   }

   public long get() {
      if (!this.resolved) {
         this.cachedValue = (Long)this.internal.get();
         this.resolved = true;
      }

      return this.cachedValue;
   }

   @Override
   public long getAsLong() {
      return this.get();
   }

   public void set(long value) {
      this.internal.set(value);
      this.cachedValue = value;
   }

   @Override
   protected boolean clearCachedValue(boolean checkChanged) {
      if (!this.resolved) {
         return false;
      } else {
         long oldCachedValue = this.cachedValue;
         this.resolved = false;
         return checkChanged && oldCachedValue != this.get();
      }
   }
}
