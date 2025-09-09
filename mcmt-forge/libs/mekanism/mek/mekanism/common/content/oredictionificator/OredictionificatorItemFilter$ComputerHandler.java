package mekanism.common.content.oredictionificator;

import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.annotation.MethodFactory;
import net.minecraft.world.item.Item;

@MethodFactory(
   target = OredictionificatorItemFilter.class
)
public class OredictionificatorItemFilter$ComputerHandler extends ComputerMethodFactory<OredictionificatorItemFilter> {
   private final String[] NAMES_item = new String[]{"item"};
   private final Class[] TYPES_b987be9f = new Class[]{Item.class};

   public OredictionificatorItemFilter$ComputerHandler() {
      this.register(MethodData.builder("getSelectedOutput", OredictionificatorItemFilter$ComputerHandler::getSelectedOutput_0).returnType(Item.class));
      this.register(
         MethodData.builder("setSelectedOutput", OredictionificatorItemFilter$ComputerHandler::setSelectedOutput_1)
            .threadSafe()
            .arguments(this.NAMES_item, this.TYPES_b987be9f)
      );
   }

   public static Object getSelectedOutput_0(OredictionificatorItemFilter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getResultElement());
   }

   public static Object setSelectedOutput_1(OredictionificatorItemFilter subject, BaseComputerHelper helper) throws ComputerException {
      subject.computerSetSelectedOutput(helper.getItem(0));
      return helper.voidResult();
   }
}
