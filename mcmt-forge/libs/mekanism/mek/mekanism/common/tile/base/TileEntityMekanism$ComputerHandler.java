package mekanism.common.tile.base;

import mekanism.api.math.FloatingLong;
import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.MethodRestriction;
import mekanism.common.integration.computer.annotation.MethodFactory;
import mekanism.common.tile.interfaces.IRedstoneControl;
import net.minecraft.core.Direction;

@MethodFactory(
   target = TileEntityMekanism.class
)
public class TileEntityMekanism$ComputerHandler extends ComputerMethodFactory<TileEntityMekanism> {
   private final String[] NAMES_type = new String[]{"type"};
   private final Class[] TYPES_930470e6 = new Class[]{IRedstoneControl.RedstoneControl.class};

   public TileEntityMekanism$ComputerHandler() {
      this.register(
         MethodData.builder("getDirection", TileEntityMekanism$ComputerHandler::getDirection_0)
            .restriction(MethodRestriction.DIRECTIONAL)
            .returnType(Direction.class)
      );
      this.register(
         MethodData.builder("getRedstoneMode", TileEntityMekanism$ComputerHandler::getRedstoneMode_0)
            .restriction(MethodRestriction.REDSTONE_CONTROL)
            .returnType(IRedstoneControl.RedstoneControl.class)
      );
      this.register(
         MethodData.builder("getComparatorLevel", TileEntityMekanism$ComputerHandler::getComparatorLevel_0)
            .restriction(MethodRestriction.COMPARATOR)
            .returnType(int.class)
      );
      this.register(
         MethodData.builder("getEnergy", TileEntityMekanism$ComputerHandler::getEnergy_0).restriction(MethodRestriction.ENERGY).returnType(FloatingLong.class)
      );
      this.register(
         MethodData.builder("getMaxEnergy", TileEntityMekanism$ComputerHandler::getMaxEnergy_0)
            .restriction(MethodRestriction.ENERGY)
            .returnType(FloatingLong.class)
      );
      this.register(
         MethodData.builder("getEnergyNeeded", TileEntityMekanism$ComputerHandler::getEnergyNeeded_0)
            .restriction(MethodRestriction.ENERGY)
            .returnType(FloatingLong.class)
      );
      this.register(
         MethodData.builder("getEnergyFilledPercentage", TileEntityMekanism$ComputerHandler::getEnergyFilledPercentage_0)
            .restriction(MethodRestriction.ENERGY)
            .returnType(double.class)
      );
      this.register(
         MethodData.builder("setRedstoneMode", TileEntityMekanism$ComputerHandler::setRedstoneMode_1)
            .restriction(MethodRestriction.REDSTONE_CONTROL)
            .requiresPublicSecurity()
            .arguments(this.NAMES_type, this.TYPES_930470e6)
      );
   }

   public static Object getDirection_0(TileEntityMekanism subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getDirection());
   }

   public static Object getRedstoneMode_0(TileEntityMekanism subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getControlType());
   }

   public static Object getComparatorLevel_0(TileEntityMekanism subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getCurrentRedstoneLevel());
   }

   public static Object getEnergy_0(TileEntityMekanism subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getTotalEnergy());
   }

   public static Object getMaxEnergy_0(TileEntityMekanism subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getTotalMaxEnergy());
   }

   public static Object getEnergyNeeded_0(TileEntityMekanism subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getTotalEnergyNeeded());
   }

   public static Object getEnergyFilledPercentage_0(TileEntityMekanism subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getTotalEnergyFilledPercentage());
   }

   public static Object setRedstoneMode_1(TileEntityMekanism subject, BaseComputerHelper helper) throws ComputerException {
      subject.setRedstoneMode(helper.getEnum(0, IRedstoneControl.RedstoneControl.class));
      return helper.voidResult();
   }
}
