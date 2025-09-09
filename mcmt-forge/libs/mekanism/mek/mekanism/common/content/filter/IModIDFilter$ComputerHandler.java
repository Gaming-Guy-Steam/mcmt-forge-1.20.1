package mekanism.common.content.filter;

import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.annotation.MethodFactory;

@MethodFactory(
   target = IModIDFilter.class
)
public class IModIDFilter$ComputerHandler extends ComputerMethodFactory<IModIDFilter> {
   private final String[] NAMES_id = new String[]{"id"};
   private final Class[] TYPES_473e3684 = new Class[]{String.class};

   public IModIDFilter$ComputerHandler() {
      this.register(MethodData.builder("setModID", IModIDFilter$ComputerHandler::setModID_1).threadSafe().arguments(this.NAMES_id, this.TYPES_473e3684));
      this.register(MethodData.builder("getModID", IModIDFilter$ComputerHandler::getModID_0).threadSafe().returnType(String.class));
   }

   public static Object setModID_1(IModIDFilter subject, BaseComputerHelper helper) throws ComputerException {
      subject.setModID(helper.getString(0));
      return helper.voidResult();
   }

   public static Object getModID_0(IModIDFilter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getModID());
   }
}
