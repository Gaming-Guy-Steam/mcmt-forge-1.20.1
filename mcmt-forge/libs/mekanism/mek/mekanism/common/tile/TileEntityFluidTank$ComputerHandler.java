package mekanism.common.tile;

import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.MethodFactory;
import mekanism.common.tile.interfaces.IFluidContainerManager;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

@MethodFactory(
   target = TileEntityFluidTank.class
)
public class TileEntityFluidTank$ComputerHandler extends ComputerMethodFactory<TileEntityFluidTank> {
   private final String[] NAMES_mode = new String[]{"mode"};
   private final Class[] TYPES_f8347998 = new Class[]{IFluidContainerManager.ContainerEditMode.class};

   public TileEntityFluidTank$ComputerHandler() {
      this.register(
         MethodData.builder("getStored", TileEntityFluidTank$ComputerHandler::fluidTank$getStored)
            .returnType(FluidStack.class)
            .methodDescription("Get the contents of the tank.")
      );
      this.register(
         MethodData.builder("getCapacity", TileEntityFluidTank$ComputerHandler::fluidTank$getCapacity)
            .returnType(int.class)
            .methodDescription("Get the capacity of the tank.")
      );
      this.register(
         MethodData.builder("getNeeded", TileEntityFluidTank$ComputerHandler::fluidTank$getNeeded)
            .returnType(int.class)
            .methodDescription("Get the amount needed to fill the tank.")
      );
      this.register(
         MethodData.builder("getFilledPercentage", TileEntityFluidTank$ComputerHandler::fluidTank$getFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the tank.")
      );
      this.register(
         MethodData.builder("getInputItem", TileEntityFluidTank$ComputerHandler::inputSlot$getInputItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the input slot.")
      );
      this.register(
         MethodData.builder("getOutputItem", TileEntityFluidTank$ComputerHandler::outputSlot$getOutputItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the output slot.")
      );
      this.register(
         MethodData.builder("getContainerEditMode", TileEntityFluidTank$ComputerHandler::getContainerEditMode_0)
            .returnType(IFluidContainerManager.ContainerEditMode.class)
      );
      this.register(
         MethodData.builder("setContainerEditMode", TileEntityFluidTank$ComputerHandler::setContainerEditMode_1)
            .requiresPublicSecurity()
            .arguments(this.NAMES_mode, this.TYPES_f8347998)
      );
      this.register(
         MethodData.builder("incrementContainerEditMode", TileEntityFluidTank$ComputerHandler::incrementContainerEditMode_0).requiresPublicSecurity()
      );
      this.register(
         MethodData.builder("decrementContainerEditMode", TileEntityFluidTank$ComputerHandler::decrementContainerEditMode_0).requiresPublicSecurity()
      );
   }

   public static Object fluidTank$getStored(TileEntityFluidTank subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerFluidTankWrapper.getStack(subject.fluidTank));
   }

   public static Object fluidTank$getCapacity(TileEntityFluidTank subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerFluidTankWrapper.getCapacity(subject.fluidTank));
   }

   public static Object fluidTank$getNeeded(TileEntityFluidTank subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerFluidTankWrapper.getNeeded(subject.fluidTank));
   }

   public static Object fluidTank$getFilledPercentage(TileEntityFluidTank subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerFluidTankWrapper.getFilledPercentage(subject.fluidTank));
   }

   public static Object inputSlot$getInputItem(TileEntityFluidTank subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.inputSlot));
   }

   public static Object outputSlot$getOutputItem(TileEntityFluidTank subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.outputSlot));
   }

   public static Object getContainerEditMode_0(TileEntityFluidTank subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getContainerEditMode());
   }

   public static Object setContainerEditMode_1(TileEntityFluidTank subject, BaseComputerHelper helper) throws ComputerException {
      subject.setContainerEditMode(helper.getEnum(0, IFluidContainerManager.ContainerEditMode.class));
      return helper.voidResult();
   }

   public static Object incrementContainerEditMode_0(TileEntityFluidTank subject, BaseComputerHelper helper) throws ComputerException {
      subject.incrementContainerEditMode();
      return helper.voidResult();
   }

   public static Object decrementContainerEditMode_0(TileEntityFluidTank subject, BaseComputerHelper helper) throws ComputerException {
      subject.decrementContainerEditMode();
      return helper.voidResult();
   }
}
