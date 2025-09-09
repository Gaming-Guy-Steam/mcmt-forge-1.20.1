package mekanism.common.tile.transmitter;

import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.annotation.MethodFactory;
import net.minecraftforge.fluids.FluidStack;

@MethodFactory(
   target = TileEntityMechanicalPipe.class
)
public class TileEntityMechanicalPipe$ComputerHandler extends ComputerMethodFactory<TileEntityMechanicalPipe> {
   public TileEntityMechanicalPipe$ComputerHandler() {
      this.register(MethodData.builder("getBuffer", TileEntityMechanicalPipe$ComputerHandler::getBuffer_0).returnType(FluidStack.class));
      this.register(MethodData.builder("getCapacity", TileEntityMechanicalPipe$ComputerHandler::getCapacity_0).returnType(long.class));
      this.register(MethodData.builder("getNeeded", TileEntityMechanicalPipe$ComputerHandler::getNeeded_0).returnType(long.class));
      this.register(MethodData.builder("getFilledPercentage", TileEntityMechanicalPipe$ComputerHandler::getFilledPercentage_0).returnType(double.class));
   }

   public static Object getBuffer_0(TileEntityMechanicalPipe subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getBuffer());
   }

   public static Object getCapacity_0(TileEntityMechanicalPipe subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getCapacity());
   }

   public static Object getNeeded_0(TileEntityMechanicalPipe subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getNeeded());
   }

   public static Object getFilledPercentage_0(TileEntityMechanicalPipe subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getFilledPercentage());
   }
}
