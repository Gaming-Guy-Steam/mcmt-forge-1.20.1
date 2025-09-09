package mekanism.common.config.value;

import java.util.function.Supplier;
import mekanism.common.config.IMekanismConfig;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CachedResolvableConfigValue<TYPE, REAL> extends CachedValue<REAL> implements Supplier<TYPE> {
   @Nullable
   private TYPE cachedValue;

   protected CachedResolvableConfigValue(IMekanismConfig config, ConfigValue<REAL> internal) {
      super(config, internal);
   }

   protected abstract TYPE resolve(REAL encoded);

   protected abstract REAL encode(TYPE value);

   @NotNull
   public TYPE getOrDefault() {
      return this.cachedValue == null && !this.isLoaded() ? this.resolve((REAL)this.internal.getDefault()) : this.get();
   }

   @NotNull
   @Override
   public TYPE get() {
      if (this.cachedValue == null) {
         this.cachedValue = this.resolve((REAL)this.internal.get());
      }

      return this.cachedValue;
   }

   public void set(TYPE value) {
      this.internal.set(this.encode(value));
      this.cachedValue = value;
   }

   @Override
   protected boolean clearCachedValue(boolean checkChanged) {
      if (this.cachedValue == null) {
         return false;
      } else {
         TYPE oldCachedValue = this.cachedValue;
         this.cachedValue = null;
         return checkChanged && !oldCachedValue.equals(this.get());
      }
   }
}
