package mekanism.common.tile.transmitter;

import mekanism.common.content.network.transmitter.DiversionTransporter;
import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.annotation.MethodFactory;
import net.minecraft.core.Direction;

@MethodFactory(
   target = TileEntityDiversionTransporter.class
)
public class TileEntityDiversionTransporter$ComputerHandler extends ComputerMethodFactory<TileEntityDiversionTransporter> {
   private final String[] NAMES_side = new String[]{"side"};
   private final String[] NAMES_side_mode = new String[]{"side", "mode"};
   private final Class[] TYPES_f3f828ca = new Class[]{Direction.class, DiversionTransporter.DiversionControl.class};
   private final Class[] TYPES_64e4a581 = new Class[]{Direction.class};

   public TileEntityDiversionTransporter$ComputerHandler() {
      this.register(
         MethodData.builder("getMode", TileEntityDiversionTransporter$ComputerHandler::getMode_1)
            .returnType(DiversionTransporter.DiversionControl.class)
            .arguments(this.NAMES_side, this.TYPES_64e4a581)
      );
      this.register(
         MethodData.builder("setMode", TileEntityDiversionTransporter$ComputerHandler::setMode_2).arguments(this.NAMES_side_mode, this.TYPES_f3f828ca)
      );
      this.register(
         MethodData.builder("incrementMode", TileEntityDiversionTransporter$ComputerHandler::incrementMode_1).arguments(this.NAMES_side, this.TYPES_64e4a581)
      );
      this.register(
         MethodData.builder("decrementMode", TileEntityDiversionTransporter$ComputerHandler::decrementMode_1).arguments(this.NAMES_side, this.TYPES_64e4a581)
      );
   }

   public static Object getMode_1(TileEntityDiversionTransporter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getMode(helper.getEnum(0, Direction.class)));
   }

   public static Object setMode_2(TileEntityDiversionTransporter subject, BaseComputerHelper helper) throws ComputerException {
      subject.setMode(helper.getEnum(0, Direction.class), helper.getEnum(1, DiversionTransporter.DiversionControl.class));
      return helper.voidResult();
   }

   public static Object incrementMode_1(TileEntityDiversionTransporter subject, BaseComputerHelper helper) throws ComputerException {
      subject.incrementMode(helper.getEnum(0, Direction.class));
      return helper.voidResult();
   }

   public static Object decrementMode_1(TileEntityDiversionTransporter subject, BaseComputerHelper helper) throws ComputerException {
      subject.decrementMode(helper.getEnum(0, Direction.class));
      return helper.voidResult();
   }
}
