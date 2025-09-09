package mekanism.common.tile;

import java.util.Collection;
import mekanism.api.chemical.ChemicalStack;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.MethodFactory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

@MethodFactory(
   target = TileEntityQuantumEntangloporter.class
)
public class TileEntityQuantumEntangloporter$ComputerHandler extends ComputerMethodFactory<TileEntityQuantumEntangloporter> {
   private final String[] NAMES_name = new String[]{"name"};
   private final Class[] TYPES_473e3684 = new Class[]{String.class};

   public TileEntityQuantumEntangloporter$ComputerHandler() {
      this.register(MethodData.builder("hasFrequency", TileEntityQuantumEntangloporter$ComputerHandler::hasFrequency_0).returnType(boolean.class));
      this.register(
         MethodData.builder("getTransferLoss", TileEntityQuantumEntangloporter$ComputerHandler::getTransferLoss_0)
            .returnType(double.class)
            .methodDescription("May not be accurate if there is no frequency")
      );
      this.register(
         MethodData.builder("getEnvironmentalLoss", TileEntityQuantumEntangloporter$ComputerHandler::getEnvironmentalLoss_0)
            .returnType(double.class)
            .methodDescription("May not be accurate if there is no frequency")
      );
      this.register(
         MethodData.builder("getFrequencies", TileEntityQuantumEntangloporter$ComputerHandler::getFrequencies_0)
            .returnType(Collection.class)
            .returnExtra(InventoryFrequency.class)
            .methodDescription("Lists public frequencies")
      );
      this.register(
         MethodData.builder("getFrequency", TileEntityQuantumEntangloporter$ComputerHandler::getFrequency_0)
            .returnType(InventoryFrequency.class)
            .methodDescription("Requires a frequency to be selected")
      );
      this.register(
         MethodData.builder("setFrequency", TileEntityQuantumEntangloporter$ComputerHandler::setFrequency_1)
            .methodDescription("Requires a public frequency to exist")
            .requiresPublicSecurity()
            .arguments(this.NAMES_name, this.TYPES_473e3684)
      );
      this.register(
         MethodData.builder("createFrequency", TileEntityQuantumEntangloporter$ComputerHandler::createFrequency_1)
            .methodDescription(
               "Requires frequency to not already exist and for it to be public so that it can make it as the player who owns the block. Also sets the frequency after creation"
            )
            .requiresPublicSecurity()
            .arguments(this.NAMES_name, this.TYPES_473e3684)
      );
      this.register(MethodData.builder("getBufferItem", TileEntityQuantumEntangloporter$ComputerHandler::getBufferItem_0).returnType(ItemStack.class));
      this.register(
         MethodData.builder("getBufferFluid", TileEntityQuantumEntangloporter$ComputerHandler::getBufferFluidTank$getBufferFluid)
            .returnType(FluidStack.class)
            .methodDescription("Get the contents of the fluid buffer.")
      );
      this.register(
         MethodData.builder("getBufferFluidCapacity", TileEntityQuantumEntangloporter$ComputerHandler::getBufferFluidTank$getBufferFluidCapacity)
            .returnType(int.class)
            .methodDescription("Get the capacity of the fluid buffer.")
      );
      this.register(
         MethodData.builder("getBufferFluidNeeded", TileEntityQuantumEntangloporter$ComputerHandler::getBufferFluidTank$getBufferFluidNeeded)
            .returnType(int.class)
            .methodDescription("Get the amount needed to fill the fluid buffer.")
      );
      this.register(
         MethodData.builder(
               "getBufferFluidFilledPercentage", TileEntityQuantumEntangloporter$ComputerHandler::getBufferFluidTank$getBufferFluidFilledPercentage
            )
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the fluid buffer.")
      );
      this.register(
         MethodData.builder("getBufferGas", TileEntityQuantumEntangloporter$ComputerHandler::getBufferGasTank$getBufferGas)
            .returnType(ChemicalStack.class)
            .methodDescription("Get the contents of the gas buffer.")
      );
      this.register(
         MethodData.builder("getBufferGasCapacity", TileEntityQuantumEntangloporter$ComputerHandler::getBufferGasTank$getBufferGasCapacity)
            .returnType(long.class)
            .methodDescription("Get the capacity of the gas buffer.")
      );
      this.register(
         MethodData.builder("getBufferGasNeeded", TileEntityQuantumEntangloporter$ComputerHandler::getBufferGasTank$getBufferGasNeeded)
            .returnType(long.class)
            .methodDescription("Get the amount needed to fill the gas buffer.")
      );
      this.register(
         MethodData.builder("getBufferGasFilledPercentage", TileEntityQuantumEntangloporter$ComputerHandler::getBufferGasTank$getBufferGasFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the gas buffer.")
      );
      this.register(
         MethodData.builder("getBufferInfuseType", TileEntityQuantumEntangloporter$ComputerHandler::getBufferInfuseTypeTank$getBufferInfuseType)
            .returnType(ChemicalStack.class)
            .methodDescription("Get the contents of the infusion buffer.")
      );
      this.register(
         MethodData.builder("getBufferInfuseTypeCapacity", TileEntityQuantumEntangloporter$ComputerHandler::getBufferInfuseTypeTank$getBufferInfuseTypeCapacity)
            .returnType(long.class)
            .methodDescription("Get the capacity of the infusion buffer.")
      );
      this.register(
         MethodData.builder("getBufferInfuseTypeNeeded", TileEntityQuantumEntangloporter$ComputerHandler::getBufferInfuseTypeTank$getBufferInfuseTypeNeeded)
            .returnType(long.class)
            .methodDescription("Get the amount needed to fill the infusion buffer.")
      );
      this.register(
         MethodData.builder(
               "getBufferInfuseTypeFilledPercentage",
               TileEntityQuantumEntangloporter$ComputerHandler::getBufferInfuseTypeTank$getBufferInfuseTypeFilledPercentage
            )
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the infusion buffer.")
      );
      this.register(
         MethodData.builder("getBufferPigment", TileEntityQuantumEntangloporter$ComputerHandler::getBufferPigmentTank$getBufferPigment)
            .returnType(ChemicalStack.class)
            .methodDescription("Get the contents of the pigment buffer.")
      );
      this.register(
         MethodData.builder("getBufferPigmentCapacity", TileEntityQuantumEntangloporter$ComputerHandler::getBufferPigmentTank$getBufferPigmentCapacity)
            .returnType(long.class)
            .methodDescription("Get the capacity of the pigment buffer.")
      );
      this.register(
         MethodData.builder("getBufferPigmentNeeded", TileEntityQuantumEntangloporter$ComputerHandler::getBufferPigmentTank$getBufferPigmentNeeded)
            .returnType(long.class)
            .methodDescription("Get the amount needed to fill the pigment buffer.")
      );
      this.register(
         MethodData.builder(
               "getBufferPigmentFilledPercentage", TileEntityQuantumEntangloporter$ComputerHandler::getBufferPigmentTank$getBufferPigmentFilledPercentage
            )
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the pigment buffer.")
      );
      this.register(
         MethodData.builder("getBufferSlurry", TileEntityQuantumEntangloporter$ComputerHandler::getBufferSlurryTank$getBufferSlurry)
            .returnType(ChemicalStack.class)
            .methodDescription("Get the contents of the slurry buffer.")
      );
      this.register(
         MethodData.builder("getBufferSlurryCapacity", TileEntityQuantumEntangloporter$ComputerHandler::getBufferSlurryTank$getBufferSlurryCapacity)
            .returnType(long.class)
            .methodDescription("Get the capacity of the slurry buffer.")
      );
      this.register(
         MethodData.builder("getBufferSlurryNeeded", TileEntityQuantumEntangloporter$ComputerHandler::getBufferSlurryTank$getBufferSlurryNeeded)
            .returnType(long.class)
            .methodDescription("Get the amount needed to fill the slurry buffer.")
      );
      this.register(
         MethodData.builder(
               "getBufferSlurryFilledPercentage", TileEntityQuantumEntangloporter$ComputerHandler::getBufferSlurryTank$getBufferSlurryFilledPercentage
            )
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the slurry buffer.")
      );
      this.register(
         MethodData.builder("getTemperature", TileEntityQuantumEntangloporter$ComputerHandler::getTemperature_0)
            .returnType(double.class)
            .methodDescription("Requires a frequency to be selected")
      );
   }

   public static Object hasFrequency_0(TileEntityQuantumEntangloporter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.hasFrequency());
   }

