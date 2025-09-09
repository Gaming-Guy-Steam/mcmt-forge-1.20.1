package mekanism.api.radiation;

import mekanism.api.Coord4D;
import org.jetbrains.annotations.NotNull;

public interface IRadiationSource {
   @NotNull
   Coord4D getPos();

   double getMagnitude();

   void radiate(double var1);

   boolean decay();
}
