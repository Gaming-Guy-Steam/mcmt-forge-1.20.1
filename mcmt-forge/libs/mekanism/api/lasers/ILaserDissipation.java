package mekanism.api.lasers;

import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@AutoRegisterCapability
public interface ILaserDissipation {
   double getDissipationPercent();

   double getRefractionPercent();
}
