package mekanism.common.config.listener;

import java.util.function.Supplier;
import mekanism.common.config.value.CachedValue;
import net.minecraftforge.common.util.NonNullSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConfigBasedCachedSupplier<VALUE> implements NonNullSupplier<VALUE>, Supplier<VALUE> {
   private final NonNullSupplier<VALUE> resolver;
   @Nullable
   private VALUE cachedValue;

   public ConfigBasedCachedSupplier(NonNullSupplier<VALUE> resolver, CachedValue<?>... dependantConfigValues) {
      this.resolver = resolver;
      CachedValue.IConfigValueInvalidationListener refreshListener = this::refresh;

      for (CachedValue<?> configValue : dependantConfigValues) {
         configValue.addInvalidationListener(refreshListener);
      }
   }

   protected final void refresh() {
      this.cachedValue = (VALUE)this.resolver.get();
   }

   @NotNull
   @Override
   public VALUE get() {
      if (this.cachedValue == null) {
         this.refresh();
      }

      return this.cachedValue;
   }
}
