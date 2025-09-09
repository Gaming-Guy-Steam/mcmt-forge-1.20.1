package mekanism.common.content.qio.filter;

import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.annotation.MethodFactory;

@MethodFactory(
   target = QIOFilter.class
)
public class QIOFilter$ComputerHandler extends ComputerMethodFactory<QIOFilter> {
   public QIOFilter$ComputerHandler() {
      this.register(MethodData.builder("clone", QIOFilter$ComputerHandler::clone_0).threadSafe().returnType(QIOFilter.class));
   }

   public static Object clone_0(QIOFilter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.clone());
   }
}
