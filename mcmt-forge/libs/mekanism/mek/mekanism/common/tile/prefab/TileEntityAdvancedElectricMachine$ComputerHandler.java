package mekanism.common.tile.prefab;

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
   target = TileEntityAdvancedElectricMachine.class
)
public class TileEntityAdvancedElectricMachine$ComputerHandler extends ComputerMethodFactory<TileEntityAdvancedElectricMachine> {
   public TileEntityAdvancedElectricMachine$ComputerHandler() {
      this.register(
         MethodData.builder("getChemical", TileEntityAdvancedElectricMachine$ComputerHandler::gasTank$getChemical)
            .returnType(ChemicalStack.class)
            .methodDescription("Get the contents of the gas tank.")
      );
      this.register(
         MethodData.builder("getChemicalCapacity", TileEntityAdvancedElectricMachine$ComputerHandler::gasTank$getChemicalCapacity)
            .returnType(long.class)
            .methodDescription("Get the capacity of the gas tank.")
      );
      this.register(
         MethodData.builder("getChemicalNeeded", TileEntityAdvancedElectricMachine$ComputerHandler::gasTank$getChemicalNeeded)
            .returnType(long.class)
            .methodDescription("Get the amount needed to fill the gas tank.")
      );
      this.register(
         MethodData.builder("getChemicalFilledPercentage", TileEntityAdvancedElectricMachine$ComputerHandler::gasTank$getChemicalFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the gas tank.")
      );
      this.register(
         MethodData.builder("getInput", TileEntityAdvancedElectricMachine$ComputerHandler::inputSlot$getInput)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the input slot.")
      );
      this.register(
         MethodData.builder("getOutput", TileEntityAdvancedElectricMachine$ComputerHandler::outputSlot$getOutput)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the output slot.")
      );
      this.register(
         MethodData.builder("getChemicalItem", TileEntityAdvancedElectricMachine$ComputerHandler::secondarySlot$getChemicalItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the secondary input slot.")
      );
      this.register(
         MethodData.builder("getEnergyItem", TileEntityAdvancedElectricMachine$ComputerHandler::energySlot$getEnergyItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the energy slot.")
      );
      this.register(
         MethodData.builder("getEnergyUsage", TileEntityAdvancedElectricMachine$ComputerHandler::getEnergyUsage_0)
            .returnType(FloatingLong.class)
            .methodDescription("Get the energy used in the last tick by the machine")
      );
      this.register(
         MethodData.builder("dumpChemical", TileEntityAdvancedElectricMachine$ComputerHandler::dumpChemical_0)
            .methodDescription("Empty the contents of the gas tank into the environment")
            .requiresPublicSecurity()
      );
   }

   public static Object gasTank$getChemical(TileEntityAdvancedElectricMachine subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getStack(subject.gasTank));
   }

   public static Object gasTank$getChemicalCapacity(TileEntityAdvancedElectricMachine subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getCapacity(subject.gasTank));
   }

   public static Object gasTank$getChemicalNeeded(TileEntityAdvancedElectricMachine subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getNeeded(subject.gasTank));
   }

   public static Object gasTank$getChemicalFilledPercentage(TileEntityAdvancedElectricMachine subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getFilledPercentage(subject.gasTank));
   }

   public static Object inputSlot$getInput(TileEntityAdvancedElectricMachine subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.inputSlot));
   }

   public static Object outputSlot$getOutput(TileEntityAdvancedElectricMachine subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.outputSlot));
   }

   public static Object secondarySlot$getChemicalItem(TileEntityAdvancedElectricMachine subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.secondarySlot));
   }

   public static Object energySlot$getEnergyItem(TileEntityAdvancedElectricMachine subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.energySlot));
   }

   public static Object getEnergyUsage_0(TileEntityAdvancedElectricMachine subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getEnergyUsage());
   }

   public static Object dumpChemical_0(TileEntityAdvancedElectricMachine subject, BaseComputerHelper helper) throws ComputerException {
      subject.dumpChemical();
      return helper.voidResult();
   }
}
