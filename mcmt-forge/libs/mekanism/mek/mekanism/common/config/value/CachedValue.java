package mekanism.common.config.value;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import mekanism.common.Mekanism;
import mekanism.common.config.IMekanismConfig;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public abstract class CachedValue<T> {
   private final IMekanismConfig config;
   protected final ConfigValue<T> internal;
   private Set<CachedValue.IConfigValueInvalidationListener> invalidationListeners;

   protected CachedValue(IMekanismConfig config, ConfigValue<T> internal) {
      this.config = config;
      this.internal = internal;
      this.config.addCachedValue(this);
   }

   public boolean hasInvalidationListeners() {
      return this.invalidationListeners != null && !this.invalidationListeners.isEmpty();
   }

   public void addInvalidationListener(CachedValue.IConfigValueInvalidationListener listener) {
      if (this.invalidationListeners == null) {
         this.invalidationListeners = new HashSet<>();
      }

      if (!this.invalidationListeners.add(listener)) {
         Mekanism.logger.warn("Duplicate invalidation listener added");
      }
   }

   public void removeInvalidationListener(CachedValue.IConfigValueInvalidationListener listener) {
      if (this.invalidationListeners == null) {
         Mekanism.logger.warn("Unable to remove specified invalidation listener, no invalidation listeners have been added.");
      } else if (!this.invalidationListeners.remove(listener)) {
         Mekanism.logger.warn("Unable to remove specified invalidation listener.");
      }
   }

   public boolean removeInvalidationListenersMatching(Predicate<CachedValue.IConfigValueInvalidationListener> checker) {
      return this.invalidationListeners != null && !this.invalidationListeners.isEmpty() && this.invalidationListeners.removeIf(checker);
   }

   protected abstract boolean clearCachedValue(boolean checkChanged);

   public final void clearCache(boolean unloading) {
      if (this.hasInvalidationListeners()) {
         if (!unloading && this.isLoaded() && this.clearCachedValue(true)) {
            this.invalidationListeners.forEach(Runnable::run);
         }
      } else {
         this.clearCachedValue(false);
      }
   }

   protected boolean isLoaded() {
      return this.config.isLoaded();
   }

   @FunctionalInterface
   public interface IConfigValueInvalidationListener extends Runnable {
   }
}
