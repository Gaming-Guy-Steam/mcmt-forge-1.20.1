package mekanism.api.lasers;

import mekanism.api.math.FloatingLong;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import org.jetbrains.annotations.NotNull;

@AutoRegisterCapability
public interface ILaserReceptor {
   void receiveLaserEnergy(@NotNull FloatingLong var1);

   boolean canLasersDig();
}
