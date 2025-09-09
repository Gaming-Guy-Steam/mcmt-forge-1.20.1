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
   target = TileEntityPaintingMachine.class
)
public class TileEntityPaintingMachine$ComputerHandler extends ComputerMethodFactory<TileEntityPaintingMachine> {
   public TileEntityPaintingMachine$ComputerHandler() {
      this.register(
         MethodData.builder("getPigmentInput", TileEntityPaintingMachine$ComputerHandler::pigmentTank$getPigmentInput)
            .returnType(ChemicalStack.class)
            .methodDescription("Get the contents of the pigment tank.")
      );
      this.register(
         MethodData.builder("getPigmentInputCapacity", TileEntityPaintingMachine$ComputerHandler::pigmentTank$getPigmentInputCapacity)
            .returnType(long.class)
            .methodDescription("Get the capacity of the pigment tank.")
      );
      this.register(
         MethodData.builder("getPigmentInputNeeded", TileEntityPaintingMachine$ComputerHandler::pigmentTank$getPigmentInputNeeded)
            .returnType(long.class)
            .methodDescription("Get the amount needed to fill the pigment tank.")
      );
      this.register(
         MethodData.builder("getPigmentInputFilledPercentage", TileEntityPaintingMachine$ComputerHandler::pigmentTank$getPigmentInputFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the pigment tank.")
      );
      this.register(
         MethodData.builder("getInputPigmentItem", TileEntityPaintingMachine$ComputerHandler::pigmentInputSlot$getInputPigmentItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the pigment slot.")
      );
      this.register(
         MethodData.builder("getInputItem", TileEntityPaintingMachine$ComputerHandler::inputSlot$getInputItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the paintable item slot.")
      );
      this.register(
         MethodData.builder("getOutput", TileEntityPaintingMachine$ComputerHandler::outputSlot$getOutput)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the painted item slot.")
      );
      this.register(
         MethodData.builder("getEnergyItem", TileEntityPaintingMachine$ComputerHandler::energySlot$getEnergyItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the energy slot.")
      );
      this.register(
         MethodData.builder("getEnergyUsage", TileEntityPaintingMachine$ComputerHandler::getEnergyUsage_0)
            .returnType(FloatingLong.class)
            .methodDescription("Get the energy used in the last tick by the machine")
      );
   }

   public static Object pigmentTank$getPigmentInput(TileEntityPaintingMachine subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getStack(subject.pigmentTank));
   }

   public static Object pigmentTank$getPigmentInputCapacity(TileEntityPaintingMachine subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getCapacity(subject.pigmentTank));
   }

   public static Object pigmentTank$getPigmentInputNeeded(TileEntityPaintingMachine subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getNeeded(subject.pigmentTank));
   }

   public static Object pigmentTank$getPigmentInputFilledPercentage(TileEntityPaintingMachine subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getFilledPercentage(subject.pigmentTank));
   }

   public static Object pigmentInputSlot$getInputPigmentItem(TileEntityPaintingMachine subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.pigmentInputSlot));
   }

   public static Object inputSlot$getInputItem(TileEntityPaintingMachine subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.inputSlot));
   }

   public static Object outputSlot$getOutput(TileEntityPaintingMachine subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.outputSlot));
   }

   public static Object energySlot$getEnergyItem(TileEntityPaintingMachine subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.energySlot));
   }

   public static Object getEnergyUsage_0(TileEntityPaintingMachine subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getEnergyUsage());
   }
}
