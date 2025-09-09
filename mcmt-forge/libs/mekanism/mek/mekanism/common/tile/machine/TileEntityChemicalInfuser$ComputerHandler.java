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
   target = TileEntityChemicalInfuser.class
)
public class TileEntityChemicalInfuser$ComputerHandler extends ComputerMethodFactory<TileEntityChemicalInfuser> {
   public TileEntityChemicalInfuser$ComputerHandler() {
      this.register(
         MethodData.builder("getLeftInput", TileEntityChemicalInfuser$ComputerHandler::leftTank$getLeftInput)
            .returnType(ChemicalStack.class)
            .methodDescription("Get the contents of the left input tank.")
      );
      this.register(
         MethodData.builder("getLeftInputCapacity", TileEntityChemicalInfuser$ComputerHandler::leftTank$getLeftInputCapacity)
            .returnType(long.class)
            .methodDescription("Get the capacity of the left input tank.")
      );
      this.register(
         MethodData.builder("getLeftInputNeeded", TileEntityChemicalInfuser$ComputerHandler::leftTank$getLeftInputNeeded)
            .returnType(long.class)
            .methodDescription("Get the amount needed to fill the left input tank.")
      );
      this.register(
         MethodData.builder("getLeftInputFilledPercentage", TileEntityChemicalInfuser$ComputerHandler::leftTank$getLeftInputFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the left input tank.")
      );
      this.register(
         MethodData.builder("getRightInput", TileEntityChemicalInfuser$ComputerHandler::rightTank$getRightInput)
            .returnType(ChemicalStack.class)
            .methodDescription("Get the contents of the right input tank.")
      );
      this.register(
         MethodData.builder("getRightInputCapacity", TileEntityChemicalInfuser$ComputerHandler::rightTank$getRightInputCapacity)
            .returnType(long.class)
            .methodDescription("Get the capacity of the right input tank.")
      );
      this.register(
         MethodData.builder("getRightInputNeeded", TileEntityChemicalInfuser$ComputerHandler::rightTank$getRightInputNeeded)
            .returnType(long.class)
            .methodDescription("Get the amount needed to fill the right input tank.")
      );
      this.register(
         MethodData.builder("getRightInputFilledPercentage", TileEntityChemicalInfuser$ComputerHandler::rightTank$getRightInputFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the right input tank.")
      );
      this.register(
         MethodData.builder("getOutput", TileEntityChemicalInfuser$ComputerHandler::centerTank$getOutput)
            .returnType(ChemicalStack.class)
            .methodDescription("Get the contents of the output (center) tank.")
      );
      this.register(
         MethodData.builder("getOutputCapacity", TileEntityChemicalInfuser$ComputerHandler::centerTank$getOutputCapacity)
            .returnType(long.class)
            .methodDescription("Get the capacity of the output (center) tank.")
      );
      this.register(
         MethodData.builder("getOutputNeeded", TileEntityChemicalInfuser$ComputerHandler::centerTank$getOutputNeeded)
            .returnType(long.class)
            .methodDescription("Get the amount needed to fill the output (center) tank.")
      );
      this.register(
         MethodData.builder("getOutputFilledPercentage", TileEntityChemicalInfuser$ComputerHandler::centerTank$getOutputFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the output (center) tank.")
      );
      this.register(
         MethodData.builder("getLeftInputItem", TileEntityChemicalInfuser$ComputerHandler::leftInputSlot$getLeftInputItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the left input item slot.")
      );
      this.register(
         MethodData.builder("getOutputItem", TileEntityChemicalInfuser$ComputerHandler::outputSlot$getOutputItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the output item slot.")
      );
      this.register(
         MethodData.builder("getRightInputItem", TileEntityChemicalInfuser$ComputerHandler::rightInputSlot$getRightInputItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the right input item slot.")
      );
      this.register(
         MethodData.builder("getEnergyItem", TileEntityChemicalInfuser$ComputerHandler::energySlot$getEnergyItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the energy slot.")
      );
      this.register(
         MethodData.builder("getEnergyUsage", TileEntityChemicalInfuser$ComputerHandler::getEnergyUsage_0)
            .returnType(FloatingLong.class)
            .methodDescription("Get the energy used in the last tick by the machine")
      );
   }

   public static Object leftTank$getLeftInput(TileEntityChemicalInfuser subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getStack(subject.leftTank));
   }

   public static Object leftTank$getLeftInputCapacity(TileEntityChemicalInfuser subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getCapacity(subject.leftTank));
   }

   public static Object leftTank$getLeftInputNeeded(TileEntityChemicalInfuser subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getNeeded(subject.leftTank));
   }

   public static Object leftTank$getLeftInputFilledPercentage(TileEntityChemicalInfuser subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getFilledPercentage(subject.leftTank));
   }

   public static Object rightTank$getRightInput(TileEntityChemicalInfuser subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getStack(subject.rightTank));
   }

   public static Object rightTank$getRightInputCapacity(TileEntityChemicalInfuser subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getCapacity(subject.rightTank));
   }

   public static Object rightTank$getRightInputNeeded(TileEntityChemicalInfuser subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getNeeded(subject.rightTank));
   }

   public static Object rightTank$getRightInputFilledPercentage(TileEntityChemicalInfuser subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getFilledPercentage(subject.rightTank));
   }

   public static Object centerTank$getOutput(TileEntityChemicalInfuser subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getStack(subject.centerTank));
   }

   public static Object centerTank$getOutputCapacity(TileEntityChemicalInfuser subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getCapacity(subject.centerTank));
   }

   public static Object centerTank$getOutputNeeded(TileEntityChemicalInfuser subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getNeeded(subject.centerTank));
   }

   public static Object centerTank$getOutputFilledPercentage(TileEntityChemicalInfuser subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getFilledPercentage(subject.centerTank));
   }

   public static Object leftInputSlot$getLeftInputItem(TileEntityChemicalInfuser subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.leftInputSlot));
   }

   public static Object outputSlot$getOutputItem(TileEntityChemicalInfuser subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.outputSlot));
   }

   public static Object rightInputSlot$getRightInputItem(TileEntityChemicalInfuser subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.rightInputSlot));
   }

   public static Object energySlot$getEnergyItem(TileEntityChemicalInfuser subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.energySlot));
   }

   public static Object getEnergyUsage_0(TileEntityChemicalInfuser subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getEnergyUsed());
   }
}
