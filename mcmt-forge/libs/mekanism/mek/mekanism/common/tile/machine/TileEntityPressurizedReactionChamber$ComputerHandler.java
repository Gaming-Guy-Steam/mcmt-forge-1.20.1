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
import net.minecraftforge.fluids.FluidStack;

@MethodFactory(
   target = TileEntityPressurizedReactionChamber.class
)
public class TileEntityPressurizedReactionChamber$ComputerHandler extends ComputerMethodFactory<TileEntityPressurizedReactionChamber> {
   public TileEntityPressurizedReactionChamber$ComputerHandler() {
      this.register(
         MethodData.builder("getInputFluid", TileEntityPressurizedReactionChamber$ComputerHandler::inputFluidTank$getInputFluid)
            .returnType(FluidStack.class)
            .methodDescription("Get the contents of the fluid input.")
      );
      this.register(
         MethodData.builder("getInputFluidCapacity", TileEntityPressurizedReactionChamber$ComputerHandler::inputFluidTank$getInputFluidCapacity)
            .returnType(int.class)
            .methodDescription("Get the capacity of the fluid input.")
      );
      this.register(
         MethodData.builder("getInputFluidNeeded", TileEntityPressurizedReactionChamber$ComputerHandler::inputFluidTank$getInputFluidNeeded)
            .returnType(int.class)
            .methodDescription("Get the amount needed to fill the fluid input.")
      );
      this.register(
         MethodData.builder("getInputFluidFilledPercentage", TileEntityPressurizedReactionChamber$ComputerHandler::inputFluidTank$getInputFluidFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the fluid input.")
      );
      this.register(
         MethodData.builder("getInputGas", TileEntityPressurizedReactionChamber$ComputerHandler::inputGasTank$getInputGas)
            .returnType(ChemicalStack.class)
            .methodDescription("Get the contents of the gas input.")
      );
      this.register(
         MethodData.builder("getInputGasCapacity", TileEntityPressurizedReactionChamber$ComputerHandler::inputGasTank$getInputGasCapacity)
            .returnType(long.class)
            .methodDescription("Get the capacity of the gas input.")
      );
      this.register(
         MethodData.builder("getInputGasNeeded", TileEntityPressurizedReactionChamber$ComputerHandler::inputGasTank$getInputGasNeeded)
            .returnType(long.class)
            .methodDescription("Get the amount needed to fill the gas input.")
      );
      this.register(
         MethodData.builder("getInputGasFilledPercentage", TileEntityPressurizedReactionChamber$ComputerHandler::inputGasTank$getInputGasFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the gas input.")
      );
      this.register(
         MethodData.builder("getOutputGas", TileEntityPressurizedReactionChamber$ComputerHandler::outputGasTank$getOutputGas)
            .returnType(ChemicalStack.class)
            .methodDescription("Get the contents of the gas output.")
      );
      this.register(
         MethodData.builder("getOutputGasCapacity", TileEntityPressurizedReactionChamber$ComputerHandler::outputGasTank$getOutputGasCapacity)
            .returnType(long.class)
            .methodDescription("Get the capacity of the gas output.")
      );
      this.register(
         MethodData.builder("getOutputGasNeeded", TileEntityPressurizedReactionChamber$ComputerHandler::outputGasTank$getOutputGasNeeded)
            .returnType(long.class)
            .methodDescription("Get the amount needed to fill the gas output.")
      );
      this.register(
         MethodData.builder("getOutputGasFilledPercentage", TileEntityPressurizedReactionChamber$ComputerHandler::outputGasTank$getOutputGasFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the gas output.")
      );
      this.register(
         MethodData.builder("getInputItem", TileEntityPressurizedReactionChamber$ComputerHandler::inputSlot$getInputItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the item input slot.")
      );
      this.register(
         MethodData.builder("getOutputItem", TileEntityPressurizedReactionChamber$ComputerHandler::outputSlot$getOutputItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the item output slot.")
      );
      this.register(
         MethodData.builder("getEnergyItem", TileEntityPressurizedReactionChamber$ComputerHandler::energySlot$getEnergyItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the energy slot.")
      );
      this.register(
         MethodData.builder("getEnergyUsage", TileEntityPressurizedReactionChamber$ComputerHandler::getEnergyUsage_0)
            .returnType(FloatingLong.class)
            .methodDescription("Get the energy used in the last tick by the machine")
      );
   }

   public static Object inputFluidTank$getInputFluid(TileEntityPressurizedReactionChamber subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerFluidTankWrapper.getStack(subject.inputFluidTank));
   }

   public static Object inputFluidTank$getInputFluidCapacity(TileEntityPressurizedReactionChamber subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerFluidTankWrapper.getCapacity(subject.inputFluidTank));
   }

   public static Object inputFluidTank$getInputFluidNeeded(TileEntityPressurizedReactionChamber subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerFluidTankWrapper.getNeeded(subject.inputFluidTank));
   }

   public static Object inputFluidTank$getInputFluidFilledPercentage(TileEntityPressurizedReactionChamber subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerFluidTankWrapper.getFilledPercentage(subject.inputFluidTank));
   }

   public static Object inputGasTank$getInputGas(TileEntityPressurizedReactionChamber subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getStack(subject.inputGasTank));
   }

   public static Object inputGasTank$getInputGasCapacity(TileEntityPressurizedReactionChamber subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getCapacity(subject.inputGasTank));
   }

   public static Object inputGasTank$getInputGasNeeded(TileEntityPressurizedReactionChamber subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getNeeded(subject.inputGasTank));
   }

   public static Object inputGasTank$getInputGasFilledPercentage(TileEntityPressurizedReactionChamber subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getFilledPercentage(subject.inputGasTank));
   }

   public static Object outputGasTank$getOutputGas(TileEntityPressurizedReactionChamber subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getStack(subject.outputGasTank));
   }

   public static Object outputGasTank$getOutputGasCapacity(TileEntityPressurizedReactionChamber subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getCapacity(subject.outputGasTank));
   }

   public static Object outputGasTank$getOutputGasNeeded(TileEntityPressurizedReactionChamber subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getNeeded(subject.outputGasTank));
   }

   public static Object outputGasTank$getOutputGasFilledPercentage(TileEntityPressurizedReactionChamber subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getFilledPercentage(subject.outputGasTank));
   }

   public static Object inputSlot$getInputItem(TileEntityPressurizedReactionChamber subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.inputSlot));
   }

   public static Object outputSlot$getOutputItem(TileEntityPressurizedReactionChamber subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.outputSlot));
   }

   public static Object energySlot$getEnergyItem(TileEntityPressurizedReactionChamber subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.energySlot));
   }

   public static Object getEnergyUsage_0(TileEntityPressurizedReactionChamber subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getEnergyUsage());
   }
}
