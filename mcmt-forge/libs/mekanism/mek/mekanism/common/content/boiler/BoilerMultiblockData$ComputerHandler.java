package mekanism.common.content.boiler;

import mekanism.api.chemical.ChemicalStack;
import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.MethodFactory;
import net.minecraftforge.fluids.FluidStack;

@MethodFactory(
   target = BoilerMultiblockData.class
)
public class BoilerMultiblockData$ComputerHandler extends ComputerMethodFactory<BoilerMultiblockData> {
   public BoilerMultiblockData$ComputerHandler() {
      this.register(
         MethodData.builder("getHeatedCoolant", BoilerMultiblockData$ComputerHandler::superheatedCoolantTank$getHeatedCoolant)
            .returnType(ChemicalStack.class)
            .methodDescription("Get the contents of the heated coolant tank.")
      );
      this.register(
         MethodData.builder("getHeatedCoolantCapacity", BoilerMultiblockData$ComputerHandler::superheatedCoolantTank$getHeatedCoolantCapacity)
            .returnType(long.class)
            .methodDescription("Get the capacity of the heated coolant tank.")
      );
      this.register(
         MethodData.builder("getHeatedCoolantNeeded", BoilerMultiblockData$ComputerHandler::superheatedCoolantTank$getHeatedCoolantNeeded)
            .returnType(long.class)
            .methodDescription("Get the amount needed to fill the heated coolant tank.")
      );
      this.register(
         MethodData.builder("getHeatedCoolantFilledPercentage", BoilerMultiblockData$ComputerHandler::superheatedCoolantTank$getHeatedCoolantFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the heated coolant tank.")
      );
      this.register(
         MethodData.builder("getCooledCoolant", BoilerMultiblockData$ComputerHandler::cooledCoolantTank$getCooledCoolant)
            .returnType(ChemicalStack.class)
            .methodDescription("Get the contents of the cooled coolant tank.")
      );
      this.register(
         MethodData.builder("getCooledCoolantCapacity", BoilerMultiblockData$ComputerHandler::cooledCoolantTank$getCooledCoolantCapacity)
            .returnType(long.class)
            .methodDescription("Get the capacity of the cooled coolant tank.")
      );
      this.register(
         MethodData.builder("getCooledCoolantNeeded", BoilerMultiblockData$ComputerHandler::cooledCoolantTank$getCooledCoolantNeeded)
            .returnType(long.class)
            .methodDescription("Get the amount needed to fill the cooled coolant tank.")
      );
      this.register(
         MethodData.builder("getCooledCoolantFilledPercentage", BoilerMultiblockData$ComputerHandler::cooledCoolantTank$getCooledCoolantFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the cooled coolant tank.")
      );
      this.register(
         MethodData.builder("getWater", BoilerMultiblockData$ComputerHandler::waterTank$getWater)
            .returnType(FluidStack.class)
            .methodDescription("Get the contents of the water tank.")
      );
      this.register(
         MethodData.builder("getWaterCapacity", BoilerMultiblockData$ComputerHandler::waterTank$getWaterCapacity)
            .returnType(int.class)
            .methodDescription("Get the capacity of the water tank.")
      );
      this.register(
         MethodData.builder("getWaterNeeded", BoilerMultiblockData$ComputerHandler::waterTank$getWaterNeeded)
            .returnType(int.class)
            .methodDescription("Get the amount needed to fill the water tank.")
      );
      this.register(
         MethodData.builder("getWaterFilledPercentage", BoilerMultiblockData$ComputerHandler::waterTank$getWaterFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the water tank.")
      );
      this.register(
         MethodData.builder("getSteam", BoilerMultiblockData$ComputerHandler::steamTank$getSteam)
            .returnType(ChemicalStack.class)
            .methodDescription("Get the contents of the steam tank.")
      );
      this.register(
         MethodData.builder("getSteamCapacity", BoilerMultiblockData$ComputerHandler::steamTank$getSteamCapacity)
            .returnType(long.class)
            .methodDescription("Get the capacity of the steam tank.")
      );
      this.register(
         MethodData.builder("getSteamNeeded", BoilerMultiblockData$ComputerHandler::steamTank$getSteamNeeded)
            .returnType(long.class)
            .methodDescription("Get the amount needed to fill the steam tank.")
      );
      this.register(
         MethodData.builder("getSteamFilledPercentage", BoilerMultiblockData$ComputerHandler::steamTank$getSteamFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the steam tank.")
      );
      this.register(
         MethodData.builder("getTemperature", BoilerMultiblockData$ComputerHandler::heatCapacitor$getTemperature)
            .returnType(double.class)
            .methodDescription("Get the temperature of the boiler in Kelvin.")
      );
      this.register(
         MethodData.builder("getEnvironmentalLoss", BoilerMultiblockData$ComputerHandler::getEnvironmentalLoss_0)
            .returnType(double.class)
            .methodDescription("Get the amount of heat lost to the environment in the last tick (Kelvin)")
      );
      this.register(
         MethodData.builder("getBoilRate", BoilerMultiblockData$ComputerHandler::getBoilRate_0)
            .returnType(int.class)
            .methodDescription("Get the rate of boiling (mB/t)")
      );
      this.register(
         MethodData.builder("getMaxBoilRate", BoilerMultiblockData$ComputerHandler::getMaxBoilRate_0)
            .returnType(int.class)
            .methodDescription("Get the maximum rate of boiling seen (mB/t)")
      );
      this.register(
         MethodData.builder("getSuperheaters", BoilerMultiblockData$ComputerHandler::getSuperheaters_0)
            .returnType(int.class)
            .methodDescription("How many superheaters this Boiler has")
      );
      this.register(
         MethodData.builder("getBoilCapacity", BoilerMultiblockData$ComputerHandler::getBoilCapacity_0)
            .returnType(long.class)
            .methodDescription("Get the maximum possible boil rate for this Boiler, based on the number of Superheating Elements")
      );
   }

   public static Object superheatedCoolantTank$getHeatedCoolant(BoilerMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getStack(subject.superheatedCoolantTank));
   }

   public static Object superheatedCoolantTank$getHeatedCoolantCapacity(BoilerMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getCapacity(subject.superheatedCoolantTank));
   }

   public static Object superheatedCoolantTank$getHeatedCoolantNeeded(BoilerMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getNeeded(subject.superheatedCoolantTank));
   }

   public static Object superheatedCoolantTank$getHeatedCoolantFilledPercentage(BoilerMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getFilledPercentage(subject.superheatedCoolantTank));
   }

