package mekanism.common.tile.qio;

import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.annotation.MethodFactory;
import net.minecraft.world.item.ItemStack;

@MethodFactory(
   target = TileEntityQIODashboard.class
)
public class TileEntityQIODashboard$ComputerHandler extends ComputerMethodFactory<TileEntityQIODashboard> {
   private final String[] NAMES_window = new String[]{"window"};
   private final String[] NAMES_window_slot = new String[]{"window", "slot"};
   private final Class[] TYPES_3301a1 = new Class[]{int.class, int.class};
   private final Class[] TYPES_1980e = new Class[]{int.class};

   public TileEntityQIODashboard$ComputerHandler() {
      this.register(
         MethodData.builder("getCraftingInput", TileEntityQIODashboard$ComputerHandler::getCraftingInput_2)
            .returnType(ItemStack.class)
            .arguments(this.NAMES_window_slot, this.TYPES_3301a1)
      );
      this.register(
         MethodData.builder("getCraftingOutput", TileEntityQIODashboard$ComputerHandler::getCraftingOutput_1)
            .returnType(ItemStack.class)
            .arguments(this.NAMES_window, this.TYPES_1980e)
      );
   }

   public static Object getCraftingInput_2(TileEntityQIODashboard subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getCraftingInput(helper.getInt(0), helper.getInt(1)));
   }

   public static Object getCraftingOutput_1(TileEntityQIODashboard subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getCraftingOutput(helper.getInt(0)));
   }
}
