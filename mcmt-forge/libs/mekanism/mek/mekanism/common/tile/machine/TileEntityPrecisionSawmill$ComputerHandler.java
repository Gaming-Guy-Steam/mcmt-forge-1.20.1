package mekanism.common.tile.machine;

import mekanism.api.math.FloatingLong;
import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.MethodFactory;
import net.minecraft.world.item.ItemStack;

@MethodFactory(
   target = TileEntityPrecisionSawmill.class
)
public class TileEntityPrecisionSawmill$ComputerHandler extends ComputerMethodFactory<TileEntityPrecisionSawmill> {
   public TileEntityPrecisionSawmill$ComputerHandler() {
      this.register(
         MethodData.builder("getInput", TileEntityPrecisionSawmill$ComputerHandler::inputSlot$getInput)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the input slot.")
      );
      this.register(
         MethodData.builder("getOutput", TileEntityPrecisionSawmill$ComputerHandler::outputSlot$getOutput)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the output slot.")
      );
      this.register(
         MethodData.builder("getSecondaryOutput", TileEntityPrecisionSawmill$ComputerHandler::secondaryOutputSlot$getSecondaryOutput)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the secondary output slot.")
      );
      this.register(
         MethodData.builder("getEnergyItem", TileEntityPrecisionSawmill$ComputerHandler::energySlot$getEnergyItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the energy slot.")
      );
      this.register(
         MethodData.builder("getEnergyUsage", TileEntityPrecisionSawmill$ComputerHandler::getEnergyUsage_0)
            .returnType(FloatingLong.class)
            .methodDescription("Get the energy used in the last tick by the machine")
      );
   }

   public static Object inputSlot$getInput(TileEntityPrecisionSawmill subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.inputSlot));
   }

   public static Object outputSlot$getOutput(TileEntityPrecisionSawmill subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.outputSlot));
   }

   public static Object secondaryOutputSlot$getSecondaryOutput(TileEntityPrecisionSawmill subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.secondaryOutputSlot));
   }

   public static Object energySlot$getEnergyItem(TileEntityPrecisionSawmill subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.energySlot));
   }

   public static Object getEnergyUsage_0(TileEntityPrecisionSawmill subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getEnergyUsage());
   }
}
