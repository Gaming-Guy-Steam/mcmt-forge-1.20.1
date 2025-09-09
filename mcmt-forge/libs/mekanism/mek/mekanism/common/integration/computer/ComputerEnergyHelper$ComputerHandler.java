package mekanism.common.integration.computer;

import mekanism.api.math.FloatingLong;
import mekanism.common.integration.computer.annotation.MethodFactory;

@MethodFactory(
   target = ComputerEnergyHelper.class
)
public class ComputerEnergyHelper$ComputerHandler extends ComputerMethodFactory<ComputerEnergyHelper> {
   private final String[] NAMES_joules = new String[]{"joules"};
   private final String[] NAMES_fe = new String[]{"fe"};
   private final String[] NAMES_eu = new String[]{"eu"};
   private final Class[] TYPES_6a7f69c8 = new Class[]{FloatingLong.class};

   public ComputerEnergyHelper$ComputerHandler() {
      this.register(
         MethodData.builder("joulesToFE", ComputerEnergyHelper$ComputerHandler::joulesToFE_1)
            .returnType(FloatingLong.class)
            .methodDescription("Convert Mekanism Joules to Forge Energy")
            .arguments(this.NAMES_joules, this.TYPES_6a7f69c8)
      );
      this.register(
         MethodData.builder("feToJoules", ComputerEnergyHelper$ComputerHandler::feToJoules_1)
            .returnType(FloatingLong.class)
            .methodDescription("Convert Forge Energy to Mekanism Joules")
            .arguments(this.NAMES_fe, this.TYPES_6a7f69c8)
      );
      this.register(
         MethodData.builder("joulesToEU", ComputerEnergyHelper$ComputerHandler::joulesToEU_1)
            .requiredMods("ic2")
            .returnType(FloatingLong.class)
            .methodDescription("Convert Mekanism Joules to IC2 Energy Units")
            .arguments(this.NAMES_joules, this.TYPES_6a7f69c8)
      );
      this.register(
         MethodData.builder("euToJoules", ComputerEnergyHelper$ComputerHandler::euToJoules_1)
            .requiredMods("ic2")
            .returnType(FloatingLong.class)
            .methodDescription("Convert IC2 Energy Units to Mekanism Joules")
            .arguments(this.NAMES_eu, this.TYPES_6a7f69c8)
      );
   }

   public static Object joulesToFE_1(ComputerEnergyHelper subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(ComputerEnergyHelper.joulesToFE(helper.getFloatingLong(0)));
   }

   public static Object feToJoules_1(ComputerEnergyHelper subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(ComputerEnergyHelper.feToJoules(helper.getFloatingLong(0)));
   }

   public static Object joulesToEU_1(ComputerEnergyHelper subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(ComputerEnergyHelper.joulesToEU(helper.getFloatingLong(0)));
   }

   public static Object euToJoules_1(ComputerEnergyHelper subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(ComputerEnergyHelper.euToJoules(helper.getFloatingLong(0)));
   }
}