   public static Object cooledCoolantTank$getCooledCoolant(BoilerMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getStack(subject.cooledCoolantTank));
   }

   public static Object cooledCoolantTank$getCooledCoolantCapacity(BoilerMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getCapacity(subject.cooledCoolantTank));
   }

   public static Object cooledCoolantTank$getCooledCoolantNeeded(BoilerMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getNeeded(subject.cooledCoolantTank));
   }

   public static Object cooledCoolantTank$getCooledCoolantFilledPercentage(BoilerMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getFilledPercentage(subject.cooledCoolantTank));
   }

   public static Object waterTank$getWater(BoilerMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerFluidTankWrapper.getStack(subject.waterTank));
   }

   public static Object waterTank$getWaterCapacity(BoilerMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerFluidTankWrapper.getCapacity(subject.waterTank));
   }

   public static Object waterTank$getWaterNeeded(BoilerMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerFluidTankWrapper.getNeeded(subject.waterTank));
   }

   public static Object waterTank$getWaterFilledPercentage(BoilerMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerFluidTankWrapper.getFilledPercentage(subject.waterTank));
   }

   public static Object steamTank$getSteam(BoilerMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getStack(subject.steamTank));
   }

   public static Object steamTank$getSteamCapacity(BoilerMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getCapacity(subject.steamTank));
   }

   public static Object steamTank$getSteamNeeded(BoilerMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getNeeded(subject.steamTank));
   }

   public static Object steamTank$getSteamFilledPercentage(BoilerMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getFilledPercentage(subject.steamTank));
   }

   public static Object heatCapacitor$getTemperature(BoilerMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerHeatCapacitorWrapper.getTemperature(subject.heatCapacitor));
   }

   public static Object getEnvironmentalLoss_0(BoilerMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.lastEnvironmentLoss);
   }

   public static Object getBoilRate_0(BoilerMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.lastBoilRate);
   }

   public static Object getMaxBoilRate_0(BoilerMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.lastMaxBoil);
   }

   public static Object getSuperheaters_0(BoilerMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.superheatingElements);
   }

   public static Object getBoilCapacity_0(BoilerMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getBoilCapacity());
   }
}
