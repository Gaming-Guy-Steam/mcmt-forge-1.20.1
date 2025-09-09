package mekanism.common.lib.multiblock;

import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.annotation.MethodFactory;
import net.minecraft.core.BlockPos;

@MethodFactory(
   target = MultiblockData.class
)
public class MultiblockData$ComputerHandler extends ComputerMethodFactory<MultiblockData> {
   public MultiblockData$ComputerHandler() {
      this.register(MethodData.builder("getLength", MultiblockData$ComputerHandler::getLength_0).returnType(int.class));
      this.register(MethodData.builder("getWidth", MultiblockData$ComputerHandler::getWidth_0).returnType(int.class));
      this.register(MethodData.builder("getHeight", MultiblockData$ComputerHandler::getHeight_0).returnType(int.class));
      this.register(MethodData.builder("getMinPos", MultiblockData$ComputerHandler::getMinPos_0).returnType(BlockPos.class));
      this.register(MethodData.builder("getMaxPos", MultiblockData$ComputerHandler::getMaxPos_0).returnType(BlockPos.class));
   }

   public static Object getLength_0(MultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.length());
   }

   public static Object getWidth_0(MultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.width());
   }

   public static Object getHeight_0(MultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.height());
   }

   public static Object getMinPos_0(MultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getMinPos());
   }

   public static Object getMaxPos_0(MultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getMaxPos());
   }
}
