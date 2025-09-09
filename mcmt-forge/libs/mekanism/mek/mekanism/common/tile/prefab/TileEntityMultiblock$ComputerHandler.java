package mekanism.common.tile.prefab;

import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.MethodRestriction;
import mekanism.common.integration.computer.annotation.MethodFactory;

@MethodFactory(
   target = TileEntityMultiblock.class
)
public class TileEntityMultiblock$ComputerHandler extends ComputerMethodFactory<TileEntityMultiblock> {
   public TileEntityMultiblock$ComputerHandler() {
      this.register(
         MethodData.builder("isFormed", TileEntityMultiblock$ComputerHandler::isFormed_0).restriction(MethodRestriction.MULTIBLOCK).returnType(boolean.class)
      );
   }

   public static Object isFormed_0(TileEntityMultiblock subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.isFormed());
   }
}
