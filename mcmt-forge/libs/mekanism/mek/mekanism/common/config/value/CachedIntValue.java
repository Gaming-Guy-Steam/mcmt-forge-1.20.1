package mekanism.common.config.value;

import java.util.function.IntSupplier;
import mekanism.common.config.IMekanismConfig;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class CachedIntValue extends CachedValue<Integer> implements IntSupplier {
   private boolean resolved;
   private int cachedValue;

   private CachedIntValue(IMekanismConfig config, ConfigValue<Integer> internal) {
      super(config, internal);
   }

   public static CachedIntValue wrap(IMekanismConfig config, ConfigValue<Integer> internal) {
      return new CachedIntValue(config, internal);
   }

   public int getOrDefault() {
      return !this.resolved && !this.isLoaded() ? (Integer)this.internal.getDefault() : this.get();
   }

   public int get() {
      if (!this.resolved) {
         this.cachedValue = (Integer)this.internal.get();
         this.resolved = true;
      }

      return this.cachedValue;
   }

   @Override
   public int getAsInt() {
      return this.get();
   }

   public void set(int value) {
      this.internal.set(value);
      this.cachedValue = value;
   }

   @Override
   protected boolean clearCachedValue(boolean checkChanged) {
      if (!this.resolved) {
         return false;
      } else {
         int oldCachedValue = this.cachedValue;
         this.resolved = false;
         return checkChanged && oldCachedValue != this.get();
      }
   }
}
