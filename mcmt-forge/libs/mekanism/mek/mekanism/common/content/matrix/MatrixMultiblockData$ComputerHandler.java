package mekanism.common.content.matrix;

import mekanism.api.math.FloatingLong;
import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.MethodFactory;
import net.minecraft.world.item.ItemStack;

@MethodFactory(
   target = MatrixMultiblockData.class
)
public class MatrixMultiblockData$ComputerHandler extends ComputerMethodFactory<MatrixMultiblockData> {
   public MatrixMultiblockData$ComputerHandler() {
      this.register(
         MethodData.builder("getInputItem", MatrixMultiblockData$ComputerHandler::energyInputSlot$getInputItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the input slot.")
      );
      this.register(
         MethodData.builder("getOutputItem", MatrixMultiblockData$ComputerHandler::energyOutputSlot$getOutputItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the output slot.")
      );
      this.register(MethodData.builder("getTransferCap", MatrixMultiblockData$ComputerHandler::getTransferCap_0).returnType(FloatingLong.class));
      this.register(MethodData.builder("getLastInput", MatrixMultiblockData$ComputerHandler::getLastInput_0).returnType(FloatingLong.class));
      this.register(MethodData.builder("getLastOutput", MatrixMultiblockData$ComputerHandler::getLastOutput_0).returnType(FloatingLong.class));
      this.register(MethodData.builder("getInstalledCells", MatrixMultiblockData$ComputerHandler::getInstalledCells_0).returnType(int.class));
      this.register(MethodData.builder("getInstalledProviders", MatrixMultiblockData$ComputerHandler::getInstalledProviders_0).returnType(int.class));
   }

   public static Object energyInputSlot$getInputItem(MatrixMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.energyInputSlot));
   }

   public static Object energyOutputSlot$getOutputItem(MatrixMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.energyOutputSlot));
   }

   public static Object getTransferCap_0(MatrixMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getTransferCap());
   }

   public static Object getLastInput_0(MatrixMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getLastInput());
   }

   public static Object getLastOutput_0(MatrixMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getLastOutput());
   }

   public static Object getInstalledCells_0(MatrixMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getCellCount());
   }

   public static Object getInstalledProviders_0(MatrixMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getProviderCount());
   }
}
