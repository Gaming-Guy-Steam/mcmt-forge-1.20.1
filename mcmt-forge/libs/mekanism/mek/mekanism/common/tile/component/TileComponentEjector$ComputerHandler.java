package mekanism.common.tile.component;

import mekanism.api.RelativeSide;
import mekanism.api.text.EnumColor;
import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.annotation.MethodFactory;

@MethodFactory(
   target = TileComponentEjector.class
)
public class TileComponentEjector$ComputerHandler extends ComputerMethodFactory<TileComponentEjector> {
   private final String[] NAMES_side_color = new String[]{"side", "color"};
   private final String[] NAMES_side = new String[]{"side"};
   private final String[] NAMES_color = new String[]{"color"};
   private final String[] NAMES_strict = new String[]{"strict"};
   private final Class[] TYPES_fd373121 = new Class[]{RelativeSide.class};
   private final Class[] TYPES_3db6c47 = new Class[]{boolean.class};
   private final Class[] TYPES_88e2323f = new Class[]{EnumColor.class};
   private final Class[] TYPES_3291251f = new Class[]{RelativeSide.class, EnumColor.class};

   public TileComponentEjector$ComputerHandler() {
      this.register(MethodData.builder("hasStrictInput", TileComponentEjector$ComputerHandler::hasStrictInput_0).returnType(boolean.class));
      this.register(MethodData.builder("getOutputColor", TileComponentEjector$ComputerHandler::getOutputColor_0).returnType(EnumColor.class));
      this.register(
         MethodData.builder("getInputColor", TileComponentEjector$ComputerHandler::getInputColor_1)
            .returnType(EnumColor.class)
            .arguments(this.NAMES_side, this.TYPES_fd373121)
      );
      this.register(
         MethodData.builder("setStrictInput", TileComponentEjector$ComputerHandler::setStrictInput_1)
            .requiresPublicSecurity()
            .arguments(this.NAMES_strict, this.TYPES_3db6c47)
      );
      this.register(
         MethodData.builder("clearInputColor", TileComponentEjector$ComputerHandler::clearInputColor_1)
            .requiresPublicSecurity()
            .arguments(this.NAMES_side, this.TYPES_fd373121)
      );
      this.register(
         MethodData.builder("incrementInputColor", TileComponentEjector$ComputerHandler::incrementInputColor_1)
            .requiresPublicSecurity()
            .arguments(this.NAMES_side, this.TYPES_fd373121)
      );
      this.register(
         MethodData.builder("decrementInputColor", TileComponentEjector$ComputerHandler::decrementInputColor_1)
            .requiresPublicSecurity()
            .arguments(this.NAMES_side, this.TYPES_fd373121)
      );
      this.register(
         MethodData.builder("setInputColor", TileComponentEjector$ComputerHandler::setInputColor_2)
            .requiresPublicSecurity()
            .arguments(this.NAMES_side_color, this.TYPES_3291251f)
      );
      this.register(MethodData.builder("clearOutputColor", TileComponentEjector$ComputerHandler::clearOutputColor_0).requiresPublicSecurity());
      this.register(MethodData.builder("incrementOutputColor", TileComponentEjector$ComputerHandler::incrementOutputColor_0).requiresPublicSecurity());
      this.register(MethodData.builder("decrementOutputColor", TileComponentEjector$ComputerHandler::decrementOutputColor_0).requiresPublicSecurity());
      this.register(
         MethodData.builder("setOutputColor", TileComponentEjector$ComputerHandler::setOutputColor_1)
            .requiresPublicSecurity()
            .arguments(this.NAMES_color, this.TYPES_88e2323f)
      );
   }

   public static Object hasStrictInput_0(TileComponentEjector subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.hasStrictInput());
   }

   public static Object getOutputColor_0(TileComponentEjector subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getOutputColor());
   }

   public static Object getInputColor_1(TileComponentEjector subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getInputColor(helper.getEnum(0, RelativeSide.class)));
   }

   public static Object setStrictInput_1(TileComponentEjector subject, BaseComputerHelper helper) throws ComputerException {
      subject.computerSetStrictInput(helper.getBoolean(0));
      return helper.voidResult();
   }

   public static Object clearInputColor_1(TileComponentEjector subject, BaseComputerHelper helper) throws ComputerException {
      subject.clearInputColor(helper.getEnum(0, RelativeSide.class));
      return helper.voidResult();
   }

   public static Object incrementInputColor_1(TileComponentEjector subject, BaseComputerHelper helper) throws ComputerException {
      subject.incrementInputColor(helper.getEnum(0, RelativeSide.class));
      return helper.voidResult();
   }

   public static Object decrementInputColor_1(TileComponentEjector subject, BaseComputerHelper helper) throws ComputerException {
      subject.decrementInputColor(helper.getEnum(0, RelativeSide.class));
      return helper.voidResult();
   }

   public static Object setInputColor_2(TileComponentEjector subject, BaseComputerHelper helper) throws ComputerException {
      subject.computerSetInputColor(helper.getEnum(0, RelativeSide.class), helper.getEnum(1, EnumColor.class));
      return helper.voidResult();
   }

   public static Object clearOutputColor_0(TileComponentEjector subject, BaseComputerHelper helper) throws ComputerException {
      subject.clearOutputColor();
      return helper.voidResult();
   }

   public static Object incrementOutputColor_0(TileComponentEjector subject, BaseComputerHelper helper) throws ComputerException {
      subject.incrementOutputColor();
      return helper.voidResult();
   }

   public static Object decrementOutputColor_0(TileComponentEjector subject, BaseComputerHelper helper) throws ComputerException {
      subject.decrementOutputColor();
      return helper.voidResult();
   }

   public static Object setOutputColor_1(TileComponentEjector subject, BaseComputerHelper helper) throws ComputerException {
      subject.computerSetOutputColor(helper.getEnum(0, EnumColor.class));
      return helper.voidResult();
   }
}
