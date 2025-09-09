package mekanism.common.tile.prefab;

import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.annotation.MethodFactory;

@MethodFactory(
   target = TileEntityProgressMachine.class
)
public class TileEntityProgressMachine$ComputerHandler extends ComputerMethodFactory<TileEntityProgressMachine> {
   public TileEntityProgressMachine$ComputerHandler() {
      this.register(MethodData.builder("getRecipeProgress", TileEntityProgressMachine$ComputerHandler::getRecipeProgress_0).returnType(int.class));
      this.register(MethodData.builder("getTicksRequired", TileEntityProgressMachine$ComputerHandler::getTicksRequired_0).returnType(int.class));
   }

   public static Object getRecipeProgress_0(TileEntityProgressMachine subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getOperatingTicks());
   }

   public static Object getTicksRequired_0(TileEntityProgressMachine subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getTicksRequired());
   }
}
