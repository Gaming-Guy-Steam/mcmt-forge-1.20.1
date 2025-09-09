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

@MethodFactory(
   target = TileEntityMetallurgicInfuser.class
)
public class TileEntityMetallurgicInfuser$ComputerHandler extends ComputerMethodFactory<TileEntityMetallurgicInfuser> {
   public TileEntityMetallurgicInfuser$ComputerHandler() {
      this.register(
         MethodData.builder("getInfuseType", TileEntityMetallurgicInfuser$ComputerHandler::infusionTank$getInfuseType)
            .returnType(ChemicalStack.class)
            .methodDescription("Get the contents of the infusion buffer.")
      );
      this.register(
         MethodData.builder("getInfuseTypeCapacity", TileEntityMetallurgicInfuser$ComputerHandler::infusionTank$getInfuseTypeCapacity)
            .returnType(long.class)
            .methodDescription("Get the capacity of the infusion buffer.")
      );
      this.register(
         MethodData.builder("getInfuseTypeNeeded", TileEntityMetallurgicInfuser$ComputerHandler::infusionTank$getInfuseTypeNeeded)
            .returnType(long.class)
            .methodDescription("Get the amount needed to fill the infusion buffer.")
      );
      this.register(
         MethodData.builder("getInfuseTypeFilledPercentage", TileEntityMetallurgicInfuser$ComputerHandler::infusionTank$getInfuseTypeFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the infusion buffer.")
      );
      this.register(
         MethodData.builder("getInfuseTypeItem", TileEntityMetallurgicInfuser$ComputerHandler::infusionSlot$getInfuseTypeItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the infusion (extra) input slot.")
      );
      this.register(
         MethodData.builder("getInput", TileEntityMetallurgicInfuser$ComputerHandler::inputSlot$getInput)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the input slot.")
      );
      this.register(
         MethodData.builder("getOutput", TileEntityMetallurgicInfuser$ComputerHandler::outputSlot$getOutput)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the output slot.")
      );
      this.register(
         MethodData.builder("getEnergyItem", TileEntityMetallurgicInfuser$ComputerHandler::energySlot$getEnergyItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the energy slot.")
      );
      this.register(
         MethodData.builder("getEnergyUsage", TileEntityMetallurgicInfuser$ComputerHandler::getEnergyUsage_0)
            .returnType(FloatingLong.class)
            .methodDescription("Get the energy used in the last tick by the machine")
      );
      this.register(MethodData.builder("dumpInfuseType", TileEntityMetallurgicInfuser$ComputerHandler::dumpInfuseType_0).requiresPublicSecurity());
   }

   public static Object infusionTank$getInfuseType(TileEntityMetallurgicInfuser subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getStack(subject.infusionTank));
   }

   public static Object infusionTank$getInfuseTypeCapacity(TileEntityMetallurgicInfuser subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getCapacity(subject.infusionTank));
   }

   public static Object infusionTank$getInfuseTypeNeeded(TileEntityMetallurgicInfuser subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getNeeded(subject.infusionTank));
   }

   public static Object infusionTank$getInfuseTypeFilledPercentage(TileEntityMetallurgicInfuser subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getFilledPercentage(subject.infusionTank));
   }

   public static Object infusionSlot$getInfuseTypeItem(TileEntityMetallurgicInfuser subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.infusionSlot));
   }

   public static Object inputSlot$getInput(TileEntityMetallurgicInfuser subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.inputSlot));
   }

   public static Object outputSlot$getOutput(TileEntityMetallurgicInfuser subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.outputSlot));
   }

   public static Object energySlot$getEnergyItem(TileEntityMetallurgicInfuser subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.energySlot));
   }

   public static Object getEnergyUsage_0(TileEntityMetallurgicInfuser subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getEnergyUsage());
   }

   public static Object dumpInfuseType_0(TileEntityMetallurgicInfuser subject, BaseComputerHelper helper) throws ComputerException {
      subject.dumpInfuseType();
      return helper.voidResult();
   }
}
