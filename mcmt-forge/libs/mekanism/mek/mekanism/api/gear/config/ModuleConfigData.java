package mekanism.api.gear.config;

import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.nbt.CompoundTag;

@NothingNullByDefault
public interface ModuleConfigData<TYPE> {
   TYPE get();

   void set(TYPE var1);

   void read(String var1, CompoundTag var2);

   void write(String var1, CompoundTag var2);
}
