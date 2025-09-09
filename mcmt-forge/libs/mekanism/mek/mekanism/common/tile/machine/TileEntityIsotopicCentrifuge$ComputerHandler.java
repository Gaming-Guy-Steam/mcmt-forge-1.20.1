package mekanism.common.tile.machine;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.math.FloatingLong;
import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.MethodFactory;
import net.minecraft.world.item.ItemStack;

@MethodFactory(
   target = TileEntityIsotopicCentrifuge.class
)
public class TileEntityIsotopicCentrifuge$ComputerHandler extends ComputerMethodFactory<TileEntityIsotopicCentrifuge> {
   public TileEntityIsotopicCentrifuge$ComputerHandler() {
      this.register(
         MethodData.builder("getInput", TileEntityIsotopicCentrifuge$ComputerHandler::inputTank$getInput)
            .returnType(ChemicalStack.class)
            .methodDescription("Get the contents of the input tank.")
      );
      this.register(
         MethodData.builder("getInputCapacity", TileEntityIsotopicCentrifuge$ComputerHandler::inputTank$getInputCapacity)
            .returnType(long.class)
            .methodDescription("Get the capacity of the input tank.")
      );
      this.register(
         MethodData.builder("getInputNeeded", TileEntityIsotopicCentrifuge$ComputerHandler::inputTank$getInputNeeded)
            .returnType(long.class)
            .methodDescription("Get the amount needed to fill the input tank.")
      );
      this.register(
         MethodData.builder("getInputFilledPercentage", TileEntityIsotopicCentrifuge$ComputerHandler::inputTank$getInputFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the input tank.")
      );
      this.register(
         MethodData.builder("getOutput", TileEntityIsotopicCentrifuge$ComputerHandler::outputTank$getOutput)
            .returnType(ChemicalStack.class)
            .methodDescription("Get the contents of the output tank.")
      );
      this.register(
         MethodData.builder("getOutputCapacity", TileEntityIsotopicCentrifuge$ComputerHandler::outputTank$getOutputCapacity)
            .returnType(long.class)
            .methodDescription("Get the capacity of the output tank.")
      );
      this.register(
         MethodData.builder("getOutputNeeded", TileEntityIsotopicCentrifuge$ComputerHandler::outputTank$getOutputNeeded)
            .returnType(long.class)
            .methodDescription("Get the amount needed to fill the output tank.")
      );
      this.register(
         MethodData.builder("getOutputFilledPercentage", TileEntityIsotopicCentrifuge$ComputerHandler::outputTank$getOutputFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the output tank.")
      );
      this.register(
         MethodData.builder("getInputItem", TileEntityIsotopicCentrifuge$ComputerHandler::inputSlot$getInputItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the input slot.")
      );
      this.register(
         MethodData.builder("getOutputItem", TileEntityIsotopicCentrifuge$ComputerHandler::outputSlot$getOutputItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the output slot.")
      );
      this.register(
         MethodData.builder("getEnergyItem", TileEntityIsotopicCentrifuge$ComputerHandler::energySlot$getEnergyItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the energy slot.")
      );
      this.register(
         MethodData.builder("getEnergyUsage", TileEntityIsotopicCentrifuge$ComputerHandler::getEnergyUsage_0)
            .returnType(FloatingLong.class)
            .methodDescription("Get the energy used in the last tick by the machine")
      );
   }

   public static Object inputTank$getInput(TileEntityIsotopicCentrifuge subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getStack(subject.inputTank));
   }

   public static Object inputTank$getInputCapacity(TileEntityIsotopicCentrifuge subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getCapacity(subject.inputTank));
   }

   public static Object inputTank$getInputNeeded(TileEntityIsotopicCentrifuge subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getNeeded(subject.inputTank));
   }

   public static Object inputTank$getInputFilledPercentage(TileEntityIsotopicCentrifuge subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getFilledPercentage(subject.inputTank));
   }

   public static Object outputTank$getOutput(TileEntityIsotopicCentrifuge subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getStack(subject.outputTank));
   }

   public static Object outputTank$getOutputCapacity(TileEntityIsotopicCentrifuge subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getCapacity(subject.outputTank));
   }

   public static Object outputTank$getOutputNeeded(TileEntityIsotopicCentrifuge subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getNeeded(subject.outputTank));
   }

   public static Object outputTank$getOutputFilledPercentage(TileEntityIsotopicCentrifuge subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getFilledPercentage(subject.outputTank));
   }

   public static Object inputSlot$getInputItem(TileEntityIsotopicCentrifuge subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.inputSlot));
   }

   public static Object outputSlot$getOutputItem(TileEntityIsotopicCentrifuge subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.outputSlot));
   }

   public static Object energySlot$getEnergyItem(TileEntityIsotopicCentrifuge subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.energySlot));
   }

   public static Object getEnergyUsage_0(TileEntityIsotopicCentrifuge subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getEnergyUsed());
   }
}
