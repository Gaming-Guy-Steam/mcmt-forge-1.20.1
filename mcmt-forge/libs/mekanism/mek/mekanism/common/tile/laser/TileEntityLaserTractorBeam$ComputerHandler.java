package mekanism.common.tile.laser;

import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.annotation.MethodFactory;
import net.minecraft.world.item.ItemStack;

@MethodFactory(
   target = TileEntityLaserTractorBeam.class
)
public class TileEntityLaserTractorBeam$ComputerHandler extends ComputerMethodFactory<TileEntityLaserTractorBeam> {
   private final String[] NAMES_slot = new String[]{"slot"};
   private final Class[] TYPES_1980e = new Class[]{int.class};

   public TileEntityLaserTractorBeam$ComputerHandler() {
      this.register(MethodData.builder("getSlotCount", TileEntityLaserTractorBeam$ComputerHandler::getSlotCount_0).returnType(int.class));
      this.register(
         MethodData.builder("getItemInSlot", TileEntityLaserTractorBeam$ComputerHandler::getItemInSlot_1)
            .returnType(ItemStack.class)
            .arguments(this.NAMES_slot, this.TYPES_1980e)
      );
   }

   public static Object getSlotCount_0(TileEntityLaserTractorBeam subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getSlotCount());
   }

   public static Object getItemInSlot_1(TileEntityLaserTractorBeam subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getItemInSlot(helper.getInt(0)));
   }
}
