package mekanism.api;

import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@AutoRegisterCapability
public interface IEvaporationSolar {
   boolean canSeeSun();
}
