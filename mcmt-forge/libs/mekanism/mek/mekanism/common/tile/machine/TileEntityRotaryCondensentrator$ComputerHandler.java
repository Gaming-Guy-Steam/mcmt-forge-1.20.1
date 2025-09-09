package mekanism.common.tile.machine;

import mekanism.api.chemical.ChemicalStack;
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
   target = TileEntityRotaryCondensentrator.class
)
public class TileEntityRotaryCondensentrator$ComputerHandler extends ComputerMethodFactory<TileEntityRotaryCondensentrator> {
   private final String[] NAMES_value = new String[]{"value"};
   private final Class[] TYPES_3db6c47 = new Class[]{boolean.class};

   public TileEntityRotaryCondensentrator$ComputerHandler() {
      this.register(
         MethodData.builder("getGas", TileEntityRotaryCondensentrator$ComputerHandler::gasTank$getGas)
            .returnType(ChemicalStack.class)
            .methodDescription("Get the contents of the gas tank.")
      );
      this.register(
         MethodData.builder("getGasCapacity", TileEntityRotaryCondensentrator$ComputerHandler::gasTank$getGasCapacity)
            .returnType(long.class)
            .methodDescription("Get the capacity of the gas tank.")
      );
      this.register(
         MethodData.builder("getGasNeeded", TileEntityRotaryCondensentrator$ComputerHandler::gasTank$getGasNeeded)
            .returnType(long.class)
            .methodDescription("Get the amount needed to fill the gas tank.")
      );
      this.register(
         MethodData.builder("getGasFilledPercentage", TileEntityRotaryCondensentrator$ComputerHandler::gasTank$getGasFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the gas tank.")
      );
      this.register(
         MethodData.builder("getFluid", TileEntityRotaryCondensentrator$ComputerHandler::fluidTank$getFluid)
            .returnType(FluidStack.class)
            .methodDescription("Get the contents of the fluid tank.")
      );
      this.register(
         MethodData.builder("getFluidCapacity", TileEntityRotaryCondensentrator$ComputerHandler::fluidTank$getFluidCapacity)
            .returnType(int.class)
            .methodDescription("Get the capacity of the fluid tank.")
      );
      this.register(
         MethodData.builder("getFluidNeeded", TileEntityRotaryCondensentrator$ComputerHandler::fluidTank$getFluidNeeded)
            .returnType(int.class)
            .methodDescription("Get the amount needed to fill the fluid tank.")
      );
      this.register(
         MethodData.builder("getFluidFilledPercentage", TileEntityRotaryCondensentrator$ComputerHandler::fluidTank$getFluidFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the fluid tank.")
      );
      this.register(
         MethodData.builder("getGasItemInput", TileEntityRotaryCondensentrator$ComputerHandler::gasInputSlot$getGasItemInput)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the gas item input slot.")
      );
      this.register(
         MethodData.builder("getGasItemOutput", TileEntityRotaryCondensentrator$ComputerHandler::gasOutputSlot$getGasItemOutput)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the gas item output slot.")
      );
      this.register(
         MethodData.builder("getFluidItemInput", TileEntityRotaryCondensentrator$ComputerHandler::fluidInputSlot$getFluidItemInput)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the fluid item input slot.")
      );
      this.register(
         MethodData.builder("getFluidItemOutput", TileEntityRotaryCondensentrator$ComputerHandler::fluidOutputSlot$getFluidItemOutput)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the fluid item ouput slot.")
      );
      this.register(
         MethodData.builder("getEnergyItem", TileEntityRotaryCondensentrator$ComputerHandler::energySlot$getEnergyItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the energy slot.")
      );
      this.register(
         MethodData.builder("getEnergyUsage", TileEntityRotaryCondensentrator$ComputerHandler::getEnergyUsage_0)
            .returnType(FloatingLong.class)
            .methodDescription("Get the energy used in the last tick by the machine")
      );
      this.register(MethodData.builder("isCondensentrating", TileEntityRotaryCondensentrator$ComputerHandler::isCondensentrating_0).returnType(boolean.class));
      this.register(
         MethodData.builder("setCondensentrating", TileEntityRotaryCondensentrator$ComputerHandler::setCondensentrating_1)
            .requiresPublicSecurity()
            .arguments(this.NAMES_value, this.TYPES_3db6c47)
      );
   }

   public static Object gasTank$getGas(TileEntityRotaryCondensentrator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getStack(subject.gasTank));
   }

   public static Object gasTank$getGasCapacity(TileEntityRotaryCondensentrator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getCapacity(subject.gasTank));
   }

   public static Object gasTank$getGasNeeded(TileEntityRotaryCondensentrator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getNeeded(subject.gasTank));
   }

   public static Object gasTank$getGasFilledPercentage(TileEntityRotaryCondensentrator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getFilledPercentage(subject.gasTank));
   }

   public static Object fluidTank$getFluid(TileEntityRotaryCondensentrator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerFluidTankWrapper.getStack(subject.fluidTank));
   }

   public static Object fluidTank$getFluidCapacity(TileEntityRotaryCondensentrator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerFluidTankWrapper.getCapacity(subject.fluidTank));
   }

   public static Object fluidTank$getFluidNeeded(TileEntityRotaryCondensentrator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerFluidTankWrapper.getNeeded(subject.fluidTank));
   }

   public static Object fluidTank$getFluidFilledPercentage(TileEntityRotaryCondensentrator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerFluidTankWrapper.getFilledPercentage(subject.fluidTank));
   }

   public static Object gasInputSlot$getGasItemInput(TileEntityRotaryCondensentrator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.gasInputSlot));
   }

   public static Object gasOutputSlot$getGasItemOutput(TileEntityRotaryCondensentrator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.gasOutputSlot));
   }

   public static Object fluidInputSlot$getFluidItemInput(TileEntityRotaryCondensentrator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.fluidInputSlot));
   }

   public static Object fluidOutputSlot$getFluidItemOutput(TileEntityRotaryCondensentrator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.fluidOutputSlot));
   }

   public static Object energySlot$getEnergyItem(TileEntityRotaryCondensentrator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.energySlot));
   }

   public static Object getEnergyUsage_0(TileEntityRotaryCondensentrator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getEnergyUsed());
   }

   public static Object isCondensentrating_0(TileEntityRotaryCondensentrator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.isCondensentrating());
   }

   public static Object setCondensentrating_1(TileEntityRotaryCondensentrator subject, BaseComputerHelper helper) throws ComputerException {
      subject.setCondensentrating(helper.getBoolean(0));
      return helper.voidResult();
   }
}
