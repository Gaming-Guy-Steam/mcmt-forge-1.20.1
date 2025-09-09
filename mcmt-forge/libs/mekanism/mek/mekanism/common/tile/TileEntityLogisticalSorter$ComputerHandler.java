package mekanism.common.tile;

import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.common.content.transporter.SorterFilter;
import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.annotation.MethodFactory;

@MethodFactory(
   target = TileEntityLogisticalSorter.class
)
public class TileEntityLogisticalSorter$ComputerHandler extends ComputerMethodFactory<TileEntityLogisticalSorter> {
   private final String[] NAMES_color = new String[]{"color"};
   private final String[] NAMES_value = new String[]{"value"};
   private final String[] NAMES_filter = new String[]{"filter"};
   private final Class[] TYPES_3db6c47 = new Class[]{boolean.class};
   private final Class[] TYPES_88e2323f = new Class[]{EnumColor.class};
   private final Class[] TYPES_e3c86f87 = new Class[]{SorterFilter.class};

   public TileEntityLogisticalSorter$ComputerHandler() {
      this.register(MethodData.builder("getDefaultColor", TileEntityLogisticalSorter$ComputerHandler::getDefaultColor_0).returnType(EnumColor.class));
      this.register(MethodData.builder("getAutoMode", TileEntityLogisticalSorter$ComputerHandler::getAutoMode_0).returnType(boolean.class));
      this.register(MethodData.builder("isRoundRobin", TileEntityLogisticalSorter$ComputerHandler::isRoundRobin_0).returnType(boolean.class));
      this.register(MethodData.builder("isSingle", TileEntityLogisticalSorter$ComputerHandler::isSingle_0).returnType(boolean.class));
      this.register(
         MethodData.builder("setSingle", TileEntityLogisticalSorter$ComputerHandler::setSingle_1)
            .requiresPublicSecurity()
            .arguments(this.NAMES_value, this.TYPES_3db6c47)
      );
      this.register(
         MethodData.builder("setRoundRobin", TileEntityLogisticalSorter$ComputerHandler::setRoundRobin_1)
            .requiresPublicSecurity()
            .arguments(this.NAMES_value, this.TYPES_3db6c47)
      );
      this.register(
         MethodData.builder("setAutoMode", TileEntityLogisticalSorter$ComputerHandler::setAutoMode_1)
            .requiresPublicSecurity()
            .arguments(this.NAMES_value, this.TYPES_3db6c47)
      );
      this.register(MethodData.builder("clearDefaultColor", TileEntityLogisticalSorter$ComputerHandler::clearDefaultColor_0).requiresPublicSecurity());
      this.register(MethodData.builder("incrementDefaultColor", TileEntityLogisticalSorter$ComputerHandler::incrementDefaultColor_0).requiresPublicSecurity());
      this.register(MethodData.builder("decrementDefaultColor", TileEntityLogisticalSorter$ComputerHandler::decrementDefaultColor_0).requiresPublicSecurity());
      this.register(
         MethodData.builder("setDefaultColor", TileEntityLogisticalSorter$ComputerHandler::setDefaultColor_1)
            .requiresPublicSecurity()
            .arguments(this.NAMES_color, this.TYPES_88e2323f)
      );
      this.register(
         MethodData.builder("getFilters", TileEntityLogisticalSorter$ComputerHandler::getFilters_0).returnType(List.class).returnExtra(SorterFilter.class)
      );
      this.register(
         MethodData.builder("addFilter", TileEntityLogisticalSorter$ComputerHandler::addFilter_1)
            .returnType(boolean.class)
            .requiresPublicSecurity()
            .arguments(this.NAMES_filter, this.TYPES_e3c86f87)
      );
      this.register(
         MethodData.builder("removeFilter", TileEntityLogisticalSorter$ComputerHandler::removeFilter_1)
            .returnType(boolean.class)
            .requiresPublicSecurity()
            .arguments(this.NAMES_filter, this.TYPES_e3c86f87)
      );
   }

   public static Object getDefaultColor_0(TileEntityLogisticalSorter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.color);
   }

   public static Object getAutoMode_0(TileEntityLogisticalSorter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getAutoEject());
   }

   public static Object isRoundRobin_0(TileEntityLogisticalSorter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getRoundRobin());
   }

   public static Object isSingle_0(TileEntityLogisticalSorter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getSingleItem());
   }

   public static Object setSingle_1(TileEntityLogisticalSorter subject, BaseComputerHelper helper) throws ComputerException {
      subject.setSingle(helper.getBoolean(0));
      return helper.voidResult();
   }

   public static Object setRoundRobin_1(TileEntityLogisticalSorter subject, BaseComputerHelper helper) throws ComputerException {
      subject.setRoundRobin(helper.getBoolean(0));
      return helper.voidResult();
   }

   public static Object setAutoMode_1(TileEntityLogisticalSorter subject, BaseComputerHelper helper) throws ComputerException {
      subject.setAutoMode(helper.getBoolean(0));
      return helper.voidResult();
   }

   public static Object clearDefaultColor_0(TileEntityLogisticalSorter subject, BaseComputerHelper helper) throws ComputerException {
      subject.clearDefaultColor();
      return helper.voidResult();
   }

   public static Object incrementDefaultColor_0(TileEntityLogisticalSorter subject, BaseComputerHelper helper) throws ComputerException {
      subject.incrementDefaultColor();
      return helper.voidResult();
   }

   public static Object decrementDefaultColor_0(TileEntityLogisticalSorter subject, BaseComputerHelper helper) throws ComputerException {
      subject.decrementDefaultColor();
      return helper.voidResult();
   }

   public static Object setDefaultColor_1(TileEntityLogisticalSorter subject, BaseComputerHelper helper) throws ComputerException {
      subject.setDefaultColor(helper.getEnum(0, EnumColor.class));
      return helper.voidResult();
   }

   public static Object getFilters_0(TileEntityLogisticalSorter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getFilters(), helper::convert);
   }

   public static Object addFilter_1(TileEntityLogisticalSorter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.addFilter(helper.getFilter(0, SorterFilter.class)));
   }

   public static Object removeFilter_1(TileEntityLogisticalSorter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.removeFilter(helper.getFilter(0, SorterFilter.class)));
   }
}
