package mekanism.common.tile.machine;

import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.MethodFactory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

@MethodFactory(
   target = TileEntityElectricPump.class
)
public class TileEntityElectricPump$ComputerHandler extends ComputerMethodFactory<TileEntityElectricPump> {
   public TileEntityElectricPump$ComputerHandler() {
      this.register(
         MethodData.builder("getFluid", TileEntityElectricPump$ComputerHandler::fluidTank$getFluid)
            .returnType(FluidStack.class)
            .methodDescription("Get the contents of the buffer tank.")
      );
      this.register(
         MethodData.builder("getFluidCapacity", TileEntityElectricPump$ComputerHandler::fluidTank$getFluidCapacity)
            .returnType(int.class)
            .methodDescription("Get the capacity of the buffer tank.")
      );
      this.register(
         MethodData.builder("getFluidNeeded", TileEntityElectricPump$ComputerHandler::fluidTank$getFluidNeeded)
            .returnType(int.class)
            .methodDescription("Get the amount needed to fill the buffer tank.")
      );
      this.register(
         MethodData.builder("getFluidFilledPercentage", TileEntityElectricPump$ComputerHandler::fluidTank$getFluidFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the buffer tank.")
      );
      this.register(
         MethodData.builder("getInputItem", TileEntityElectricPump$ComputerHandler::inputSlot$getInputItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the input slot.")
      );
      this.register(
         MethodData.builder("getOutputItem", TileEntityElectricPump$ComputerHandler::outputSlot$getOutputItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the output slot.")
      );
      this.register(
         MethodData.builder("getEnergyItem", TileEntityElectricPump$ComputerHandler::energySlot$getEnergyItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the energy slot.")
      );
      this.register(MethodData.builder("reset", TileEntityElectricPump$ComputerHandler::reset_0).requiresPublicSecurity());
   }

   public static Object fluidTank$getFluid(TileEntityElectricPump subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerFluidTankWrapper.getStack(subject.fluidTank));
   }

   public static Object fluidTank$getFluidCapacity(TileEntityElectricPump subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerFluidTankWrapper.getCapacity(subject.fluidTank));
   }

   public static Object fluidTank$getFluidNeeded(TileEntityElectricPump subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerFluidTankWrapper.getNeeded(subject.fluidTank));
   }

   public static Object fluidTank$getFluidFilledPercentage(TileEntityElectricPump subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerFluidTankWrapper.getFilledPercentage(subject.fluidTank));
   }

   public static Object inputSlot$getInputItem(TileEntityElectricPump subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.inputSlot));
   }

   public static Object outputSlot$getOutputItem(TileEntityElectricPump subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.outputSlot));
   }

   public static Object energySlot$getEnergyItem(TileEntityElectricPump subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.energySlot));
   }

   public static Object reset_0(TileEntityElectricPump subject, BaseComputerHelper helper) throws ComputerException {
      subject.resetPump();
      return helper.voidResult();
   }
}
