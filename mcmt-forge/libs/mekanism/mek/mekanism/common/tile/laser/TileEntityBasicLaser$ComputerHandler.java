package mekanism.common.tile.laser;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.WrongMethodTypeException;
import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.annotation.MethodFactory;
import net.minecraft.core.BlockPos;

@MethodFactory(
   target = TileEntityBasicLaser.class
)
public class TileEntityBasicLaser$ComputerHandler extends ComputerMethodFactory<TileEntityBasicLaser> {
   private static MethodHandle fieldGetter$digging = getGetterHandle(TileEntityBasicLaser.class, "digging");

   public TileEntityBasicLaser$ComputerHandler() {
      this.register(MethodData.builder("getDiggingPos", TileEntityBasicLaser$ComputerHandler::getDiggingPos_0).returnType(BlockPos.class));
   }

   private static BlockPos getter$digging(TileEntityBasicLaser subject) {
      try {
         return (BlockPos)fieldGetter$digging.invokeExact((TileEntityBasicLaser)subject);
      } catch (WrongMethodTypeException var2) {
         throw new RuntimeException("Getter not bound correctly", var2);
      } catch (Throwable var3) {
         throw new RuntimeException(var3.getMessage(), var3);
      }
   }

   public static Object getDiggingPos_0(TileEntityBasicLaser subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(getter$digging(subject));
   }
}
