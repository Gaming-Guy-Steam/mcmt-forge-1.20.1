package mekanism.common.tile.qio;

import java.util.List;
import mekanism.common.content.qio.filter.QIOFilter;
import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.annotation.MethodFactory;

@MethodFactory(
   target = TileEntityQIOFilterHandler.class
)
public class TileEntityQIOFilterHandler$ComputerHandler extends ComputerMethodFactory<TileEntityQIOFilterHandler> {
   private final String[] NAMES_filter = new String[]{"filter"};
   private final Class[] TYPES_7e00eebc = new Class[]{QIOFilter.class};

   public TileEntityQIOFilterHandler$ComputerHandler() {
      this.register(
         MethodData.builder("getFilters", TileEntityQIOFilterHandler$ComputerHandler::getFilters_0).returnType(List.class).returnExtra(QIOFilter.class)
      );
      this.register(
         MethodData.builder("addFilter", TileEntityQIOFilterHandler$ComputerHandler::addFilter_1)
            .returnType(boolean.class)
            .requiresPublicSecurity()
            .arguments(this.NAMES_filter, this.TYPES_7e00eebc)
      );
      this.register(
         MethodData.builder("removeFilter", TileEntityQIOFilterHandler$ComputerHandler::removeFilter_1)
            .returnType(boolean.class)
            .requiresPublicSecurity()
            .arguments(this.NAMES_filter, this.TYPES_7e00eebc)
      );
   }

   public static Object getFilters_0(TileEntityQIOFilterHandler subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getFilters(), helper::convert);
   }

   public static Object addFilter_1(TileEntityQIOFilterHandler subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.addFilter(helper.getFilter(0, QIOFilter.class)));
   }

   public static Object removeFilter_1(TileEntityQIOFilterHandler subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.removeFilter(helper.getFilter(0, QIOFilter.class)));
   }
}
