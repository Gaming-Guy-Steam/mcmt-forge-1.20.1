package mekanism.common.registries;

import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasBuilder;
import mekanism.api.chemical.gas.attribute.GasAttributes;
import mekanism.common.ChemicalConstants;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registration.impl.GasDeferredRegister;
import mekanism.common.registration.impl.GasRegistryObject;

public class MekanismGases {
   public static final GasDeferredRegister GASES = new GasDeferredRegister("mekanism");
   public static final GasRegistryObject<Gas> HYDROGEN = GASES.register(
      ChemicalConstants.HYDROGEN, new GasAttributes.Fuel(() -> 1, MekanismConfig.general.FROM_H2)
   );
   public static final GasRegistryObject<Gas> OXYGEN = GASES.register(ChemicalConstants.OXYGEN);
   public static final GasRegistryObject<Gas> STEAM = GASES.register("steam", () -> new Gas(GasBuilder.builder(Mekanism.rl("liquid/steam"))));
   public static final GasRegistryObject<Gas> WATER_VAPOR = GASES.register("water_vapor", () -> new Gas(GasBuilder.builder(Mekanism.rl("liquid/steam"))));
   public static final GasRegistryObject<Gas> CHLORINE = GASES.register(ChemicalConstants.CHLORINE);
   public static final GasRegistryObject<Gas> SULFUR_DIOXIDE = GASES.register(ChemicalConstants.SULFUR_DIOXIDE);
   public static final GasRegistryObject<Gas> SULFUR_TRIOXIDE = GASES.register(ChemicalConstants.SULFUR_TRIOXIDE);
   public static final GasRegistryObject<Gas> SULFURIC_ACID = GASES.register(ChemicalConstants.SULFURIC_ACID);
   public static final GasRegistryObject<Gas> HYDROGEN_CHLORIDE = GASES.register(ChemicalConstants.HYDROGEN_CHLORIDE);
   public static final GasRegistryObject<Gas> HYDROFLUORIC_ACID = GASES.register(ChemicalConstants.HYDROFLUORIC_ACID);
   public static final GasRegistryObject<Gas> URANIUM_OXIDE = GASES.register(ChemicalConstants.URANIUM_OXIDE);
   public static final GasRegistryObject<Gas> URANIUM_HEXAFLUORIDE = GASES.register(ChemicalConstants.URANIUM_HEXAFLUORIDE);
   public static final GasRegistryObject<Gas> ETHENE = GASES.register(ChemicalConstants.ETHENE);
   public static final GasRegistryObject<Gas> SODIUM = GASES.register(ChemicalConstants.SODIUM, MekanismGases.Coolants.SODIUM_COOLANT);
   public static final GasRegistryObject<Gas> SUPERHEATED_SODIUM = GASES.register(
      ChemicalConstants.SUPERHEATED_SODIUM, MekanismGases.Coolants.HEATED_SODIUM_COOLANT
   );
   public static final GasRegistryObject<Gas> BRINE = GASES.register("brine", 16707484);
   public static final GasRegistryObject<Gas> LITHIUM = GASES.register(ChemicalConstants.LITHIUM);
   public static final GasRegistryObject<Gas> OSMIUM = GASES.register("osmium", 5422538);
   public static final GasRegistryObject<Gas> FISSILE_FUEL = GASES.register("fissile_fuel", 3027759);
   public static final GasRegistryObject<Gas> NUCLEAR_WASTE = GASES.register("nuclear_waste", 5194026, new GasAttributes.Radiation(0.01));
   public static final GasRegistryObject<Gas> SPENT_NUCLEAR_WASTE = GASES.register("spent_nuclear_waste", 2498581, new GasAttributes.Radiation(0.01));
   public static final GasRegistryObject<Gas> PLUTONIUM = GASES.register("plutonium", 2068892, new GasAttributes.Radiation(0.02));
   public static final GasRegistryObject<Gas> POLONIUM = GASES.register("polonium", 1810043, new GasAttributes.Radiation(0.05));
   public static final GasRegistryObject<Gas> ANTIMATTER = GASES.register("antimatter", 10773683);

   private MekanismGases() {
   }

   public static class Coolants {
      public static final GasAttributes.CooledCoolant SODIUM_COOLANT = new GasAttributes.CooledCoolant(() -> MekanismGases.SUPERHEATED_SODIUM.get(), 5.0, 1.0);
      public static final GasAttributes.HeatedCoolant HEATED_SODIUM_COOLANT = new GasAttributes.HeatedCoolant(() -> MekanismGases.SODIUM.get(), 5.0, 1.0);

      private Coolants() {
      }
   }
}
