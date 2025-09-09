package mekanism.common.integration.jsonthings.parser;

import dev.gigaherz.jsonthings.things.parsers.ThingParseException;
import dev.gigaherz.jsonthings.util.parse.value.ObjValue;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasBuilder;
import mekanism.api.chemical.gas.attribute.GasAttributes;
import mekanism.api.math.FloatingLong;
import mekanism.common.integration.LazyGasProvider;
import mekanism.common.integration.jsonthings.builder.JsonGasBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class JsonGasParser extends SimpleJsonChemicalParser<Gas, GasBuilder, JsonGasBuilder> {
   public JsonGasParser(IEventBus bus) {
      super(bus, ChemicalType.GAS, "Gas", MekanismAPI.GAS_REGISTRY_NAME, JsonGasBuilder::new);
   }

   protected void processAttribute(JsonGasBuilder builder, ObjValue rawAttribute) {
      rawAttribute.ifKey(
            "radioactivity", attribute -> attribute.doubleValue().min(1.0E-5).handle(radioactivity -> builder.with(new GasAttributes.Radiation(radioactivity)))
         )
         .ifKey(
            "coolant",
            attribute -> {
               ObjValue coolant = attribute.obj();
               boolean hasCooledGas = coolant.hasKey("cooled_gas");
               boolean hasHeatedGas = coolant.hasKey("heated_gas");
               if (hasCooledGas == hasHeatedGas) {
                  if (hasCooledGas) {
                     throw new ThingParseException("Coolants cannot declare both a cooled and heated gas");
                  } else {
                     throw new ThingParseException("Coolants must have either a 'cooled_gas' or a 'heated_gas'");
                  }
               } else {
                  JsonGasParser.CoolantData coolantData = new JsonGasParser.CoolantData();
                  coolant.key("thermal_enthalpy", thermalEnthalpy -> thermalEnthalpy.doubleValue().handle(enthalpy -> coolantData.thermalEnthalpy = enthalpy))
                     .key("conductivity", conductivity -> conductivity.doubleValue().handle(c -> coolantData.conductivity = c))
                     .key(hasCooledGas ? "cooled_gas" : "heated_gas", gas -> gas.string().map(ResourceLocation::new).handle(g -> coolantData.gas = g));
                  if (hasCooledGas) {
                     builder.with(new GasAttributes.HeatedCoolant(new LazyGasProvider(coolantData.gas), coolantData.thermalEnthalpy, coolantData.conductivity));
                  } else {
                     builder.with(new GasAttributes.CooledCoolant(new LazyGasProvider(coolantData.gas), coolantData.thermalEnthalpy, coolantData.conductivity));
                  }
               }
            }
         )
         .ifKey(
            "fuel",
            attribute -> {
               JsonGasParser.FuelData fuelData = new JsonGasParser.FuelData();
               attribute.obj()
                  .key("burn_ticks", burnTicks -> burnTicks.intValue().min(1).handle(ticks -> fuelData.burnTicks = ticks))
                  .key(
                     "energy_density",
                     energyDensity -> energyDensity.ifString(
                           string -> string.map(density -> FloatingLong.parseFloatingLong(density, true)).handle(fuelData::setEnergyDensity)
                        )
                        .ifLong(l -> l.min(1L).map(FloatingLong::createConst).handle(fuelData::setEnergyDensity))
                        .ifDouble(d -> d.min(1.0E-4).map(FloatingLong::createConst).handle(fuelData::setEnergyDensity))
                        .typeError()
                  );
               builder.with(new GasAttributes.Fuel(fuelData.burnTicks, fuelData.energyDensity));
            }
         );
   }

   private static class CoolantData {
      @Nullable
      private ResourceLocation gas;
      private double thermalEnthalpy;
      private double conductivity;
   }

   private static class FuelData {
      private FloatingLong energyDensity = FloatingLong.ZERO;
      private int burnTicks;

      private void setEnergyDensity(FloatingLong energyDensity) {
         this.energyDensity = energyDensity;
      }
   }
}
