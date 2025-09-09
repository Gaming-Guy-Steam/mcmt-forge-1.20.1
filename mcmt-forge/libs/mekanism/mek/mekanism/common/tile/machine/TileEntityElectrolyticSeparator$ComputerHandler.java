package mekanism.common.tile.machine;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.math.FloatingLong;
import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.MethodFactory;
import mekanism.common.tile.TileEntityChemicalTank;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

@MethodFactory(
   target = TileEntityElectrolyticSeparator.class
)
public class TileEntityElectrolyticSeparator$ComputerHandler extends ComputerMethodFactory<TileEntityElectrolyticSeparator> {
   private final String[] NAMES_mode = new String[]{"mode"};
   private final Class[] TYPES_ef806282 = new Class[]{TileEntityChemicalTank.GasMode.class};

   public TileEntityElectrolyticSeparator$ComputerHandler() {
      this.register(
         MethodData.builder("getInput", TileEntityElectrolyticSeparator$ComputerHandler::fluidTank$getInput)
            .returnType(FluidStack.class)
            .methodDescription("Get the contents of the input tank.")
      );
      this.register(
         MethodData.builder("getInputCapacity", TileEntityElectrolyticSeparator$ComputerHandler::fluidTank$getInputCapacity)
            .returnType(int.class)
            .methodDescription("Get the capacity of the input tank.")
      );
      this.register(
         MethodData.builder("getInputNeeded", TileEntityElectrolyticSeparator$ComputerHandler::fluidTank$getInputNeeded)
            .returnType(int.class)
            .methodDescription("Get the amount needed to fill the input tank.")
      );
      this.register(
         MethodData.builder("getInputFilledPercentage", TileEntityElectrolyticSeparator$ComputerHandler::fluidTank$getInputFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the input tank.")
      );
      this.register(
         MethodData.builder("getLeftOutput", TileEntityElectrolyticSeparator$ComputerHandler::leftTank$getLeftOutput)
            .returnType(ChemicalStack.class)
            .methodDescription("Get the contents of the left output tank.")
      );
      this.register(
         MethodData.builder("getLeftOutputCapacity", TileEntityElectrolyticSeparator$ComputerHandler::leftTank$getLeftOutputCapacity)
            .returnType(long.class)
            .methodDescription("Get the capacity of the left output tank.")
      );
      this.register(
         MethodData.builder("getLeftOutputNeeded", TileEntityElectrolyticSeparator$ComputerHandler::leftTank$getLeftOutputNeeded)
            .returnType(long.class)
            .methodDescription("Get the amount needed to fill the left output tank.")
      );
      this.register(
         MethodData.builder("getLeftOutputFilledPercentage", TileEntityElectrolyticSeparator$ComputerHandler::leftTank$getLeftOutputFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the left output tank.")
      );
      this.register(
         MethodData.builder("getRightOutput", TileEntityElectrolyticSeparator$ComputerHandler::rightTank$getRightOutput)
            .returnType(ChemicalStack.class)
            .methodDescription("Get the contents of the right output tank.")
      );
      this.register(
         MethodData.builder("getRightOutputCapacity", TileEntityElectrolyticSeparator$ComputerHandler::rightTank$getRightOutputCapacity)
            .returnType(long.class)
            .methodDescription("Get the capacity of the right output tank.")
      );
      this.register(
         MethodData.builder("getRightOutputNeeded", TileEntityElectrolyticSeparator$ComputerHandler::rightTank$getRightOutputNeeded)
            .returnType(long.class)
            .methodDescription("Get the amount needed to fill the right output tank.")
      );
      this.register(
         MethodData.builder("getRightOutputFilledPercentage", TileEntityElectrolyticSeparator$ComputerHandler::rightTank$getRightOutputFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the right output tank.")
      );
      this.register(
         MethodData.builder("getLeftOutputDumpingMode", TileEntityElectrolyticSeparator$ComputerHandler::getLeftOutputDumpingMode_0)
            .returnType(TileEntityChemicalTank.GasMode.class)
      );
      this.register(
         MethodData.builder("getRightOutputDumpingMode", TileEntityElectrolyticSeparator$ComputerHandler::getRightOutputDumpingMode_0)
            .returnType(TileEntityChemicalTank.GasMode.class)
      );
      this.register(
         MethodData.builder("getInputItem", TileEntityElectrolyticSeparator$ComputerHandler::fluidSlot$getInputItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the input item slot.")
      );
      this.register(
         MethodData.builder("getLeftOutputItem", TileEntityElectrolyticSeparator$ComputerHandler::leftOutputSlot$getLeftOutputItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the left output item slot.")
      );
      this.register(
         MethodData.builder("getRightOutputItem", TileEntityElectrolyticSeparator$ComputerHandler::rightOutputSlot$getRightOutputItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the right output item slot.")
      );
      this.register(
         MethodData.builder("getEnergyItem", TileEntityElectrolyticSeparator$ComputerHandler::energySlot$getEnergyItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the energy slot.")
      );
      this.register(
         MethodData.builder("getEnergyUsage", TileEntityElectrolyticSeparator$ComputerHandler::getEnergyUsage_0)
            .returnType(FloatingLong.class)
            .methodDescription("Get the energy used in the last tick by the machine")
      );
      this.register(
         MethodData.builder("setLeftOutputDumpingMode", TileEntityElectrolyticSeparator$ComputerHandler::setLeftOutputDumpingMode_1)
            .requiresPublicSecurity()
            .arguments(this.NAMES_mode, this.TYPES_ef806282)
      );
      this.register(
         MethodData.builder("incrementLeftOutputDumpingMode", TileEntityElectrolyticSeparator$ComputerHandler::incrementLeftOutputDumpingMode_0)
            .requiresPublicSecurity()
      );
      this.register(
         MethodData.builder("decrementLeftOutputDumpingMode", TileEntityElectrolyticSeparator$ComputerHandler::decrementLeftOutputDumpingMode_0)
            .requiresPublicSecurity()
      );
      this.register(
         MethodData.builder("setRightOutputDumpingMode", TileEntityElectrolyticSeparator$ComputerHandler::setRightOutputDumpingMode_1)
            .requiresPublicSecurity()
            .arguments(this.NAMES_mode, this.TYPES_ef806282)
      );
      this.register(
         MethodData.builder("incrementRightOutputDumpingMode", TileEntityElectrolyticSeparator$ComputerHandler::incrementRightOutputDumpingMode_0)
            .requiresPublicSecurity()
      );
      this.register(
         MethodData.builder("decrementRightOutputDumpingMode", TileEntityElectrolyticSeparator$ComputerHandler::decrementRightOutputDumpingMode_0)
            .requiresPublicSecurity()
      );
   }

   public static Object fluidTank$getInput(TileEntityElectrolyticSeparator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerFluidTankWrapper.getStack(subject.fluidTank));
   }

   public static Object fluidTank$getInputCapacity(TileEntityElectrolyticSeparator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerFluidTankWrapper.getCapacity(subject.fluidTank));
   }

   public static Object fluidTank$getInputNeeded(TileEntityElectrolyticSeparator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerFluidTankWrapper.getNeeded(subject.fluidTank));
   }

   public static Object fluidTank$getInputFilledPercentage(TileEntityElectrolyticSeparator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerFluidTankWrapper.getFilledPercentage(subject.fluidTank));
   }

   public static Object leftTank$getLeftOutput(TileEntityElectrolyticSeparator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getStack(subject.leftTank));
   }

   public static Object leftTank$getLeftOutputCapacity(TileEntityElectrolyticSeparator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getCapacity(subject.leftTank));
   }

   public static Object leftTank$getLeftOutputNeeded(TileEntityElectrolyticSeparator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getNeeded(subject.leftTank));
   }

