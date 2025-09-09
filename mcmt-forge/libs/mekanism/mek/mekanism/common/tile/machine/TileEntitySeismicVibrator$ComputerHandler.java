package mekanism.common.tile.machine;

import java.util.Map;
import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.MethodFactory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

@MethodFactory(
   target = TileEntitySeismicVibrator.class
)
public class TileEntitySeismicVibrator$ComputerHandler extends ComputerMethodFactory<TileEntitySeismicVibrator> {
   private final String[] NAMES_chunkRelativeX_y_chunkRelativeZ = new String[]{"chunkRelativeX", "y", "chunkRelativeZ"};
   private final String[] NAMES_chunkRelativeX_chunkRelativeZ = new String[]{"chunkRelativeX", "chunkRelativeZ"};
   private final Class[] TYPES_62eca6e = new Class[]{int.class, int.class, int.class};
   private final Class[] TYPES_3301a1 = new Class[]{int.class, int.class};

   public TileEntitySeismicVibrator$ComputerHandler() {
      this.register(
         MethodData.builder("getEnergyItem", TileEntitySeismicVibrator$ComputerHandler::energySlot$getEnergyItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the energy slot.")
      );
      this.register(MethodData.builder("isVibrating", TileEntitySeismicVibrator$ComputerHandler::isVibrating_0).returnType(boolean.class));
      this.register(
         MethodData.builder("getBlockAt", TileEntitySeismicVibrator$ComputerHandler::getBlockAt_3)
            .returnType(BlockState.class)
            .arguments(this.NAMES_chunkRelativeX_y_chunkRelativeZ, this.TYPES_62eca6e)
      );
      this.register(
         MethodData.builder("getColumnAt", TileEntitySeismicVibrator$ComputerHandler::getColumnAt_2)
            .returnType(Map.class)
            .returnExtra(Integer.class, BlockState.class)
            .methodDescription("Get a column info, table key is the Y level")
            .arguments(this.NAMES_chunkRelativeX_chunkRelativeZ, this.TYPES_3301a1)
      );
   }

   public static Object energySlot$getEnergyItem(TileEntitySeismicVibrator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.energySlot));
   }

   public static Object isVibrating_0(TileEntitySeismicVibrator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.isVibrating());
   }

   public static Object getBlockAt_3(TileEntitySeismicVibrator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getBlockAt(helper.getInt(0), helper.getInt(1), helper.getInt(2)));
   }

   public static Object getColumnAt_2(TileEntitySeismicVibrator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getColumnAt(helper.getInt(0), helper.getInt(1)), helper::convert, helper::convert);
   }
}
