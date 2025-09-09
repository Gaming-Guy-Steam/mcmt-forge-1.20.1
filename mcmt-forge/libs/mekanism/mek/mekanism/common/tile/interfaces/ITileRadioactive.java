package mekanism.common.tile.interfaces;

import java.util.List;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.attribute.GasAttributes;
import mekanism.api.math.MathUtils;
import mekanism.api.radiation.IRadiationManager;

public interface ITileRadioactive {
   static float calculateRadiationScale(List<IGasTank> tanks) {
      if (IRadiationManager.INSTANCE.isRadiationEnabled() && !tanks.isEmpty()) {
         float summedScale = 0.0F;

         for (IGasTank tank : tanks) {
            if (!tank.isEmpty() && tank.getStack().has(GasAttributes.Radiation.class)) {
               summedScale += (float)tank.getStored() / (float)tank.getCapacity();
            }
         }

         return summedScale / tanks.size();
      } else {
         return 0.0F;
      }
   }

   float getRadiationScale();

   default int getRadiationParticleCount() {
      return MathUtils.clampToInt((double)(10.0F * this.getRadiationScale()));
   }
}
