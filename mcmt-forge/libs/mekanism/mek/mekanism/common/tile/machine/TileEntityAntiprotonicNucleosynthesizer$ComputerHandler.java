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
   target = TileEntityAntiprotonicNucleosynthesizer.class
)
public class TileEntityAntiprotonicNucleosynthesizer$ComputerHandler extends ComputerMethodFactory<TileEntityAntiprotonicNucleosynthesizer> {
   public TileEntityAntiprotonicNucleosynthesizer$ComputerHandler() {
      this.register(
         MethodData.builder("getInputChemical", TileEntityAntiprotonicNucleosynthesizer$ComputerHandler::gasTank$getInputChemical)
            .returnType(ChemicalStack.class)
            .methodDescription("Get the contents of the input gas tank.")
      );
      this.register(
         MethodData.builder("getInputChemicalCapacity", TileEntityAntiprotonicNucleosynthesizer$ComputerHandler::gasTank$getInputChemicalCapacity)
            .returnType(long.class)
            .methodDescription("Get the capacity of the input gas tank.")
      );
      this.register(
         MethodData.builder("getInputChemicalNeeded", TileEntityAntiprotonicNucleosynthesizer$ComputerHandler::gasTank$getInputChemicalNeeded)
            .returnType(long.class)
            .methodDescription("Get the amount needed to fill the input gas tank.")
      );
      this.register(
         MethodData.builder(
               "getInputChemicalFilledPercentage", TileEntityAntiprotonicNucleosynthesizer$ComputerHandler::gasTank$getInputChemicalFilledPercentage
            )
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the input gas tank.")
      );
      this.register(
         MethodData.builder("getInputChemicalItem", TileEntityAntiprotonicNucleosynthesizer$ComputerHandler::gasInputSlot$getInputChemicalItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the input gas item slot.")
      );
      this.register(
         MethodData.builder("getInputItem", TileEntityAntiprotonicNucleosynthesizer$ComputerHandler::inputSlot$getInputItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the input item slot.")
      );
      this.register(
         MethodData.builder("getOutputItem", TileEntityAntiprotonicNucleosynthesizer$ComputerHandler::outputSlot$getOutputItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the output slot.")
      );
      this.register(
         MethodData.builder("getEnergyItem", TileEntityAntiprotonicNucleosynthesizer$ComputerHandler::energySlot$getEnergyItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the energy slot.")
      );
      this.register(
         MethodData.builder("getEnergyUsage", TileEntityAntiprotonicNucleosynthesizer$ComputerHandler::getEnergyUsage_0)
            .returnType(FloatingLong.class)
            .methodDescription("Get the energy used in the last tick by the machine")
      );
   }

   public static Object gasTank$getInputChemical(TileEntityAntiprotonicNucleosynthesizer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getStack(subject.gasTank));
   }

   public static Object gasTank$getInputChemicalCapacity(TileEntityAntiprotonicNucleosynthesizer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getCapacity(subject.gasTank));
   }

   public static Object gasTank$getInputChemicalNeeded(TileEntityAntiprotonicNucleosynthesizer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getNeeded(subject.gasTank));
   }

   public static Object gasTank$getInputChemicalFilledPercentage(TileEntityAntiprotonicNucleosynthesizer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getFilledPercentage(subject.gasTank));
   }

   public static Object gasInputSlot$getInputChemicalItem(TileEntityAntiprotonicNucleosynthesizer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.gasInputSlot));
   }

   public static Object inputSlot$getInputItem(TileEntityAntiprotonicNucleosynthesizer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.inputSlot));
   }

   public static Object outputSlot$getOutputItem(TileEntityAntiprotonicNucleosynthesizer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.outputSlot));
   }

   public static Object energySlot$getEnergyItem(TileEntityAntiprotonicNucleosynthesizer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.energySlot));
   }

   public static Object getEnergyUsage_0(TileEntityAntiprotonicNucleosynthesizer subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getEnergyUsed());
   }
}
