package mekanism.common.tile.multiblock;

import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.annotation.MethodFactory;

@MethodFactory(
   target = TileEntitySPSPort.class
)
public class TileEntitySPSPort$ComputerHandler extends ComputerMethodFactory<TileEntitySPSPort> {
   private final String[] NAMES_output = new String[]{"output"};
   private final Class[] TYPES_3db6c47 = new Class[]{boolean.class};

   public TileEntitySPSPort$ComputerHandler() {
      this.register(
         MethodData.builder("getMode", TileEntitySPSPort$ComputerHandler::getMode_0)
            .returnType(boolean.class)
            .methodDescription("true -> output, false -> input.")
      );
      this.register(
         MethodData.builder("setMode", TileEntitySPSPort$ComputerHandler::setMode_1)
            .methodDescription("true -> output, false -> input.")
            .arguments(this.NAMES_output, this.TYPES_3db6c47)
      );
   }

   public static Object getMode_0(TileEntitySPSPort subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getMode());
   }

   public static Object setMode_1(TileEntitySPSPort subject, BaseComputerHelper helper) throws ComputerException {
      subject.setMode(helper.getBoolean(0));
      return helper.voidResult();
   }
}
