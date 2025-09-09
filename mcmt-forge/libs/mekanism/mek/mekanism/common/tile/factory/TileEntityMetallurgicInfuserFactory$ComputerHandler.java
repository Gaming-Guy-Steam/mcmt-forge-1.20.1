package mekanism.common.tile.factory;

import mekanism.api.chemical.ChemicalStack;
import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.MethodFactory;
import net.minecraft.world.item.ItemStack;

@MethodFactory(
   target = TileEntityMetallurgicInfuserFactory.class
)
public class TileEntityMetallurgicInfuserFactory$ComputerHandler extends ComputerMethodFactory<TileEntityMetallurgicInfuserFactory> {
   public TileEntityMetallurgicInfuserFactory$ComputerHandler() {
      this.register(
         MethodData.builder("getInfuseTypeItem", TileEntityMetallurgicInfuserFactory$ComputerHandler::extraSlot$getInfuseTypeItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the infusion extra input slot.")
      );
      this.register(
         MethodData.builder("getInfuseType", TileEntityMetallurgicInfuserFactory$ComputerHandler::infusionTank$getInfuseType)
            .returnType(ChemicalStack.class)
            .methodDescription("Get the contents of the infusion buffer.")
      );
      this.register(
         MethodData.builder("getInfuseTypeCapacity", TileEntityMetallurgicInfuserFactory$ComputerHandler::infusionTank$getInfuseTypeCapacity)
            .returnType(long.class)
            .methodDescription("Get the capacity of the infusion buffer.")
      );
      this.register(
         MethodData.builder("getInfuseTypeNeeded", TileEntityMetallurgicInfuserFactory$ComputerHandler::infusionTank$getInfuseTypeNeeded)
            .returnType(long.class)
            .methodDescription("Get the amount needed to fill the infusion buffer.")
      );
      this.register(
         MethodData.builder("getInfuseTypeFilledPercentage", TileEntityMetallurgicInfuserFactory$ComputerHandler::infusionTank$getInfuseTypeFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the infusion buffer.")
      );
      this.register(
         MethodData.builder("dumpInfuseType", TileEntityMetallurgicInfuserFactory$ComputerHandler::dumpInfuseType_0)
            .methodDescription("Empty the contents of the infusion buffer into the environment")
            .requiresPublicSecurity()
      );
   }

   public static Object extraSlot$getInfuseTypeItem(TileEntityMetallurgicInfuserFactory subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.extraSlot));
   }

   public static Object infusionTank$getInfuseType(TileEntityMetallurgicInfuserFactory subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getStack(subject.infusionTank));
   }

   public static Object infusionTank$getInfuseTypeCapacity(TileEntityMetallurgicInfuserFactory subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getCapacity(subject.infusionTank));
   }

   public static Object infusionTank$getInfuseTypeNeeded(TileEntityMetallurgicInfuserFactory subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getNeeded(subject.infusionTank));
   }

   public static Object infusionTank$getInfuseTypeFilledPercentage(TileEntityMetallurgicInfuserFactory subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getFilledPercentage(subject.infusionTank));
   }

   public static Object dumpInfuseType_0(TileEntityMetallurgicInfuserFactory subject, BaseComputerHelper helper) throws ComputerException {
      subject.dumpInfuseType();
      return helper.voidResult();
   }
}
