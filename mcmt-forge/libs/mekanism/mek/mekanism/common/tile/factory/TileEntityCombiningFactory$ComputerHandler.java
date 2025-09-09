package mekanism.common.tile.factory;

import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.MethodFactory;
import net.minecraft.world.item.ItemStack;

@MethodFactory(
   target = TileEntityCombiningFactory.class
)
public class TileEntityCombiningFactory$ComputerHandler extends ComputerMethodFactory<TileEntityCombiningFactory> {
   public TileEntityCombiningFactory$ComputerHandler() {
      this.register(
         MethodData.builder("getSecondaryInput", TileEntityCombiningFactory$ComputerHandler::extraSlot$getSecondaryInput)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the secondary input slot.")
      );
   }

   public static Object extraSlot$getSecondaryInput(TileEntityCombiningFactory subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.extraSlot));
   }
}
