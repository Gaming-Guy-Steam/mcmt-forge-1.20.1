package mekanism.common.tile.machine;

import mekanism.api.math.FloatingLong;
import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.MethodFactory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

@MethodFactory(
   target = TileEntityNutritionalLiquifier.class
)
public class TileEntityNutritionalLiquifier$ComputerHandler extends ComputerMethodFactory<TileEntityNutritionalLiquifier> {
   public TileEntityNutritionalLiquifier$ComputerHandler() {
      this.register(
         MethodData.builder("getOutput", TileEntityNutritionalLiquifier$ComputerHandler::fluidTank$getOutput)
            .returnType(FluidStack.class)
            .methodDescription("Get the contents of the output tank.")
      );
      this.register(
         MethodData.builder("getOutputCapacity", TileEntityNutritionalLiquifier$ComputerHandler::fluidTank$getOutputCapacity)
            .returnType(int.class)
            .methodDescription("Get the capacity of the output tank.")
      );
      this.register(
         MethodData.builder("getOutputNeeded", TileEntityNutritionalLiquifier$ComputerHandler::fluidTank$getOutputNeeded)
            .returnType(int.class)
            .methodDescription("Get the amount needed to fill the output tank.")
      );
      this.register(
         MethodData.builder("getOutputFilledPercentage", TileEntityNutritionalLiquifier$ComputerHandler::fluidTank$getOutputFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the output tank.")
      );
      this.register(
         MethodData.builder("getInput", TileEntityNutritionalLiquifier$ComputerHandler::inputSlot$getInput)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the input slot.")
      );
      this.register(
         MethodData.builder("getContainerFillItem", TileEntityNutritionalLiquifier$ComputerHandler::containerFillSlot$getContainerFillItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the fillable container slot.")
      );
      this.register(
         MethodData.builder("getOutputItem", TileEntityNutritionalLiquifier$ComputerHandler::outputSlot$getOutputItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the filled container output slot.")
      );
      this.register(
         MethodData.builder("getEnergyItem", TileEntityNutritionalLiquifier$ComputerHandler::energySlot$getEnergyItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the energy slot.")
      );
      this.register(
         MethodData.builder("getEnergyUsage", TileEntityNutritionalLiquifier$ComputerHandler::getEnergyUsage_0)
            .returnType(FloatingLong.class)
            .methodDescription("Get the energy used in the last tick by the machine")
      );
   }

   public static Object fluidTank$getOutput(TileEntityNutritionalLiquifier subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerFluidTankWrapper.getStack(subject.fluidTank));
   }

   public static Object fluidTank$getOutputCapacity(TileEntityNutritionalLiquifier subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerFluidTankWrapper.getCapacity(subject.fluidTank));
   }

   public static Object fluidTank$getOutputNeeded(TileEntityNutritionalLiquifier subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerFluidTankWrapper.getNeeded(subject.fluidTank));
   }

   public static Object fluidTank$getOutputFilledPercentage(TileEntityNutritionalLiquifier subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerFluidTankWrapper.getFilledPercentage(subject.fluidTank));
   }

   public static Object inputSlot$getInput(TileEntityNutritionalLiquifier subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.inputSlot));
   }

   public static Object containerFillSlot$getContainerFillItem(TileEntityNutritionalLiquifier subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.containerFillSlot));
   }

   public static Object outputSlot$getOutputItem(TileEntityNutritionalLiquifier subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.outputSlot));
   }

   public static Object energySlot$getEnergyItem(TileEntityNutritionalLiquifier subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.energySlot));
   }

   public static Object getEnergyUsage_0(TileEntityNutritionalLiquifier subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getEnergyUsage());
   }
}