   public static Object leftTank$getLeftOutputFilledPercentage(TileEntityElectrolyticSeparator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getFilledPercentage(subject.leftTank));
   }

   public static Object rightTank$getRightOutput(TileEntityElectrolyticSeparator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getStack(subject.rightTank));
   }

   public static Object rightTank$getRightOutputCapacity(TileEntityElectrolyticSeparator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getCapacity(subject.rightTank));
   }

   public static Object rightTank$getRightOutputNeeded(TileEntityElectrolyticSeparator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getNeeded(subject.rightTank));
   }

   public static Object rightTank$getRightOutputFilledPercentage(TileEntityElectrolyticSeparator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getFilledPercentage(subject.rightTank));
   }

   public static Object getLeftOutputDumpingMode_0(TileEntityElectrolyticSeparator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.dumpLeft);
   }

   public static Object getRightOutputDumpingMode_0(TileEntityElectrolyticSeparator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.dumpRight);
   }

   public static Object fluidSlot$getInputItem(TileEntityElectrolyticSeparator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.fluidSlot));
   }

   public static Object leftOutputSlot$getLeftOutputItem(TileEntityElectrolyticSeparator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.leftOutputSlot));
   }

   public static Object rightOutputSlot$getRightOutputItem(TileEntityElectrolyticSeparator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.rightOutputSlot));
   }

   public static Object energySlot$getEnergyItem(TileEntityElectrolyticSeparator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.energySlot));
   }

   public static Object getEnergyUsage_0(TileEntityElectrolyticSeparator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getEnergyUsed());
   }

   public static Object setLeftOutputDumpingMode_1(TileEntityElectrolyticSeparator subject, BaseComputerHelper helper) throws ComputerException {
      subject.setLeftOutputDumpingMode(helper.getEnum(0, TileEntityChemicalTank.GasMode.class));
      return helper.voidResult();
   }

   public static Object incrementLeftOutputDumpingMode_0(TileEntityElectrolyticSeparator subject, BaseComputerHelper helper) throws ComputerException {
      subject.incrementLeftOutputDumpingMode();
      return helper.voidResult();
   }

   public static Object decrementLeftOutputDumpingMode_0(TileEntityElectrolyticSeparator subject, BaseComputerHelper helper) throws ComputerException {
      subject.decrementLeftOutputDumpingMode();
      return helper.voidResult();
   }

   public static Object setRightOutputDumpingMode_1(TileEntityElectrolyticSeparator subject, BaseComputerHelper helper) throws ComputerException {
      subject.setRightOutputDumpingMode(helper.getEnum(0, TileEntityChemicalTank.GasMode.class));
      return helper.voidResult();
   }

   public static Object incrementRightOutputDumpingMode_0(TileEntityElectrolyticSeparator subject, BaseComputerHelper helper) throws ComputerException {
      subject.incrementRightOutputDumpingMode();
      return helper.voidResult();
   }

   public static Object decrementRightOutputDumpingMode_0(TileEntityElectrolyticSeparator subject, BaseComputerHelper helper) throws ComputerException {
      subject.decrementRightOutputDumpingMode();
      return helper.voidResult();
   }
}
