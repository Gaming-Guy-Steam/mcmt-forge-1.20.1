package mekanism.api.gear.config;

import org.jetbrains.annotations.NotNull;

public interface IModuleConfigItem<TYPE> {
   @NotNull
   String getName();

   @NotNull
   TYPE get();

   void set(@NotNull TYPE var1);
}
