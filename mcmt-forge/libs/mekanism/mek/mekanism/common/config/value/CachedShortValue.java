package mekanism.common.config.value;

import mekanism.api.functions.ShortSupplier;
import mekanism.common.config.IMekanismConfig;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class CachedShortValue extends CachedValue<Short> implements ShortSupplier {
   private boolean resolved;
   private short cachedValue;

   private CachedShortValue(IMekanismConfig config, ConfigValue<Short> internal) {
      super(config, internal);
   }

   public static CachedShortValue wrap(IMekanismConfig config, ConfigValue<Short> internal) {
      return new CachedShortValue(config, internal);
   }

   public short getOrDefault() {
      return !this.resolved && !this.isLoaded() ? (Short)this.internal.getDefault() : this.get();
   }

   public short get() {
      if (!this.resolved) {
         this.cachedValue = (Short)this.internal.get();
         this.resolved = true;
      }

      return this.cachedValue;
   }

   @Override
   public short getAsShort() {
      return this.get();
   }

   public void set(short value) {
      this.internal.set(value);
      this.cachedValue = value;
   }

   @Override
   protected boolean clearCachedValue(boolean checkChanged) {
      if (!this.resolved) {
         return false;
      } else {
         short oldCachedValue = this.cachedValue;
         this.resolved = false;
         return checkChanged && oldCachedValue != this.get();
      }
   }
}
