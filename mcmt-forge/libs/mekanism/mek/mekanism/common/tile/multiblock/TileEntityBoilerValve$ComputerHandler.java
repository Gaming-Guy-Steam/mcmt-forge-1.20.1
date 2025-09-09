package mekanism.common.tile.multiblock;

import mekanism.common.block.attribute.AttributeStateBoilerValveMode;
import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.annotation.MethodFactory;

@MethodFactory(
   target = TileEntityBoilerValve.class
)
public class TileEntityBoilerValve$ComputerHandler extends ComputerMethodFactory<TileEntityBoilerValve> {
   private final String[] NAMES_mode = new String[]{"mode"};
   private final Class[] TYPES_b1665a9d = new Class[]{AttributeStateBoilerValveMode.BoilerValveMode.class};

   public TileEntityBoilerValve$ComputerHandler() {
      this.register(
         MethodData.builder("getMode", TileEntityBoilerValve$ComputerHandler::getMode_0)
            .returnType(AttributeStateBoilerValveMode.BoilerValveMode.class)
            .methodDescription("Get the current configuration of this valve")
      );
      this.register(
         MethodData.builder("setMode", TileEntityBoilerValve$ComputerHandler::setMode_1)
            .methodDescription("Change the configuration of this valve")
            .arguments(this.NAMES_mode, this.TYPES_b1665a9d)
      );
      this.register(
         MethodData.builder("incrementMode", TileEntityBoilerValve$ComputerHandler::incrementMode_0)
            .methodDescription("Toggle the current valve configuration to the next option in the list")
      );
      this.register(
         MethodData.builder("decrementMode", TileEntityBoilerValve$ComputerHandler::decrementMode_0)
            .methodDescription("Toggle the current valve configuration to the previous option in the list")
      );
   }

   public static Object getMode_0(TileEntityBoilerValve subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getMode());
   }

   public static Object setMode_1(TileEntityBoilerValve subject, BaseComputerHelper helper) throws ComputerException {
      subject.setMode(helper.getEnum(0, AttributeStateBoilerValveMode.BoilerValveMode.class));
      return helper.voidResult();
   }

   public static Object incrementMode_0(TileEntityBoilerValve subject, BaseComputerHelper helper) throws ComputerException {
      subject.incrementMode();
      return helper.voidResult();
   }

   public static Object decrementMode_0(TileEntityBoilerValve subject, BaseComputerHelper helper) throws ComputerException {
      subject.decrementMode();
      return helper.voidResult();
   }
}
