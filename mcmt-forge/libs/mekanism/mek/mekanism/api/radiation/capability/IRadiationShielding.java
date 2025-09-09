package mekanism.api.radiation.capability;

import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@AutoRegisterCapability
public interface IRadiationShielding {
   double getRadiationShielding();
}
