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
   target = TileEntityPigmentExtractor.class
)
public class TileEntityPigmentExtractor$ComputerHandler extends ComputerMethodFactory<TileEntityPigmentExtractor> {
   public TileEntityPigmentExtractor$ComputerHandler() {
      this.register(
         MethodData.builder("getOutput", TileEntityPigmentExtractor$ComputerHandler::pigmentTank$getOutput)
            .returnType(ChemicalStack.class)
            .methodDescription("Get the contents of the pigment tank.")
      );
      this.register(
         MethodData.builder("getOutputCapacity", TileEntityPigmentExtractor$ComputerHandler::pigmentTank$getOutputCapacity)
            .returnType(long.class)
            .methodDescription("Get the capacity of the pigment tank.")
      );
      this.register(
         MethodData.builder("getOutputNeeded", TileEntityPigmentExtractor$ComputerHandler::pigmentTank$getOutputNeeded)
            .returnType(long.class)
            .methodDescription("Get the amount needed to fill the pigment tank.")
      );
      this.register(
         MethodData.builder("getOutputFilledPercentage", TileEntityPigmentExtractor$ComputerHandler::pigmentTank$getOutputFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the pigment tank.")
      );
      this.register(
         MethodData.builder("getInput", TileEntityPigmentExtractor$ComputerHandler::inputSlot$getInput)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the input slot.")
      );
      this.register(
         MethodData.builder("getOutputItem", TileEntityPigmentExtractor$ComputerHandler::outputSlot$getOutputItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the output slot.")
      );
      this.register(
         MethodData.builder("getEnergyItem", TileEntityPigmentExtractor$ComputerHandler::energySlot$getEnergyItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the energy slot.")
      );
      this.register(
         MethodData.builder("getEnergyUsage", TileEntityPigmentExtractor$ComputerHandler::getEnergyUsage_0)
            .returnType(FloatingLong.class)
            .methodDescription("Get the energy used in the last tick by the machine")
      );
   }

   public static Object pigmentTank$getOutput(TileEntityPigmentExtractor subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getStack(subject.pigmentTank));
   }

   public static Object pigmentTank$getOutputCapacity(TileEntityPigmentExtractor subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getCapacity(subject.pigmentTank));
   }

   public static Object pigmentTank$getOutputNeeded(TileEntityPigmentExtractor subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getNeeded(subject.pigmentTank));
   }

   public static Object pigmentTank$getOutputFilledPercentage(TileEntityPigmentExtractor subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getFilledPercentage(subject.pigmentTank));
   }

   public static Object inputSlot$getInput(TileEntityPigmentExtractor subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.inputSlot));
   }

   public static Object outputSlot$getOutputItem(TileEntityPigmentExtractor subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.outputSlot));
   }

   public static Object energySlot$getEnergyItem(TileEntityPigmentExtractor subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.energySlot));
   }

   public static Object getEnergyUsage_0(TileEntityPigmentExtractor subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getEnergyUsage());
   }
}
