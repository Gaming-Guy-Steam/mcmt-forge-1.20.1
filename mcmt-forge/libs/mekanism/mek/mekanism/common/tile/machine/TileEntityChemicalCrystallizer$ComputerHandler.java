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
   target = TileEntityChemicalCrystallizer.class
)
public class TileEntityChemicalCrystallizer$ComputerHandler extends ComputerMethodFactory<TileEntityChemicalCrystallizer> {
   public TileEntityChemicalCrystallizer$ComputerHandler() {
      this.register(
         MethodData.builder("getInputItem", TileEntityChemicalCrystallizer$ComputerHandler::inputSlot$getInputItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the input item slot.")
      );
      this.register(
         MethodData.builder("getOutput", TileEntityChemicalCrystallizer$ComputerHandler::outputSlot$getOutput)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the output slot.")
      );
      this.register(
         MethodData.builder("getEnergyItem", TileEntityChemicalCrystallizer$ComputerHandler::energySlot$getEnergyItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the energy slot.")
      );
      this.register(
         MethodData.builder("getEnergyUsage", TileEntityChemicalCrystallizer$ComputerHandler::getEnergyUsage_0)
            .returnType(FloatingLong.class)
            .methodDescription("Get the energy used in the last tick by the machine")
      );
      this.register(
         MethodData.builder("getInput", TileEntityChemicalCrystallizer$ComputerHandler::getInputTank$getInput)
            .returnType(ChemicalStack.class)
            .methodDescription("Get the contents of the input tank.")
      );
      this.register(
         MethodData.builder("getInputCapacity", TileEntityChemicalCrystallizer$ComputerHandler::getInputTank$getInputCapacity)
            .returnType(long.class)
            .methodDescription("Get the capacity of the input tank.")
      );
      this.register(
         MethodData.builder("getInputNeeded", TileEntityChemicalCrystallizer$ComputerHandler::getInputTank$getInputNeeded)
            .returnType(long.class)
            .methodDescription("Get the amount needed to fill the input tank.")
      );
      this.register(
         MethodData.builder("getInputFilledPercentage", TileEntityChemicalCrystallizer$ComputerHandler::getInputTank$getInputFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the input tank.")
      );
   }

   public static Object inputSlot$getInputItem(TileEntityChemicalCrystallizer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.inputSlot));
   }

   public static Object outputSlot$getOutput(TileEntityChemicalCrystallizer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.outputSlot));
   }

   public static Object energySlot$getEnergyItem(TileEntityChemicalCrystallizer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.energySlot));
   }

   public static Object getEnergyUsage_0(TileEntityChemicalCrystallizer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getEnergyUsage());
   }

   public static Object getInputTank$getInput(TileEntityChemicalCrystallizer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getStack(subject.getInputTank()));
   }

   public static Object getInputTank$getInputCapacity(TileEntityChemicalCrystallizer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getCapacity(subject.getInputTank()));
   }

   public static Object getInputTank$getInputNeeded(TileEntityChemicalCrystallizer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getNeeded(subject.getInputTank()));
   }

   public static Object getInputTank$getInputFilledPercentage(TileEntityChemicalCrystallizer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getFilledPercentage(subject.getInputTank()));
   }
}
