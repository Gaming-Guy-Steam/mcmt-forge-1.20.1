package mekanism.common.tile;

import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.MethodFactory;
import net.minecraft.world.item.ItemStack;

@MethodFactory(
   target = TileEntityModificationStation.class
)
public class TileEntityModificationStation$ComputerHandler extends ComputerMethodFactory<TileEntityModificationStation> {
   public TileEntityModificationStation$ComputerHandler() {
      this.register(
         MethodData.builder("getEnergyItem", TileEntityModificationStation$ComputerHandler::energySlot$getEnergyItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the energy slot.")
      );
      this.register(
         MethodData.builder("getModuleItem", TileEntityModificationStation$ComputerHandler::moduleSlot$getModuleItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the module slot.")
      );
      this.register(
         MethodData.builder("getContainerItem", TileEntityModificationStation$ComputerHandler::containerSlot$getContainerItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the module holder slot (suit, tool, etc).")
      );
   }

   public static Object energySlot$getEnergyItem(TileEntityModificationStation subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.energySlot));
   }

   public static Object moduleSlot$getModuleItem(TileEntityModificationStation subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.moduleSlot));
   }

   public static Object containerSlot$getContainerItem(TileEntityModificationStation subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.containerSlot));
   }
}
