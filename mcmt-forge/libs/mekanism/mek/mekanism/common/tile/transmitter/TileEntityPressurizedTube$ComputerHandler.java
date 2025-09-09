package mekanism.common.tile.transmitter;

import mekanism.api.chemical.ChemicalStack;
import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.annotation.MethodFactory;

@MethodFactory(
   target = TileEntityPressurizedTube.class
)
public class TileEntityPressurizedTube$ComputerHandler extends ComputerMethodFactory<TileEntityPressurizedTube> {
   public TileEntityPressurizedTube$ComputerHandler() {
      this.register(MethodData.builder("getBuffer", TileEntityPressurizedTube$ComputerHandler::getBuffer_0).returnType(ChemicalStack.class));
      this.register(MethodData.builder("getCapacity", TileEntityPressurizedTube$ComputerHandler::getCapacity_0).returnType(long.class));
      this.register(MethodData.builder("getNeeded", TileEntityPressurizedTube$ComputerHandler::getNeeded_0).returnType(long.class));
      this.register(MethodData.builder("getFilledPercentage", TileEntityPressurizedTube$ComputerHandler::getFilledPercentage_0).returnType(double.class));
   }

   public static Object getBuffer_0(TileEntityPressurizedTube subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getBuffer());
   }

   public static Object getCapacity_0(TileEntityPressurizedTube subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getCapacity());
   }

   public static Object getNeeded_0(TileEntityPressurizedTube subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getNeeded());
   }

   public static Object getFilledPercentage_0(TileEntityPressurizedTube subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getFilledPercentage());
   }
}
