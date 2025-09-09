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
   target = TileEntityChemicalOxidizer.class
)
public class TileEntityChemicalOxidizer$ComputerHandler extends ComputerMethodFactory<TileEntityChemicalOxidizer> {
   public TileEntityChemicalOxidizer$ComputerHandler() {
      this.register(
         MethodData.builder("getOutput", TileEntityChemicalOxidizer$ComputerHandler::gasTank$getOutput)
            .returnType(ChemicalStack.class)
            .methodDescription("Get the contents of the output tank.")
      );
      this.register(
         MethodData.builder("getOutputCapacity", TileEntityChemicalOxidizer$ComputerHandler::gasTank$getOutputCapacity)
            .returnType(long.class)
            .methodDescription("Get the capacity of the output tank.")
      );
      this.register(
         MethodData.builder("getOutputNeeded", TileEntityChemicalOxidizer$ComputerHandler::gasTank$getOutputNeeded)
            .returnType(long.class)
            .methodDescription("Get the amount needed to fill the output tank.")
      );
      this.register(
         MethodData.builder("getOutputFilledPercentage", TileEntityChemicalOxidizer$ComputerHandler::gasTank$getOutputFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the output tank.")
      );
      this.register(
         MethodData.builder("getInput", TileEntityChemicalOxidizer$ComputerHandler::inputSlot$getInput)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the input slot.")
      );
      this.register(
         MethodData.builder("getOutputItem", TileEntityChemicalOxidizer$ComputerHandler::outputSlot$getOutputItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the output item slot.")
      );
      this.register(
         MethodData.builder("getEnergyItem", TileEntityChemicalOxidizer$ComputerHandler::energySlot$getEnergyItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the energy slot.")
      );
      this.register(
         MethodData.builder("getEnergyUsage", TileEntityChemicalOxidizer$ComputerHandler::getEnergyUsage_0)
            .returnType(FloatingLong.class)
            .methodDescription("Get the energy used in the last tick by the machine")
      );
   }

   public static Object gasTank$getOutput(TileEntityChemicalOxidizer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getStack(subject.gasTank));
   }

   public static Object gasTank$getOutputCapacity(TileEntityChemicalOxidizer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getCapacity(subject.gasTank));
   }

   public static Object gasTank$getOutputNeeded(TileEntityChemicalOxidizer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getNeeded(subject.gasTank));
   }

   public static Object gasTank$getOutputFilledPercentage(TileEntityChemicalOxidizer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getFilledPercentage(subject.gasTank));
   }

   public static Object inputSlot$getInput(TileEntityChemicalOxidizer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.inputSlot));
   }

   public static Object outputSlot$getOutputItem(TileEntityChemicalOxidizer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.outputSlot));
   }

   public static Object energySlot$getEnergyItem(TileEntityChemicalOxidizer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.energySlot));
   }

   public static Object getEnergyUsage_0(TileEntityChemicalOxidizer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getEnergyUsage());
   }
}
