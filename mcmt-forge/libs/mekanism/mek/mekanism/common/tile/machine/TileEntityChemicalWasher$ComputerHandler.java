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
   target = TileEntityChemicalWasher.class
)
public class TileEntityChemicalWasher$ComputerHandler extends ComputerMethodFactory<TileEntityChemicalWasher> {
   public TileEntityChemicalWasher$ComputerHandler() {
      this.register(
         MethodData.builder("getFluid", TileEntityChemicalWasher$ComputerHandler::fluidTank$getFluid)
            .returnType(FluidStack.class)
            .methodDescription("Get the contents of the fluid tank.")
      );
      this.register(
         MethodData.builder("getFluidCapacity", TileEntityChemicalWasher$ComputerHandler::fluidTank$getFluidCapacity)
            .returnType(int.class)
            .methodDescription("Get the capacity of the fluid tank.")
      );
      this.register(
         MethodData.builder("getFluidNeeded", TileEntityChemicalWasher$ComputerHandler::fluidTank$getFluidNeeded)
            .returnType(int.class)
            .methodDescription("Get the amount needed to fill the fluid tank.")
      );
      this.register(
         MethodData.builder("getFluidFilledPercentage", TileEntityChemicalWasher$ComputerHandler::fluidTank$getFluidFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the fluid tank.")
      );
      this.register(
         MethodData.builder("getSlurryInput", TileEntityChemicalWasher$ComputerHandler::inputTank$getSlurryInput)
            .returnType(ChemicalStack.class)
            .methodDescription("Get the contents of the input slurry tank.")
      );
      this.register(
         MethodData.builder("getSlurryInputCapacity", TileEntityChemicalWasher$ComputerHandler::inputTank$getSlurryInputCapacity)
            .returnType(long.class)
            .methodDescription("Get the capacity of the input slurry tank.")
      );
      this.register(
         MethodData.builder("getSlurryInputNeeded", TileEntityChemicalWasher$ComputerHandler::inputTank$getSlurryInputNeeded)
            .returnType(long.class)
            .methodDescription("Get the amount needed to fill the input slurry tank.")
      );
      this.register(
         MethodData.builder("getSlurryInputFilledPercentage", TileEntityChemicalWasher$ComputerHandler::inputTank$getSlurryInputFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the input slurry tank.")
      );
      this.register(
         MethodData.builder("getSlurryOutput", TileEntityChemicalWasher$ComputerHandler::outputTank$getSlurryOutput)
            .returnType(ChemicalStack.class)
            .methodDescription("Get the contents of the output slurry tank.")
      );
      this.register(
         MethodData.builder("getSlurryOutputCapacity", TileEntityChemicalWasher$ComputerHandler::outputTank$getSlurryOutputCapacity)
            .returnType(long.class)
            .methodDescription("Get the capacity of the output slurry tank.")
      );
      this.register(
         MethodData.builder("getSlurryOutputNeeded", TileEntityChemicalWasher$ComputerHandler::outputTank$getSlurryOutputNeeded)
            .returnType(long.class)
            .methodDescription("Get the amount needed to fill the output slurry tank.")
      );
      this.register(
         MethodData.builder("getSlurryOutputFilledPercentage", TileEntityChemicalWasher$ComputerHandler::outputTank$getSlurryOutputFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the output slurry tank.")
      );
      this.register(
         MethodData.builder("getFluidItemInput", TileEntityChemicalWasher$ComputerHandler::fluidSlot$getFluidItemInput)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the fluid item input slot.")
      );
      this.register(
         MethodData.builder("getFluidItemOutput", TileEntityChemicalWasher$ComputerHandler::fluidOutputSlot$getFluidItemOutput)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the fluid item output slot.")
      );
      this.register(
         MethodData.builder("getOutputItem", TileEntityChemicalWasher$ComputerHandler::slurryOutputSlot$getOutputItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the slurry item output slot.")
      );
      this.register(
         MethodData.builder("getEnergyItem", TileEntityChemicalWasher$ComputerHandler::energySlot$getEnergyItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the energy slot.")
      );
      this.register(
         MethodData.builder("getEnergyUsage", TileEntityChemicalWasher$ComputerHandler::getEnergyUsage_0)
            .returnType(FloatingLong.class)
            .methodDescription("Get the energy used in the last tick by the machine")
      );
   }

   public static Object fluidTank$getFluid(TileEntityChemicalWasher subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerFluidTankWrapper.getStack(subject.fluidTank));
   }

   public static Object fluidTank$getFluidCapacity(TileEntityChemicalWasher subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerFluidTankWrapper.getCapacity(subject.fluidTank));
   }

   public static Object fluidTank$getFluidNeeded(TileEntityChemicalWasher subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerFluidTankWrapper.getNeeded(subject.fluidTank));
   }

   public static Object fluidTank$getFluidFilledPercentage(TileEntityChemicalWasher subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerFluidTankWrapper.getFilledPercentage(subject.fluidTank));
   }

   public static Object inputTank$getSlurryInput(TileEntityChemicalWasher subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getStack(subject.inputTank));
   }

   public static Object inputTank$getSlurryInputCapacity(TileEntityChemicalWasher subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getCapacity(subject.inputTank));
   }

   public static Object inputTank$getSlurryInputNeeded(TileEntityChemicalWasher subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getNeeded(subject.inputTank));
   }

   public static Object inputTank$getSlurryInputFilledPercentage(TileEntityChemicalWasher subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getFilledPercentage(subject.inputTank));
   }

   public static Object outputTank$getSlurryOutput(TileEntityChemicalWasher subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getStack(subject.outputTank));
   }

   public static Object outputTank$getSlurryOutputCapacity(TileEntityChemicalWasher subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getCapacity(subject.outputTank));
   }

   public static Object outputTank$getSlurryOutputNeeded(TileEntityChemicalWasher subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getNeeded(subject.outputTank));
   }

   public static Object outputTank$getSlurryOutputFilledPercentage(TileEntityChemicalWasher subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getFilledPercentage(subject.outputTank));
   }

   public static Object fluidSlot$getFluidItemInput(TileEntityChemicalWasher subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.fluidSlot));
   }

   public static Object fluidOutputSlot$getFluidItemOutput(TileEntityChemicalWasher subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.fluidOutputSlot));
   }

   public static Object slurryOutputSlot$getOutputItem(TileEntityChemicalWasher subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.slurryOutputSlot));
   }

   public static Object energySlot$getEnergyItem(TileEntityChemicalWasher subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.energySlot));
   }

   public static Object getEnergyUsage_0(TileEntityChemicalWasher subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getEnergyUsed());
   }
}
