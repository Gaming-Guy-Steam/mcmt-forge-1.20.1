package mekanism.common.tile.component;

import java.util.List;
import java.util.Set;
import mekanism.api.RelativeSide;
import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.annotation.MethodFactory;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.config.DataType;

@MethodFactory(
   target = TileComponentConfig.class
)
public class TileComponentConfig$ComputerHandler extends ComputerMethodFactory<TileComponentConfig> {
   private final String[] NAMES_type_ejecting = new String[]{"type", "ejecting"};
   private final String[] NAMES_type_side_mode = new String[]{"type", "side", "mode"};
   private final String[] NAMES_type_side = new String[]{"type", "side"};
   private final String[] NAMES_type = new String[]{"type"};
   private final Class[] TYPES_427106b0 = new Class[]{TransmissionType.class, RelativeSide.class, DataType.class};
   private final Class[] TYPES_82280b1b = new Class[]{TransmissionType.class};
   private final Class[] TYPES_c0108947 = new Class[]{TransmissionType.class, RelativeSide.class};
   private final Class[] TYPES_c6b4c46d = new Class[]{TransmissionType.class, boolean.class};

   public TileComponentConfig$ComputerHandler() {
      this.register(
         MethodData.builder("getConfigurableTypes", TileComponentConfig$ComputerHandler::getConfigurableTypes_0)
            .returnType(List.class)
            .returnExtra(TransmissionType.class)
      );
      this.register(
         MethodData.builder("canEject", TileComponentConfig$ComputerHandler::canEject_1)
            .returnType(boolean.class)
            .arguments(this.NAMES_type, this.TYPES_82280b1b)
      );
      this.register(
         MethodData.builder("isEjecting", TileComponentConfig$ComputerHandler::isEjecting_1)
            .returnType(boolean.class)
            .arguments(this.NAMES_type, this.TYPES_82280b1b)
      );
      this.register(
         MethodData.builder("setEjecting", TileComponentConfig$ComputerHandler::setEjecting_2)
            .requiresPublicSecurity()
            .arguments(this.NAMES_type_ejecting, this.TYPES_c6b4c46d)
      );
      this.register(
         MethodData.builder("getSupportedModes", TileComponentConfig$ComputerHandler::getSupportedModes_1)
            .returnType(Set.class)
            .returnExtra(DataType.class)
            .requiresPublicSecurity()
            .arguments(this.NAMES_type, this.TYPES_82280b1b)
      );
      this.register(
         MethodData.builder("getMode", TileComponentConfig$ComputerHandler::getMode_2)
            .returnType(DataType.class)
            .requiresPublicSecurity()
            .arguments(this.NAMES_type_side, this.TYPES_c0108947)
      );
      this.register(
         MethodData.builder("setMode", TileComponentConfig$ComputerHandler::setMode_3)
            .requiresPublicSecurity()
            .arguments(this.NAMES_type_side_mode, this.TYPES_427106b0)
      );
      this.register(
         MethodData.builder("incrementMode", TileComponentConfig$ComputerHandler::incrementMode_2)
            .requiresPublicSecurity()
            .arguments(this.NAMES_type_side, this.TYPES_c0108947)
      );
      this.register(
         MethodData.builder("decrementMode", TileComponentConfig$ComputerHandler::decrementMode_2)
            .requiresPublicSecurity()
            .arguments(this.NAMES_type_side, this.TYPES_c0108947)
      );
   }

   public static Object getConfigurableTypes_0(TileComponentConfig subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getTransmissions(), helper::convert);
   }

   public static Object canEject_1(TileComponentConfig subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.canEject(helper.getEnum(0, TransmissionType.class)));
   }

   public static Object isEjecting_1(TileComponentConfig subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.isEjecting(helper.getEnum(0, TransmissionType.class)));
   }

   public static Object setEjecting_2(TileComponentConfig subject, BaseComputerHelper helper) throws ComputerException {
      subject.setEjecting(helper.getEnum(0, TransmissionType.class), helper.getBoolean(1));
      return helper.voidResult();
   }

   public static Object getSupportedModes_1(TileComponentConfig subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getSupportedModes(helper.getEnum(0, TransmissionType.class)), helper::convert);
   }

   public static Object getMode_2(TileComponentConfig subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getMode(helper.getEnum(0, TransmissionType.class), helper.getEnum(1, RelativeSide.class)));
   }

   public static Object setMode_3(TileComponentConfig subject, BaseComputerHelper helper) throws ComputerException {
      subject.setMode(helper.getEnum(0, TransmissionType.class), helper.getEnum(1, RelativeSide.class), helper.getEnum(2, DataType.class));
      return helper.voidResult();
   }

   public static Object incrementMode_2(TileComponentConfig subject, BaseComputerHelper helper) throws ComputerException {
      subject.incrementMode(helper.getEnum(0, TransmissionType.class), helper.getEnum(1, RelativeSide.class));
      return helper.voidResult();
   }

   public static Object decrementMode_2(TileComponentConfig subject, BaseComputerHelper helper) throws ComputerException {
      subject.decrementMode(helper.getEnum(0, TransmissionType.class), helper.getEnum(1, RelativeSide.class));
      return helper.voidResult();
   }
}
