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
   target = TileEntityChemicalDissolutionChamber.class
)
public class TileEntityChemicalDissolutionChamber$ComputerHandler extends ComputerMethodFactory<TileEntityChemicalDissolutionChamber> {
   public TileEntityChemicalDissolutionChamber$ComputerHandler() {
      this.register(
         MethodData.builder("getGasInput", TileEntityChemicalDissolutionChamber$ComputerHandler::injectTank$getGasInput)
            .returnType(ChemicalStack.class)
            .methodDescription("Get the contents of the gas input tank.")
      );
      this.register(
         MethodData.builder("getGasInputCapacity", TileEntityChemicalDissolutionChamber$ComputerHandler::injectTank$getGasInputCapacity)
            .returnType(long.class)
            .methodDescription("Get the capacity of the gas input tank.")
      );
      this.register(
         MethodData.builder("getGasInputNeeded", TileEntityChemicalDissolutionChamber$ComputerHandler::injectTank$getGasInputNeeded)
            .returnType(long.class)
            .methodDescription("Get the amount needed to fill the gas input tank.")
      );
      this.register(
         MethodData.builder("getGasInputFilledPercentage", TileEntityChemicalDissolutionChamber$ComputerHandler::injectTank$getGasInputFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the gas input tank.")
      );
      this.register(
         MethodData.builder("getInputGasItem", TileEntityChemicalDissolutionChamber$ComputerHandler::gasInputSlot$getInputGasItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the gas input item slot.")
      );
      this.register(
         MethodData.builder("getInputItem", TileEntityChemicalDissolutionChamber$ComputerHandler::inputSlot$getInputItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the input slot.")
      );
      this.register(
         MethodData.builder("getOutputItem", TileEntityChemicalDissolutionChamber$ComputerHandler::outputSlot$getOutputItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the output slot.")
      );
      this.register(
         MethodData.builder("getEnergyItem", TileEntityChemicalDissolutionChamber$ComputerHandler::energySlot$getEnergyItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the energy slot.")
      );
      this.register(
         MethodData.builder("getEnergyUsage", TileEntityChemicalDissolutionChamber$ComputerHandler::getEnergyUsage_0)
            .returnType(FloatingLong.class)
            .methodDescription("Get the energy used in the last tick by the machine")
      );
      this.register(
         MethodData.builder("getOutput", TileEntityChemicalDissolutionChamber$ComputerHandler::getOutputTank$getOutput)
            .returnType(ChemicalStack.class)
            .methodDescription("Get the contents of the output tank.")
      );
      this.register(
         MethodData.builder("getOutputCapacity", TileEntityChemicalDissolutionChamber$ComputerHandler::getOutputTank$getOutputCapacity)
            .returnType(long.class)
            .methodDescription("Get the capacity of the output tank.")
      );
      this.register(
         MethodData.builder("getOutputNeeded", TileEntityChemicalDissolutionChamber$ComputerHandler::getOutputTank$getOutputNeeded)
            .returnType(long.class)
            .methodDescription("Get the amount needed to fill the output tank.")
      );
      this.register(
         MethodData.builder("getOutputFilledPercentage", TileEntityChemicalDissolutionChamber$ComputerHandler::getOutputTank$getOutputFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the output tank.")
      );
   }

   public static Object injectTank$getGasInput(TileEntityChemicalDissolutionChamber subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getStack(subject.injectTank));
   }

   public static Object injectTank$getGasInputCapacity(TileEntityChemicalDissolutionChamber subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getCapacity(subject.injectTank));
   }

   public static Object injectTank$getGasInputNeeded(TileEntityChemicalDissolutionChamber subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getNeeded(subject.injectTank));
   }

   public static Object injectTank$getGasInputFilledPercentage(TileEntityChemicalDissolutionChamber subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getFilledPercentage(subject.injectTank));
   }

   public static Object gasInputSlot$getInputGasItem(TileEntityChemicalDissolutionChamber subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.gasInputSlot));
   }

   public static Object inputSlot$getInputItem(TileEntityChemicalDissolutionChamber subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.inputSlot));
   }

   public static Object outputSlot$getOutputItem(TileEntityChemicalDissolutionChamber subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.outputSlot));
   }

   public static Object energySlot$getEnergyItem(TileEntityChemicalDissolutionChamber subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.energySlot));
   }

   public static Object getEnergyUsage_0(TileEntityChemicalDissolutionChamber subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getEnergyUsage());
   }

   public static Object getOutputTank$getOutput(TileEntityChemicalDissolutionChamber subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getStack(subject.getOutputTank()));
   }

   public static Object getOutputTank$getOutputCapacity(TileEntityChemicalDissolutionChamber subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getCapacity(subject.getOutputTank()));
   }

   public static Object getOutputTank$getOutputNeeded(TileEntityChemicalDissolutionChamber subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getNeeded(subject.getOutputTank()));
   }

   public static Object getOutputTank$getOutputFilledPercentage(TileEntityChemicalDissolutionChamber subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getFilledPercentage(subject.getOutputTank()));
   }
}
