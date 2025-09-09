package mekanism.common.tile;

import mekanism.api.chemical.ChemicalStack;
import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.MethodFactory;
import net.minecraft.world.item.ItemStack;

@MethodFactory(
   target = TileEntityChemicalTank.class
)
public class TileEntityChemicalTank$ComputerHandler extends ComputerMethodFactory<TileEntityChemicalTank> {
   private final String[] NAMES_mode = new String[]{"mode"};
   private final Class[] TYPES_ef806282 = new Class[]{TileEntityChemicalTank.GasMode.class};

   public TileEntityChemicalTank$ComputerHandler() {
      this.register(
         MethodData.builder("getDumpingMode", TileEntityChemicalTank$ComputerHandler::getDumpingMode_0)
            .returnType(TileEntityChemicalTank.GasMode.class)
            .methodDescription("Get the current Dumping configuration")
      );
      this.register(
         MethodData.builder("getDrainItem", TileEntityChemicalTank$ComputerHandler::drainSlot$getDrainItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the drain slot.")
      );
      this.register(
         MethodData.builder("getFillItem", TileEntityChemicalTank$ComputerHandler::fillSlot$getFillItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the fill slot.")
      );
      this.register(
         MethodData.builder("getStored", TileEntityChemicalTank$ComputerHandler::getCurrentTank$getStored)
            .returnType(ChemicalStack.class)
            .methodDescription("Get the contents of the tank.")
      );
      this.register(
         MethodData.builder("getCapacity", TileEntityChemicalTank$ComputerHandler::getCurrentTank$getCapacity)
            .returnType(long.class)
            .methodDescription("Get the capacity of the tank.")
      );
      this.register(
         MethodData.builder("getNeeded", TileEntityChemicalTank$ComputerHandler::getCurrentTank$getNeeded)
            .returnType(long.class)
            .methodDescription("Get the amount needed to fill the tank.")
      );
      this.register(
         MethodData.builder("getFilledPercentage", TileEntityChemicalTank$ComputerHandler::getCurrentTank$getFilledPercentage)
            .returnType(double.class)
            .methodDescription("Get the filled percentage of the tank.")
      );
      this.register(
         MethodData.builder("setDumpingMode", TileEntityChemicalTank$ComputerHandler::setDumpingMode_1)
            .methodDescription("Set the Dumping mode of the tank")
            .requiresPublicSecurity()
            .arguments(this.NAMES_mode, this.TYPES_ef806282)
      );
      this.register(
         MethodData.builder("incrementDumpingMode", TileEntityChemicalTank$ComputerHandler::incrementDumpingMode_0)
            .methodDescription("Advance the Dumping mode to the next configuration in the list")
            .requiresPublicSecurity()
      );
      this.register(
         MethodData.builder("decrementDumpingMode", TileEntityChemicalTank$ComputerHandler::decrementDumpingMode_0)
            .methodDescription("Descend the Dumping mode to the previous configuration in the list")
            .requiresPublicSecurity()
      );
   }

   public static Object getDumpingMode_0(TileEntityChemicalTank subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.dumping);
   }

   public static Object drainSlot$getDrainItem(TileEntityChemicalTank subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.drainSlot));
   }

   public static Object fillSlot$getFillItem(TileEntityChemicalTank subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.fillSlot));
   }

   public static Object getCurrentTank$getStored(TileEntityChemicalTank subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getStack(subject.getCurrentTank()));
   }

   public static Object getCurrentTank$getCapacity(TileEntityChemicalTank subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getCapacity(subject.getCurrentTank()));
   }

   public static Object getCurrentTank$getNeeded(TileEntityChemicalTank subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getNeeded(subject.getCurrentTank()));
   }

   public static Object getCurrentTank$getFilledPercentage(TileEntityChemicalTank subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.getFilledPercentage(subject.getCurrentTank()));
   }

   public static Object setDumpingMode_1(TileEntityChemicalTank subject, BaseComputerHelper helper) throws ComputerException {
      subject.setDumpingMode(helper.getEnum(0, TileEntityChemicalTank.GasMode.class));
      return helper.voidResult();
   }

   public static Object incrementDumpingMode_0(TileEntityChemicalTank subject, BaseComputerHelper helper) throws ComputerException {
      subject.incrementDumpingMode();
      return helper.voidResult();
   }

   public static Object decrementDumpingMode_0(TileEntityChemicalTank subject, BaseComputerHelper helper) throws ComputerException {
      subject.decrementDumpingMode();
      return helper.voidResult();
   }
}
