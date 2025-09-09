package mekanism.common.tile.machine;

import java.util.List;
import mekanism.common.content.oredictionificator.OredictionificatorItemFilter;
import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.MethodFactory;
import net.minecraft.world.item.ItemStack;

@MethodFactory(
   target = TileEntityOredictionificator.class
)
public class TileEntityOredictionificator$ComputerHandler extends ComputerMethodFactory<TileEntityOredictionificator> {
   private final String[] NAMES_filter = new String[]{"filter"};
   private final Class[] TYPES_419c1a6b = new Class[]{OredictionificatorItemFilter.class};

   public TileEntityOredictionificator$ComputerHandler() {
      this.register(
         MethodData.builder("getInputItem", TileEntityOredictionificator$ComputerHandler::inputSlot$getInputItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the input slot.")
      );
      this.register(
         MethodData.builder("getOutputItem", TileEntityOredictionificator$ComputerHandler::outputSlot$getOutputItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the output slot.")
      );
      this.register(
         MethodData.builder("getFilters", TileEntityOredictionificator$ComputerHandler::getFilters_0)
            .returnType(List.class)
            .returnExtra(OredictionificatorItemFilter.class)
      );
      this.register(
         MethodData.builder("addFilter", TileEntityOredictionificator$ComputerHandler::addFilter_1)
            .returnType(boolean.class)
            .requiresPublicSecurity()
            .arguments(this.NAMES_filter, this.TYPES_419c1a6b)
      );
      this.register(
         MethodData.builder("removeFilter", TileEntityOredictionificator$ComputerHandler::removeFilter_1)
            .returnType(boolean.class)
            .requiresPublicSecurity()
            .arguments(this.NAMES_filter, this.TYPES_419c1a6b)
      );
   }

   public static Object inputSlot$getInputItem(TileEntityOredictionificator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.inputSlot));
   }

   public static Object outputSlot$getOutputItem(TileEntityOredictionificator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.outputSlot));
   }

   public static Object getFilters_0(TileEntityOredictionificator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getFilters(), helper::convert);
   }

   public static Object addFilter_1(TileEntityOredictionificator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.addFilter(helper.getFilter(0, OredictionificatorItemFilter.class)));
   }

   public static Object removeFilter_1(TileEntityOredictionificator subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.removeFilter(helper.getFilter(0, OredictionificatorItemFilter.class)));
   }
}
