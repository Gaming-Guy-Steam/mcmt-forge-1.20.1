package mekanism.common.tile.factory;

import mekanism.api.chemical.ChemicalStack;
import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.MethodFactory;
import net.minecraft.world.item.ItemStack;

@MethodFactory(
   target = TileEntityItemStackGasToItemStackFactory.class
)
public class TileEntityItemStackGasToItemStackFactory$ComputerHandler extends ComputerMethodFactory<TileEntityItemStackGasToItemStackFactory> {
   public TileEntityItemStackGasToItemStackFactory$ComputerHandler() {
      this.register(
         MethodData.builder("getChemicalItem", TileEntityItemStackGasToItemStackFactory$ComputerHandler::extraSlot$getChemicalItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the chemical item (extra) slot.")
      );
      this.register(
         MethodData.builder("getChemical", TileEntityItemStackGasToItemStackFactory$ComputerHandler::gasTank$getChemical)
            .returnType(ChemicalStack.class)
            .methodDescription("Get the contents of the gas tank.")
      );
      this.register(
         MethodData.builder("getChemicalCapacity", TileEntityItemStackGasToItemStackFactory$ComputerHandler::gasTank$getChemicalCapacity)
            .returnType(long.class)
            .methodDescription("Get the capacity of the gas tank.")
      );
      this.register(
         MethodData.builder("getChemicalNeeded", TileEntityItemStackGasToItemStackFactory$ComputerHandler::gasTank$getChemicalNeeded)
            .returnType(long.class)
            .methodDescription("Get the amount needed to fill the gas tank.")
      );
      this.register(
         MethodData.builder("getChemicalFilledPercentage", TileEntityItemStackGasToItemStackFactory$ComputerHandler::gasTank$getChemicalFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the gas tank.")
      );
      this.register(
         MethodData.builder("dumpChemical", TileEntityItemStackGasToItemStackFactory$ComputerHandler::dumpChemical_0)
            .methodDescription("Empty the contents of the gas tank into the environment")
            .requiresPublicSecurity()
      );
   }

   public static Object extraSlot$getChemicalItem(TileEntityItemStackGasToItemStackFactory subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.extraSlot));
   }

   public static Object gasTank$getChemical(TileEntityItemStackGasToItemStackFactory subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getStack(subject.gasTank));
   }

   public static Object gasTank$getChemicalCapacity(TileEntityItemStackGasToItemStackFactory subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getCapacity(subject.gasTank));
   }

   public static Object gasTank$getChemicalNeeded(TileEntityItemStackGasToItemStackFactory subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getNeeded(subject.gasTank));
   }

   public static Object gasTank$getChemicalFilledPercentage(TileEntityItemStackGasToItemStackFactory subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getFilledPercentage(subject.gasTank));
   }

   public static Object dumpChemical_0(TileEntityItemStackGasToItemStackFactory subject, BaseComputerHelper helper) throws ComputerException {
      subject.dumpChemical();
      return helper.voidResult();
   }
}
