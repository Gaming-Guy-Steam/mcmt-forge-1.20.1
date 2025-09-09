package mekanism.common.content.tank;

import com.mojang.datafixers.util.Either;
import mekanism.api.chemical.ChemicalStack;
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
   target = TankMultiblockData.class
)
public class TankMultiblockData$ComputerHandler extends ComputerMethodFactory<TankMultiblockData> {
   private final String[] NAMES_mode = new String[]{"mode"};
   private final Class[] TYPES_f8347998 = new Class[]{IFluidContainerManager.ContainerEditMode.class};

   public TankMultiblockData$ComputerHandler() {
      this.register(
         MethodData.builder("getContainerEditMode", TankMultiblockData$ComputerHandler::getContainerEditMode_0)
            .returnType(IFluidContainerManager.ContainerEditMode.class)
      );
      this.register(
         MethodData.builder("getInputItem", TankMultiblockData$ComputerHandler::inputSlot$getInputItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the input slot.")
      );
      this.register(
         MethodData.builder("getOutputItem", TankMultiblockData$ComputerHandler::outputSlot$getOutputItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the output slot.")
      );
      this.register(MethodData.builder("getTankCapacity", TankMultiblockData$ComputerHandler::getTankCapacity_0).returnType(int.class));
      this.register(MethodData.builder("getChemicalTankCapacity", TankMultiblockData$ComputerHandler::getChemicalTankCapacity_0).returnType(long.class));
      this.register(
         MethodData.builder("setContainerEditMode", TankMultiblockData$ComputerHandler::setContainerEditMode_1).arguments(this.NAMES_mode, this.TYPES_f8347998)
      );
      this.register(MethodData.builder("incrementContainerEditMode", TankMultiblockData$ComputerHandler::incrementContainerEditMode_0));
      this.register(MethodData.builder("decrementContainerEditMode", TankMultiblockData$ComputerHandler::decrementContainerEditMode_0));
      this.register(
         MethodData.builder("getStored", TankMultiblockData$ComputerHandler::getStored_0)
            .returnType(Either.class)
            .returnExtra(ChemicalStack.class, FluidStack.class)
      );
      this.register(MethodData.builder("getFilledPercentage", TankMultiblockData$ComputerHandler::getFilledPercentage_0).returnType(double.class));
   }

   public static Object getContainerEditMode_0(TankMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.editMode);
   }

   public static Object inputSlot$getInputItem(TankMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.inputSlot));
   }

   public static Object outputSlot$getOutputItem(TankMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.outputSlot));
   }

   public static Object getTankCapacity_0(TankMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getTankCapacity());
   }

   public static Object getChemicalTankCapacity_0(TankMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getChemicalTankCapacity());
   }

   public static Object setContainerEditMode_1(TankMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      subject.setContainerEditMode(helper.getEnum(0, IFluidContainerManager.ContainerEditMode.class));
      return helper.voidResult();
   }

   public static Object incrementContainerEditMode_0(TankMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      subject.incrementContainerEditMode();
      return helper.voidResult();
   }

   public static Object decrementContainerEditMode_0(TankMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      subject.decrementContainerEditMode();
      return helper.voidResult();
   }

   public static Object getStored_0(TankMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return subject.getStored().map(helper::convert, helper::convert);
   }

   public static Object getFilledPercentage_0(TankMultiblockData subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getFilledPercentage());
   }
}
