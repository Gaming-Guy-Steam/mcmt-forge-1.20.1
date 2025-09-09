package mekanism.common.config;

import mekanism.common.config.value.CachedValue;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig.Type;

public interface IMekanismConfig {
   String getFileName();

   ForgeConfigSpec getConfigSpec();

   default boolean isLoaded() {
      return this.getConfigSpec().isLoaded();
   }

   Type getConfigType();

   default void save() {
      this.getConfigSpec().save();
   }

   void clearCache(boolean unloading);

   void addCachedValue(CachedValue<?> configValue);

   default boolean addToContainer() {
      return true;
   }
}
