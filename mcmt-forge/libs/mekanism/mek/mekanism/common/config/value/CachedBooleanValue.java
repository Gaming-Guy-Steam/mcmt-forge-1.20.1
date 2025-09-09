package mekanism.common.config.value;

import java.util.function.BooleanSupplier;
import mekanism.common.config.IMekanismConfig;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class CachedBooleanValue extends CachedValue<Boolean> implements BooleanSupplier {
   private boolean resolved;
   private boolean cachedValue;

   private CachedBooleanValue(IMekanismConfig config, ConfigValue<Boolean> internal) {
      super(config, internal);
   }

   public static CachedBooleanValue wrap(IMekanismConfig config, ConfigValue<Boolean> internal) {
      return new CachedBooleanValue(config, internal);
   }

   public boolean getOrDefault() {
      return !this.resolved && !this.isLoaded() ? (Boolean)this.internal.getDefault() : this.get();
   }

   public boolean get() {
      if (!this.resolved) {
         this.cachedValue = (Boolean)this.internal.get();
         this.resolved = true;
      }

      return this.cachedValue;
   }

   @Override
   public boolean getAsBoolean() {
      return this.get();
   }

   public void set(boolean value) {
      this.internal.set(value);
      this.cachedValue = value;
   }

   @Override
   protected boolean clearCachedValue(boolean checkChanged) {
      if (!this.resolved) {
         return false;
      } else {
         boolean oldCachedValue = this.cachedValue;
         this.resolved = false;
         return checkChanged && oldCachedValue != this.get();
      }
   }
}
