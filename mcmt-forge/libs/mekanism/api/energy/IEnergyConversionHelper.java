package mekanism.api.energy;

import java.util.ServiceLoader;

public interface IEnergyConversionHelper {
   IEnergyConversionHelper INSTANCE = ServiceLoader.load(IEnergyConversionHelper.class)
      .findFirst()
      .orElseThrow(() -> new IllegalStateException("No valid ServiceImpl for IEnergyConversionHelper found"));

   IEnergyConversion jouleConversion();

   IEnergyConversion feConversion();

   IEnergyConversion euConversion();
}
