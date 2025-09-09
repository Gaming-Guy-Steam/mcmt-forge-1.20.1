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
   target = TileEntityPigmentMixer.class
)
public class TileEntityPigmentMixer$ComputerHandler extends ComputerMethodFactory<TileEntityPigmentMixer> {
   public TileEntityPigmentMixer$ComputerHandler() {
      this.register(
         MethodData.builder("getLeftInput", TileEntityPigmentMixer$ComputerHandler::leftInputTank$getLeftInput)
            .returnType(ChemicalStack.class)
            .methodDescription("Get the contents of the left pigment tank.")
      );
      this.register(
         MethodData.builder("getLeftInputCapacity", TileEntityPigmentMixer$ComputerHandler::leftInputTank$getLeftInputCapacity)
            .returnType(long.class)
            .methodDescription("Get the capacity of the left pigment tank.")
      );
      this.register(
         MethodData.builder("getLeftInputNeeded", TileEntityPigmentMixer$ComputerHandler::leftInputTank$getLeftInputNeeded)
            .returnType(long.class)
            .methodDescription("Get the amount needed to fill the left pigment tank.")
      );
      this.register(
         MethodData.builder("getLeftInputFilledPercentage", TileEntityPigmentMixer$ComputerHandler::leftInputTank$getLeftInputFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the left pigment tank.")
      );
      this.register(
         MethodData.builder("getRightInput", TileEntityPigmentMixer$ComputerHandler::rightInputTank$getRightInput)
            .returnType(ChemicalStack.class)
            .methodDescription("Get the contents of the right pigment tank.")
      );
      this.register(
         MethodData.builder("getRightInputCapacity", TileEntityPigmentMixer$ComputerHandler::rightInputTank$getRightInputCapacity)
            .returnType(long.class)
            .methodDescription("Get the capacity of the right pigment tank.")
      );
      this.register(
         MethodData.builder("getRightInputNeeded", TileEntityPigmentMixer$ComputerHandler::rightInputTank$getRightInputNeeded)
            .returnType(long.class)
            .methodDescription("Get the amount needed to fill the right pigment tank.")
      );
      this.register(
         MethodData.builder("getRightInputFilledPercentage", TileEntityPigmentMixer$ComputerHandler::rightInputTank$getRightInputFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the right pigment tank.")
      );
      this.register(
         MethodData.builder("getOutput", TileEntityPigmentMixer$ComputerHandler::outputTank$getOutput)
            .returnType(ChemicalStack.class)
            .methodDescription("Get the contents of the output pigment tank.")
      );
      this.register(
         MethodData.builder("getOutputCapacity", TileEntityPigmentMixer$ComputerHandler::outputTank$getOutputCapacity)
            .returnType(long.class)
            .methodDescription("Get the capacity of the output pigment tank.")
      );
      this.register(
         MethodData.builder("getOutputNeeded", TileEntityPigmentMixer$ComputerHandler::outputTank$getOutputNeeded)
            .returnType(long.class)
            .methodDescription("Get the amount needed to fill the output pigment tank.")
      );
      this.register(
         MethodData.builder("getOutputFilledPercentage", TileEntityPigmentMixer$ComputerHandler::outputTank$getOutputFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the output pigment tank.")
      );
      this.register(
         MethodData.builder("getLeftInputItem", TileEntityPigmentMixer$ComputerHandler::leftInputSlot$getLeftInputItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the left input slot.")
      );
      this.register(
         MethodData.builder("getOutputItem", TileEntityPigmentMixer$ComputerHandler::outputSlot$getOutputItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the output slot.")
      );
      this.register(
         MethodData.builder("getRightInputItem", TileEntityPigmentMixer$ComputerHandler::rightInputSlot$getRightInputItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the right input slot.")
      );
      this.register(
         MethodData.builder("getEnergyItem", TileEntityPigmentMixer$ComputerHandler::energySlot$getEnergyItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the energy slot.")
      );
      this.register(
         MethodData.builder("getEnergyUsage", TileEntityPigmentMixer$ComputerHandler::getEnergyUsage_0)
            .returnType(FloatingLong.class)
            .methodDescription("Get the energy used in the last tick by the machine")
      );
   }

   public static Object leftInputTank$getLeftInput(TileEntityPigmentMixer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getStack(subject.leftInputTank));
   }

   public static Object leftInputTank$getLeftInputCapacity(TileEntityPigmentMixer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getCapacity(subject.leftInputTank));
   }

   public static Object leftInputTank$getLeftInputNeeded(TileEntityPigmentMixer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getNeeded(subject.leftInputTank));
   }

   public static Object leftInputTank$getLeftInputFilledPercentage(TileEntityPigmentMixer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getFilledPercentage(subject.leftInputTank));
   }

   public static Object rightInputTank$getRightInput(TileEntityPigmentMixer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getStack(subject.rightInputTank));
   }

   public static Object rightInputTank$getRightInputCapacity(TileEntityPigmentMixer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getCapacity(subject.rightInputTank));
   }

   public static Object rightInputTank$getRightInputNeeded(TileEntityPigmentMixer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getNeeded(subject.rightInputTank));
   }

   public static Object rightInputTank$getRightInputFilledPercentage(TileEntityPigmentMixer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getFilledPercentage(subject.rightInputTank));
   }

   public static Object outputTank$getOutput(TileEntityPigmentMixer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getStack(subject.outputTank));
   }

   public static Object outputTank$getOutputCapacity(TileEntityPigmentMixer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getCapacity(subject.outputTank));
   }

   public static Object outputTank$getOutputNeeded(TileEntityPigmentMixer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getNeeded(subject.outputTank));
   }

   public static Object outputTank$getOutputFilledPercentage(TileEntityPigmentMixer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getFilledPercentage(subject.outputTank));
   }

   public static Object leftInputSlot$getLeftInputItem(TileEntityPigmentMixer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.leftInputSlot));
   }

   public static Object outputSlot$getOutputItem(TileEntityPigmentMixer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.outputSlot));
   }

   public static Object rightInputSlot$getRightInputItem(TileEntityPigmentMixer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.rightInputSlot));
   }

   public static Object energySlot$getEnergyItem(TileEntityPigmentMixer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.energySlot));
   }

   public static Object getEnergyUsage_0(TileEntityPigmentMixer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getEnergyUsed());
   }
}
