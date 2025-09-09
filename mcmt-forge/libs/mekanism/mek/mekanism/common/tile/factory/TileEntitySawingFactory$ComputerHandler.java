package mekanism.common.tile.factory;

import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.annotation.MethodFactory;
import net.minecraft.world.item.ItemStack;

@MethodFactory(
   target = TileEntitySawingFactory.class
)
public class TileEntitySawingFactory$ComputerHandler extends ComputerMethodFactory<TileEntitySawingFactory> {
   private final String[] NAMES_process = new String[]{"process"};
   private final Class[] TYPES_1980e = new Class[]{int.class};

   public TileEntitySawingFactory$ComputerHandler() {
      this.register(
         MethodData.builder("getSecondaryOutput", TileEntitySawingFactory$ComputerHandler::getSecondaryOutput_1)
            .returnType(ItemStack.class)
            .arguments(this.NAMES_process, this.TYPES_1980e)
      );
   }

   public static Object getSecondaryOutput_1(TileEntitySawingFactory subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getSecondaryOutput(helper.getInt(0)));
   }
}
