package mekanism.common.tile.machine;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.WrongMethodTypeException;
import mekanism.api.chemical.ChemicalStack;
import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.MethodFactory;
import net.minecraft.world.item.ItemStack;

@MethodFactory(
   target = TileEntitySolarNeutronActivator.class
)
public class TileEntitySolarNeutronActivator$ComputerHandler extends ComputerMethodFactory<TileEntitySolarNeutronActivator> {
   private static MethodHandle fieldGetter$peakProductionRate = getGetterHandle(TileEntitySolarNeutronActivator.class, "peakProductionRate");
   private static MethodHandle fieldGetter$productionRate = getGetterHandle(TileEntitySolarNeutronActivator.class, "productionRate");

   public TileEntitySolarNeutronActivator$ComputerHandler() {
      this.register(
         MethodData.builder("getInput", TileEntitySolarNeutronActivator$ComputerHandler::inputTank$getInput)
            .returnType(ChemicalStack.class)
            .methodDescription("Get the contents of the input tank.")
      );
      this.register(
         MethodData.builder("getInputCapacity", TileEntitySolarNeutronActivator$ComputerHandler::inputTank$getInputCapacity)
            .returnType(long.class)
            .methodDescription("Get the capacity of the input tank.")
      );
      this.register(
         MethodData.builder("getInputNeeded", TileEntitySolarNeutronActivator$ComputerHandler::inputTank$getInputNeeded)
            .returnType(long.class)
            .methodDescription("Get the amount needed to fill the input tank.")
      );
      this.register(
         MethodData.builder("getInputFilledPercentage", TileEntitySolarNeutronActivator$ComputerHandler::inputTank$getInputFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the input tank.")
      );
      this.register(
         MethodData.builder("getOutput", TileEntitySolarNeutronActivator$ComputerHandler::outputTank$getOutput)
            .returnType(ChemicalStack.class)
            .methodDescription("Get the contents of the output tank.")
      );
      this.register(
         MethodData.builder("getOutputCapacity", TileEntitySolarNeutronActivator$ComputerHandler::outputTank$getOutputCapacity)
            .returnType(long.class)
            .methodDescription("Get the capacity of the output tank.")
      );
      this.register(
         MethodData.builder("getOutputNeeded", TileEntitySolarNeutronActivator$ComputerHandler::outputTank$getOutputNeeded)
            .returnType(long.class)
            .methodDescription("Get the amount needed to fill the output tank.")
      );
      this.register(
         MethodData.builder("getOutputFilledPercentage", TileEntitySolarNeutronActivator$ComputerHandler::outputTank$getOutputFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the output tank.")
      );
      this.register(
         MethodData.builder("getPeakProductionRate", TileEntitySolarNeutronActivator$ComputerHandler::getPeakProductionRate_0).returnType(float.class)
      );
      this.register(MethodData.builder("getProductionRate", TileEntitySolarNeutronActivator$ComputerHandler::getProductionRate_0).returnType(float.class));
      this.register(
         MethodData.builder("getInputItem", TileEntitySolarNeutronActivator$ComputerHandler::inputSlot$getInputItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the input slot.")
      );
      this.register(
         MethodData.builder("getOutputItem", TileEntitySolarNeutronActivator$ComputerHandler::outputSlot$getOutputItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the output slot.")
      );
      this.register(MethodData.builder("canSeeSun", TileEntitySolarNeutronActivator$ComputerHandler::canSeeSun_0).returnType(boolean.class));
   }

   public static Object inputTank$getInput(TileEntitySolarNeutronActivator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getStack(subject.inputTank));
   }

   public static Object inputTank$getInputCapacity(TileEntitySolarNeutronActivator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getCapacity(subject.inputTank));
   }

   public static Object inputTank$getInputNeeded(TileEntitySolarNeutronActivator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getNeeded(subject.inputTank));
   }

   public static Object inputTank$getInputFilledPercentage(TileEntitySolarNeutronActivator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getFilledPercentage(subject.inputTank));
   }

   public static Object outputTank$getOutput(TileEntitySolarNeutronActivator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getStack(subject.outputTank));
   }

   public static Object outputTank$getOutputCapacity(TileEntitySolarNeutronActivator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getCapacity(subject.outputTank));
   }

   public static Object outputTank$getOutputNeeded(TileEntitySolarNeutronActivator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getNeeded(subject.outputTank));
   }

   public static Object outputTank$getOutputFilledPercentage(TileEntitySolarNeutronActivator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getFilledPercentage(subject.outputTank));
   }

   private static float getter$peakProductionRate(TileEntitySolarNeutronActivator subject) {
      try {
         return (float)fieldGetter$peakProductionRate.invokeExact((TileEntitySolarNeutronActivator)subject);
      } catch (WrongMethodTypeException var2) {
         throw new RuntimeException("Getter not bound correctly", var2);
      } catch (Throwable var3) {
         throw new RuntimeException(var3.getMessage(), var3);
      }
   }

   public static Object getPeakProductionRate_0(TileEntitySolarNeutronActivator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert((double)getter$peakProductionRate(subject));
   }

   private static float getter$productionRate(TileEntitySolarNeutronActivator subject) {
      try {
         return (float)fieldGetter$productionRate.invokeExact((TileEntitySolarNeutronActivator)subject);
      } catch (WrongMethodTypeException var2) {
         throw new RuntimeException("Getter not bound correctly", var2);
      } catch (Throwable var3) {
         throw new RuntimeException(var3.getMessage(), var3);
      }
   }

   public static Object getProductionRate_0(TileEntitySolarNeutronActivator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert((double)getter$productionRate(subject));
   }

   public static Object inputSlot$getInputItem(TileEntitySolarNeutronActivator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.inputSlot));
   }

   public static Object outputSlot$getOutputItem(TileEntitySolarNeutronActivator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.outputSlot));
   }

   public static Object canSeeSun_0(TileEntitySolarNeutronActivator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.canSeeSun());
   }
}
