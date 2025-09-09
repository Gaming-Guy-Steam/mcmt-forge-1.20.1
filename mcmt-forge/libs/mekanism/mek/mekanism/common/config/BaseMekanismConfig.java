package mekanism.common.config;

import java.util.ArrayList;
import java.util.List;
import mekanism.common.config.value.CachedValue;

public abstract class BaseMekanismConfig implements IMekanismConfig {
   private final List<CachedValue<?>> cachedConfigValues = new ArrayList<>();

   @Override
   public void clearCache(boolean unloading) {
      for (CachedValue<?> cachedConfigValue : this.cachedConfigValues) {
         cachedConfigValue.clearCache(unloading);
      }
   }

   @Override
   public void addCachedValue(CachedValue<?> configValue) {
      this.cachedConfigValues.add(configValue);
   }
}
