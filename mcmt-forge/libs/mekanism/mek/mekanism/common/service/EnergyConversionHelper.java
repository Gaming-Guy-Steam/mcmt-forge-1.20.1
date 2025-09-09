package mekanism.common.service;

import mekanism.api.energy.IEnergyConversion;
import mekanism.api.energy.IEnergyConversionHelper;
import mekanism.common.util.UnitDisplayUtils;

public class EnergyConversionHelper implements IEnergyConversionHelper {
   @Override
   public IEnergyConversion jouleConversion() {
      return UnitDisplayUtils.EnergyUnit.JOULES;
   }

   @Override
   public IEnergyConversion feConversion() {
      return UnitDisplayUtils.EnergyUnit.FORGE_ENERGY;
   }

   @Override
   public IEnergyConversion euConversion() {
      return UnitDisplayUtils.EnergyUnit.ELECTRICAL_UNITS;
   }
}
