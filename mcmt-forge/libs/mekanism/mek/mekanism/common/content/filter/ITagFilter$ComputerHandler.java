package mekanism.common.content.filter;

import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.annotation.MethodFactory;

@MethodFactory(
   target = ITagFilter.class
)
public class ITagFilter$ComputerHandler extends ComputerMethodFactory<ITagFilter> {
   private final String[] NAMES_name = new String[]{"name"};
   private final Class[] TYPES_473e3684 = new Class[]{String.class};

   public ITagFilter$ComputerHandler() {
      this.register(MethodData.builder("setTagName", ITagFilter$ComputerHandler::setTagName_1).threadSafe().arguments(this.NAMES_name, this.TYPES_473e3684));
      this.register(MethodData.builder("getTagName", ITagFilter$ComputerHandler::getTagName_0).threadSafe().returnType(String.class));
   }

   public static Object setTagName_1(ITagFilter subject, BaseComputerHelper helper) throws ComputerException {
      subject.setTagName(helper.getString(0));
      return helper.voidResult();
   }

   public static Object getTagName_0(ITagFilter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getTagName());
   }
}
