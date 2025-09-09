package mekanism.common.content.filter;

import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.annotation.MethodFactory;

@MethodFactory(
   target = IFilter.class
)
public class IFilter$ComputerHandler extends ComputerMethodFactory<IFilter> {
   private final String[] NAMES_enabled = new String[]{"enabled"};
   private final Class[] TYPES_3db6c47 = new Class[]{boolean.class};

   public IFilter$ComputerHandler() {
      this.register(MethodData.builder("getFilterType", IFilter$ComputerHandler::getFilterType_0).threadSafe().returnType(FilterType.class));
      this.register(MethodData.builder("isEnabled", IFilter$ComputerHandler::isEnabled_0).threadSafe().returnType(boolean.class));
      this.register(MethodData.builder("setEnabled", IFilter$ComputerHandler::setEnabled_1).threadSafe().arguments(this.NAMES_enabled, this.TYPES_3db6c47));
   }

   public static Object getFilterType_0(IFilter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getFilterType());
   }

   public static Object isEnabled_0(IFilter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.isEnabled());
   }

   public static Object setEnabled_1(IFilter subject, BaseComputerHelper helper) throws ComputerException {
      subject.setEnabled(helper.getBoolean(0));
      return helper.voidResult();
   }
}
