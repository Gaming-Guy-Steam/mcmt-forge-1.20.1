package mekanism.common.tile;

import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.MethodFactory;
import net.minecraft.world.item.ItemStack;

@MethodFactory(
   target = TileEntityBin.class
)
public class TileEntityBin$ComputerHandler extends ComputerMethodFactory<TileEntityBin> {
   public TileEntityBin$ComputerHandler() {
      this.register(
         MethodData.builder("getStored", TileEntityBin$ComputerHandler::binSlot$getStored)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the bin.")
      );
      this.register(
         MethodData.builder("getCapacity", TileEntityBin$ComputerHandler::getCapacity_0)
            .returnType(int.class)
            .methodDescription("Get the maximum number of items the bin can contain.")
      );
      this.register(
         MethodData.builder("isLocked", TileEntityBin$ComputerHandler::isLocked_0)
            .returnType(boolean.class)
            .methodDescription("If true, the Bin is locked to a particular item type.")
      );
      this.register(
         MethodData.builder("getLock", TileEntityBin$ComputerHandler::getLock_0)
            .returnType(ItemStack.class)
            .methodDescription("Get the type of item the Bin is locked to (or Air if not locked)")
      );
      this.register(
         MethodData.builder("lock", TileEntityBin$ComputerHandler::lock_0)
            .methodDescription("Lock the Bin to the currently stored item type. The Bin must not be creative, empty, or already locked")
      );
      this.register(
         MethodData.builder("unlock", TileEntityBin$ComputerHandler::unlock_0)
            .methodDescription("Unlock the Bin's fixed item type. The Bin must not be creative, or already unlocked")
      );
   }

   public static Object binSlot$getStored(TileEntityBin subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.binSlot));
   }

   public static Object getCapacity_0(TileEntityBin subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getCapacity());
   }

   public static Object isLocked_0(TileEntityBin subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.isLocked());
   }

   public static Object getLock_0(TileEntityBin subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getLock());
   }

   public static Object lock_0(TileEntityBin subject, BaseComputerHelper helper) throws ComputerException {
      subject.lock();
      return helper.voidResult();
   }

   public static Object unlock_0(TileEntityBin subject, BaseComputerHelper helper) throws ComputerException {
      subject.unlock();
      return helper.voidResult();
   }
}
