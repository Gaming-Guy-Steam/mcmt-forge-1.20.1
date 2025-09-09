package mekanism.common.tile.transmitter;

import mekanism.api.math.FloatingLong;
import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.annotation.MethodFactory;

@MethodFactory(
   target = TileEntityUniversalCable.class
)
public class TileEntityUniversalCable$ComputerHandler extends ComputerMethodFactory<TileEntityUniversalCable> {
   public TileEntityUniversalCable$ComputerHandler() {
      this.register(MethodData.builder("getBuffer", TileEntityUniversalCable$ComputerHandler::getBuffer_0).returnType(FloatingLong.class));
      this.register(MethodData.builder("getCapacity", TileEntityUniversalCable$ComputerHandler::getCapacity_0).returnType(FloatingLong.class));
      this.register(MethodData.builder("getNeeded", TileEntityUniversalCable$ComputerHandler::getNeeded_0).returnType(FloatingLong.class));
      this.register(MethodData.builder("getFilledPercentage", TileEntityUniversalCable$ComputerHandler::getFilledPercentage_0).returnType(double.class));
   }

   public static Object getBuffer_0(TileEntityUniversalCable subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getBuffer());
   }

   public static Object getCapacity_0(TileEntityUniversalCable subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getCapacity());
   }

   public static Object getNeeded_0(TileEntityUniversalCable subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getNeeded());
   }

   public static Object getFilledPercentage_0(TileEntityUniversalCable subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getFilledPercentage());
   }
}
