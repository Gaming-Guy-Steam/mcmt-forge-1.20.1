package mekanism.common.tile;

import mekanism.api.chemical.ChemicalStack;
import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.MethodFactory;

@MethodFactory(
   target = TileEntityRadioactiveWasteBarrel.class
)
public class TileEntityRadioactiveWasteBarrel$ComputerHandler extends ComputerMethodFactory<TileEntityRadioactiveWasteBarrel> {
   public TileEntityRadioactiveWasteBarrel$ComputerHandler() {
      this.register(
         MethodData.builder("getStored", TileEntityRadioactiveWasteBarrel$ComputerHandler::gasTank$getStored)
            .returnType(ChemicalStack.class)
            .methodDescription("Get the contents of the barrel.")
      );
      this.register(
         MethodData.builder("getCapacity", TileEntityRadioactiveWasteBarrel$ComputerHandler::gasTank$getCapacity)
            .returnType(long.class)
            .methodDescription("Get the capacity of the barrel.")
      );
      this.register(
         MethodData.builder("getNeeded", TileEntityRadioactiveWasteBarrel$ComputerHandler::gasTank$getNeeded)
            .returnType(long.class)
            .methodDescription("Get the amount needed to fill the barrel.")
      );
      this.register(
         MethodData.builder("getFilledPercentage", TileEntityRadioactiveWasteBarrel$ComputerHandler::gasTank$getFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the barrel.")
      );
   }

   public static Object gasTank$getStored(TileEntityRadioactiveWasteBarrel subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getStack(subject.gasTank));
   }

   public static Object gasTank$getCapacity(TileEntityRadioactiveWasteBarrel subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getCapacity(subject.gasTank));
   }

   public static Object gasTank$getNeeded(TileEntityRadioactiveWasteBarrel subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getNeeded(subject.gasTank));
   }

   public static Object gasTank$getFilledPercentage(TileEntityRadioactiveWasteBarrel subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getFilledPercentage(subject.gasTank));
   }
}
