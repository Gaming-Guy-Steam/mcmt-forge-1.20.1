package mekanism.common.integration.computer;

import java.util.Locale;
import mekanism.api.math.FloatingLong;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.util.UnitDisplayUtils;

public class ComputerEnergyHelper {
   @ComputerMethod(
      methodDescription = "Convert Mekanism Joules to Forge Energy"
   )
   public static FloatingLong joulesToFE(FloatingLong joules) throws ComputerException {
      return convert(UnitDisplayUtils.EnergyUnit.FORGE_ENERGY, joules, true);
   }

   @ComputerMethod(
      methodDescription = "Convert Forge Energy to Mekanism Joules"
   )
   public static FloatingLong feToJoules(FloatingLong fe) throws ComputerException {
      return convert(UnitDisplayUtils.EnergyUnit.FORGE_ENERGY, fe, false);
   }

   @ComputerMethod(
      requiredMods = {"ic2"},
      methodDescription = "Convert Mekanism Joules to IC2 Energy Units"
   )
   public static FloatingLong joulesToEU(FloatingLong joules) throws ComputerException {
      return convert(UnitDisplayUtils.EnergyUnit.ELECTRICAL_UNITS, joules, true);
   }

   @ComputerMethod(
      requiredMods = {"ic2"},
      methodDescription = "Convert IC2 Energy Units to Mekanism Joules"
   )
   public static FloatingLong euToJoules(FloatingLong eu) throws ComputerException {
      return convert(UnitDisplayUtils.EnergyUnit.ELECTRICAL_UNITS, eu, false);
   }

   private static FloatingLong convert(UnitDisplayUtils.EnergyUnit type, FloatingLong energy, boolean to) throws ComputerException {
      if (type.isEnabled()) {
         return to ? type.convertTo(energy) : type.convertFrom(energy);
      } else {
         String name = type.name().replace('_', ' ').toLowerCase(Locale.ROOT);
         String between = to ? "Joules and " + name : name + " and Joules";
         throw new ComputerException("Conversion between " + between + " is disabled in Mekanism's config or missing required mods.");
      }
   }
}
