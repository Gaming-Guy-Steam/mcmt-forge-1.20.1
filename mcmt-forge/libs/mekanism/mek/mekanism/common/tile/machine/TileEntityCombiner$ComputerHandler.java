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
   target = TileEntityCombiner.class
)
public class TileEntityCombiner$ComputerHandler extends ComputerMethodFactory<TileEntityCombiner> {
   public TileEntityCombiner$ComputerHandler() {
      this.register(
         MethodData.builder("getMainInput", TileEntityCombiner$ComputerHandler::mainInputSlot$getMainInput)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the main input slot.")
      );
      this.register(
         MethodData.builder("getSecondaryInput", TileEntityCombiner$ComputerHandler::extraInputSlot$getSecondaryInput)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the secondary input slot.")
      );
      this.register(
         MethodData.builder("getOutput", TileEntityCombiner$ComputerHandler::outputSlot$getOutput)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the output slot.")
      );
      this.register(
         MethodData.builder("getEnergyItem", TileEntityCombiner$ComputerHandler::energySlot$getEnergyItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the energy slot.")
      );
      this.register(
         MethodData.builder("getEnergyUsage", TileEntityCombiner$ComputerHandler::getEnergyUsage_0)
            .returnType(FloatingLong.class)
            .methodDescription("Get the energy used in the last tick by the machine")
      );
   }

   public static Object mainInputSlot$getMainInput(TileEntityCombiner subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.mainInputSlot));
   }

   public static Object extraInputSlot$getSecondaryInput(TileEntityCombiner subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.extraInputSlot));
   }

   public static Object outputSlot$getOutput(TileEntityCombiner subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.outputSlot));
   }

   public static Object energySlot$getEnergyItem(TileEntityCombiner subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.energySlot));
   }

   public static Object getEnergyUsage_0(TileEntityCombiner subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getEnergyUsage());
   }
}
