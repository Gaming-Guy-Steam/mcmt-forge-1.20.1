package mekanism.common.content.evaporation;

import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.MethodFactory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

@MethodFactory(
   target = EvaporationMultiblockData.class
)
public class EvaporationMultiblockData$ComputerHandler extends ComputerMethodFactory<EvaporationMultiblockData> {
   public EvaporationMultiblockData$ComputerHandler() {
      this.register(
         MethodData.builder("getInput", EvaporationMultiblockData$ComputerHandler::inputTank$getInput)
            .returnType(FluidStack.class)
            .methodDescription("Get the contents of the input tank.")
      );
      this.register(
         MethodData.builder("getInputCapacity", EvaporationMultiblockData$ComputerHandler::inputTank$getInputCapacity)
            .returnType(int.class)
            .methodDescription("Get the capacity of the input tank.")
      );
      this.register(
         MethodData.builder("getInputNeeded", EvaporationMultiblockData$ComputerHandler::inputTank$getInputNeeded)
            .returnType(int.class)
            .methodDescription("Get the amount needed to fill the input tank.")
      );
      this.register(
         MethodData.builder("getInputFilledPercentage", EvaporationMultiblockData$ComputerHandler::inputTank$getInputFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the input tank.")
      );
      this.register(
         MethodData.builder("getOutput", EvaporationMultiblockData$ComputerHandler::outputTank$getOutput)
            .returnType(FluidStack.class)
            .methodDescription("Get the contents of the output tank.")
      );
      this.register(
         MethodData.builder("getOutputCapacity", EvaporationMultiblockData$ComputerHandler::outputTank$getOutputCapacity)
            .returnType(int.class)
            .methodDescription("Get the capacity of the output tank.")
      );
      this.register(
         MethodData.builder("getOutputNeeded", EvaporationMultiblockData$ComputerHandler::outputTank$getOutputNeeded)
            .returnType(int.class)
            .methodDescription("Get the amount needed to fill the output tank.")
      );
      this.register(
         MethodData.builder("getOutputFilledPercentage", EvaporationMultiblockData$ComputerHandler::outputTank$getOutputFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the output tank.")
      );
      this.register(MethodData.builder("getProductionAmount", EvaporationMultiblockData$ComputerHandler::getProductionAmount_0).returnType(double.class));
      this.register(MethodData.builder("getEnvironmentalLoss", EvaporationMultiblockData$ComputerHandler::getEnvironmentalLoss_0).returnType(double.class));
      this.register(
         MethodData.builder("getInputItemInput", EvaporationMultiblockData$ComputerHandler::inputInputSlot$getInputItemInput)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the input side's input slot.")
      );
      this.register(
         MethodData.builder("getInputItemOutput", EvaporationMultiblockData$ComputerHandler::outputInputSlot$getInputItemOutput)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the input side's output slot.")
      );
      this.register(
         MethodData.builder("getOutputItemInput", EvaporationMultiblockData$ComputerHandler::inputOutputSlot$getOutputItemInput)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the output side's input slot.")
      );
      this.register(
         MethodData.builder("getOutputItemOutput", EvaporationMultiblockData$ComputerHandler::outputOutputSlot$getOutputItemOutput)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the output side's output slot.")
      );
      this.register(MethodData.builder("getTemperature", EvaporationMultiblockData$ComputerHandler::getTemperature_0).returnType(double.class));
      this.register(MethodData.builder("getActiveSolars", EvaporationMultiblockData$ComputerHandler::getActiveSolars_0).returnType(int.class));
   }

   public static Object inputTank$getInput(EvaporationMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerFluidTankWrapper.getStack(subject.inputTank));
   }

   public static Object inputTank$getInputCapacity(EvaporationMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerFluidTankWrapper.getCapacity(subject.inputTank));
   }

   public static Object inputTank$getInputNeeded(EvaporationMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerFluidTankWrapper.getNeeded(subject.inputTank));
   }

   public static Object inputTank$getInputFilledPercentage(EvaporationMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerFluidTankWrapper.getFilledPercentage(subject.inputTank));
   }

   public static Object outputTank$getOutput(EvaporationMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerFluidTankWrapper.getStack(subject.outputTank));
   }

   public static Object outputTank$getOutputCapacity(EvaporationMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerFluidTankWrapper.getCapacity(subject.outputTank));
   }

   public static Object outputTank$getOutputNeeded(EvaporationMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerFluidTankWrapper.getNeeded(subject.outputTank));
   }

   public static Object outputTank$getOutputFilledPercentage(EvaporationMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerFluidTankWrapper.getFilledPercentage(subject.outputTank));
   }

   public static Object getProductionAmount_0(EvaporationMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.lastGain);
   }

   public static Object getEnvironmentalLoss_0(EvaporationMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.lastEnvironmentLoss);
   }

   public static Object inputInputSlot$getInputItemInput(EvaporationMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.inputInputSlot));
   }

   public static Object outputInputSlot$getInputItemOutput(EvaporationMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.outputInputSlot));
   }

   public static Object inputOutputSlot$getOutputItemInput(EvaporationMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.inputOutputSlot));
   }

   public static Object outputOutputSlot$getOutputItemOutput(EvaporationMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.outputOutputSlot));
   }

   public static Object getTemperature_0(EvaporationMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getTemperature());
   }

   public static Object getActiveSolars_0(EvaporationMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getActiveSolars());
   }
}
