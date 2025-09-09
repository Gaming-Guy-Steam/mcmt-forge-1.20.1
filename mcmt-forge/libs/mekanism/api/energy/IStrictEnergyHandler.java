package mekanism.api.energy;

import mekanism.api.Action;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongTransferUtils;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@AutoRegisterCapability
@NothingNullByDefault
public interface IStrictEnergyHandler {
   int getEnergyContainerCount();

   FloatingLong getEnergy(int var1);

   void setEnergy(int var1, FloatingLong var2);

   FloatingLong getMaxEnergy(int var1);

   FloatingLong getNeededEnergy(int var1);

   FloatingLong insertEnergy(int var1, FloatingLong var2, Action var3);

   FloatingLong extractEnergy(int var1, FloatingLong var2, Action var3);

   default FloatingLong insertEnergy(FloatingLong amount, Action action) {
      return FloatingLongTransferUtils.insert(amount, action, this::getEnergyContainerCount, this::getEnergy, this::insertEnergy);
   }

   default FloatingLong extractEnergy(FloatingLong amount, Action action) {
      return FloatingLongTransferUtils.extract(amount, action, this::getEnergyContainerCount, this::extractEnergy);
   }
}