   public static Object getTransferLoss_0(TileEntityQuantumEntangloporter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getLastTransferLoss());
   }

   public static Object getEnvironmentalLoss_0(TileEntityQuantumEntangloporter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getLastEnvironmentLoss());
   }

   public static Object getFrequencies_0(TileEntityQuantumEntangloporter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getFrequencies(), helper::convert);
   }

   public static Object getFrequency_0(TileEntityQuantumEntangloporter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getFrequency());
   }

   public static Object setFrequency_1(TileEntityQuantumEntangloporter subject, BaseComputerHelper helper) throws ComputerException {
      subject.setFrequency(helper.getString(0));
      return helper.voidResult();
   }

   public static Object createFrequency_1(TileEntityQuantumEntangloporter subject, BaseComputerHelper helper) throws ComputerException {
      subject.createFrequency(helper.getString(0));
      return helper.voidResult();
   }

   public static Object getBufferItem_0(TileEntityQuantumEntangloporter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getBufferItem());
   }

   public static Object getBufferFluidTank$getBufferFluid(TileEntityQuantumEntangloporter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerFluidTankWrapper.getStack(subject.getBufferFluidTank()));
   }

   public static Object getBufferFluidTank$getBufferFluidCapacity(TileEntityQuantumEntangloporter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerFluidTankWrapper.getCapacity(subject.getBufferFluidTank()));
   }

   public static Object getBufferFluidTank$getBufferFluidNeeded(TileEntityQuantumEntangloporter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerFluidTankWrapper.getNeeded(subject.getBufferFluidTank()));
   }

   public static Object getBufferFluidTank$getBufferFluidFilledPercentage(TileEntityQuantumEntangloporter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerFluidTankWrapper.getFilledPercentage(subject.getBufferFluidTank()));
   }

   public static Object getBufferGasTank$getBufferGas(TileEntityQuantumEntangloporter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getStack(subject.getBufferGasTank()));
   }

   public static Object getBufferGasTank$getBufferGasCapacity(TileEntityQuantumEntangloporter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getCapacity(subject.getBufferGasTank()));
   }

   public static Object getBufferGasTank$getBufferGasNeeded(TileEntityQuantumEntangloporter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getNeeded(subject.getBufferGasTank()));
   }

   public static Object getBufferGasTank$getBufferGasFilledPercentage(TileEntityQuantumEntangloporter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getFilledPercentage(subject.getBufferGasTank()));
   }

   public static Object getBufferInfuseTypeTank$getBufferInfuseType(TileEntityQuantumEntangloporter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getStack(subject.getBufferInfuseTypeTank()));
   }

   public static Object getBufferInfuseTypeTank$getBufferInfuseTypeCapacity(TileEntityQuantumEntangloporter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getCapacity(subject.getBufferInfuseTypeTank()));
   }

   public static Object getBufferInfuseTypeTank$getBufferInfuseTypeNeeded(TileEntityQuantumEntangloporter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getNeeded(subject.getBufferInfuseTypeTank()));
   }

   public static Object getBufferInfuseTypeTank$getBufferInfuseTypeFilledPercentage(TileEntityQuantumEntangloporter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getFilledPercentage(subject.getBufferInfuseTypeTank()));
   }

   public static Object getBufferPigmentTank$getBufferPigment(TileEntityQuantumEntangloporter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getStack(subject.getBufferPigmentTank()));
   }

   public static Object getBufferPigmentTank$getBufferPigmentCapacity(TileEntityQuantumEntangloporter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getCapacity(subject.getBufferPigmentTank()));
   }

   public static Object getBufferPigmentTank$getBufferPigmentNeeded(TileEntityQuantumEntangloporter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getNeeded(subject.getBufferPigmentTank()));
   }

   public static Object getBufferPigmentTank$getBufferPigmentFilledPercentage(TileEntityQuantumEntangloporter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getFilledPercentage(subject.getBufferPigmentTank()));
   }

   public static Object getBufferSlurryTank$getBufferSlurry(TileEntityQuantumEntangloporter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getStack(subject.getBufferSlurryTank()));
   }

   public static Object getBufferSlurryTank$getBufferSlurryCapacity(TileEntityQuantumEntangloporter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getCapacity(subject.getBufferSlurryTank()));
   }

   public static Object getBufferSlurryTank$getBufferSlurryNeeded(TileEntityQuantumEntangloporter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getNeeded(subject.getBufferSlurryTank()));
   }

   public static Object getBufferSlurryTank$getBufferSlurryFilledPercentage(TileEntityQuantumEntangloporter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getFilledPercentage(subject.getBufferSlurryTank()));
   }

   public static Object getTemperature_0(TileEntityQuantumEntangloporter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getTemperature());
   }
}
