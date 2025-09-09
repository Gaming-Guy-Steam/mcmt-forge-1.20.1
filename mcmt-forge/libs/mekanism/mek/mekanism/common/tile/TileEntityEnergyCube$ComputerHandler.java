package mekanism.common.tile;

import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.MethodFactory;
import net.minecraft.world.item.ItemStack;

@MethodFactory(
   target = TileEntityEnergyCube.class
)
public class TileEntityEnergyCube$ComputerHandler extends ComputerMethodFactory<TileEntityEnergyCube> {
   public TileEntityEnergyCube$ComputerHandler() {
      this.register(
         MethodData.builder("getChargeItem", TileEntityEnergyCube$ComputerHandler::chargeSlot$getChargeItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the charge slot.")
      );
      this.register(
         MethodData.builder("getDischargeItem", TileEntityEnergyCube$ComputerHandler::dischargeSlot$getDischargeItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the discharge slot.")
      );
   }

   public static Object chargeSlot$getChargeItem(TileEntityEnergyCube subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.chargeSlot));
   }

   public static Object dischargeSlot$getDischargeItem(TileEntityEnergyCube subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.dischargeSlot));
   }
}
