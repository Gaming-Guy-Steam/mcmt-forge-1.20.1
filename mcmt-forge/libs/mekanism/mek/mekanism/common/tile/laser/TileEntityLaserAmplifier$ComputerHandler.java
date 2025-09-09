package mekanism.common.tile.laser;

import mekanism.api.math.FloatingLong;
import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.annotation.MethodFactory;

@MethodFactory(
   target = TileEntityLaserAmplifier.class
)
public class TileEntityLaserAmplifier$ComputerHandler extends ComputerMethodFactory<TileEntityLaserAmplifier> {
   private final String[] NAMES_mode = new String[]{"mode"};
   private final String[] NAMES_delay = new String[]{"delay"};
   private final String[] NAMES_threshold = new String[]{"threshold"};
   private final Class[] TYPES_6a7f69c8 = new Class[]{FloatingLong.class};
   private final Class[] TYPES_737a6ba0 = new Class[]{TileEntityLaserAmplifier.RedstoneOutput.class};
   private final Class[] TYPES_1980e = new Class[]{int.class};

   public TileEntityLaserAmplifier$ComputerHandler() {
      this.register(
         MethodData.builder("getRedstoneOutputMode", TileEntityLaserAmplifier$ComputerHandler::getRedstoneOutputMode_0)
            .returnType(TileEntityLaserAmplifier.RedstoneOutput.class)
      );
      this.register(MethodData.builder("getDelay", TileEntityLaserAmplifier$ComputerHandler::getDelay_0).returnType(int.class));
      this.register(MethodData.builder("getMinThreshold", TileEntityLaserAmplifier$ComputerHandler::getMinThreshold_0).returnType(FloatingLong.class));
      this.register(MethodData.builder("getMaxThreshold", TileEntityLaserAmplifier$ComputerHandler::getMaxThreshold_0).returnType(FloatingLong.class));
      this.register(
         MethodData.builder("setRedstoneOutputMode", TileEntityLaserAmplifier$ComputerHandler::setRedstoneOutputMode_1)
            .requiresPublicSecurity()
            .arguments(this.NAMES_mode, this.TYPES_737a6ba0)
      );
      this.register(
         MethodData.builder("setDelay", TileEntityLaserAmplifier$ComputerHandler::setDelay_1)
            .requiresPublicSecurity()
            .arguments(this.NAMES_delay, this.TYPES_1980e)
      );
      this.register(
         MethodData.builder("setMinThreshold", TileEntityLaserAmplifier$ComputerHandler::setMinThreshold_1)
            .requiresPublicSecurity()
            .arguments(this.NAMES_threshold, this.TYPES_6a7f69c8)
      );
      this.register(
         MethodData.builder("setMaxThreshold", TileEntityLaserAmplifier$ComputerHandler::setMaxThreshold_1)
            .requiresPublicSecurity()
            .arguments(this.NAMES_threshold, this.TYPES_6a7f69c8)
      );
   }

   public static Object getRedstoneOutputMode_0(TileEntityLaserAmplifier subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getOutputMode());
   }

   public static Object getDelay_0(TileEntityLaserAmplifier subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getDelay());
   }

   public static Object getMinThreshold_0(TileEntityLaserAmplifier subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getMinThreshold());
   }

   public static Object getMaxThreshold_0(TileEntityLaserAmplifier subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getMaxThreshold());
   }

   public static Object setRedstoneOutputMode_1(TileEntityLaserAmplifier subject, BaseComputerHelper helper) throws ComputerException {
      subject.setRedstoneOutputMode(helper.getEnum(0, TileEntityLaserAmplifier.RedstoneOutput.class));
      return helper.voidResult();
   }

   public static Object setDelay_1(TileEntityLaserAmplifier subject, BaseComputerHelper helper) throws ComputerException {
      subject.computerSetDelay(helper.getInt(0));
      return helper.voidResult();
   }

   public static Object setMinThreshold_1(TileEntityLaserAmplifier subject, BaseComputerHelper helper) throws ComputerException {
      subject.setMinThreshold(helper.getFloatingLong(0));
      return helper.voidResult();
   }

   public static Object setMaxThreshold_1(TileEntityLaserAmplifier subject, BaseComputerHelper helper) throws ComputerException {
      subject.setMaxThreshold(helper.getFloatingLong(0));
      return helper.voidResult();
   }
}
