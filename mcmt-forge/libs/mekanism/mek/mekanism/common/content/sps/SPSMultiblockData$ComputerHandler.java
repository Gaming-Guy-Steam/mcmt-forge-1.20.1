package mekanism.common.content.sps;

import mekanism.api.chemical.ChemicalStack;
import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.MethodFactory;

@MethodFactory(
   target = SPSMultiblockData.class
)
public class SPSMultiblockData$ComputerHandler extends ComputerMethodFactory<SPSMultiblockData> {
   public SPSMultiblockData$ComputerHandler() {
      this.register(
         MethodData.builder("getInput", SPSMultiblockData$ComputerHandler::inputTank$getInput)
            .returnType(ChemicalStack.class)
            .methodDescription("Get the contents of the input tank.")
      );
      this.register(
         MethodData.builder("getInputCapacity", SPSMultiblockData$ComputerHandler::inputTank$getInputCapacity)
            .returnType(long.class)
            .methodDescription("Get the capacity of the input tank.")
      );
      this.register(
         MethodData.builder("getInputNeeded", SPSMultiblockData$ComputerHandler::inputTank$getInputNeeded)
            .returnType(long.class)
            .methodDescription("Get the amount needed to fill the input tank.")
      );
      this.register(
         MethodData.builder("getInputFilledPercentage", SPSMultiblockData$ComputerHandler::inputTank$getInputFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the input tank.")
      );
      this.register(
         MethodData.builder("getOutput", SPSMultiblockData$ComputerHandler::outputTank$getOutput)
            .returnType(ChemicalStack.class)
            .methodDescription("Get the contents of the output tank.")
      );
      this.register(
         MethodData.builder("getOutputCapacity", SPSMultiblockData$ComputerHandler::outputTank$getOutputCapacity)
            .returnType(long.class)
            .methodDescription("Get the capacity of the output tank.")
      );
      this.register(
         MethodData.builder("getOutputNeeded", SPSMultiblockData$ComputerHandler::outputTank$getOutputNeeded)
            .returnType(long.class)
            .methodDescription("Get the amount needed to fill the output tank.")
      );
      this.register(
         MethodData.builder("getOutputFilledPercentage", SPSMultiblockData$ComputerHandler::outputTank$getOutputFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the output tank.")
      );
      this.register(MethodData.builder("getProcessRate", SPSMultiblockData$ComputerHandler::getProcessRate_0).returnType(double.class));
      this.register(MethodData.builder("getCoils", SPSMultiblockData$ComputerHandler::getCoils_0).returnType(int.class));
   }

   public static Object inputTank$getInput(SPSMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getStack(subject.inputTank));
   }

   public static Object inputTank$getInputCapacity(SPSMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getCapacity(subject.inputTank));
   }

   public static Object inputTank$getInputNeeded(SPSMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getNeeded(subject.inputTank));
   }

   public static Object inputTank$getInputFilledPercentage(SPSMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getFilledPercentage(subject.inputTank));
   }

   public static Object outputTank$getOutput(SPSMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getStack(subject.outputTank));
   }

   public static Object outputTank$getOutputCapacity(SPSMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getCapacity(subject.outputTank));
   }

   public static Object outputTank$getOutputNeeded(SPSMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getNeeded(subject.outputTank));
   }

   public static Object outputTank$getOutputFilledPercentage(SPSMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getFilledPercentage(subject.outputTank));
   }

   public static Object getProcessRate_0(SPSMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getProcessRate());
   }

   public static Object getCoils_0(SPSMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getCoils());
   }
}
