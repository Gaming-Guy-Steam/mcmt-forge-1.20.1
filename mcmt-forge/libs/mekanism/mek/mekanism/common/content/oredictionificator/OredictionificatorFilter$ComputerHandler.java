package mekanism.common.content.oredictionificator;

import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.annotation.MethodFactory;
import net.minecraft.resources.ResourceLocation;

@MethodFactory(
   target = OredictionificatorFilter.class
)
public class OredictionificatorFilter$ComputerHandler extends ComputerMethodFactory<OredictionificatorFilter> {
   private final String[] NAMES_tag = new String[]{"tag"};
   private final Class[] TYPES_dbbe1d5d = new Class[]{ResourceLocation.class};

   public OredictionificatorFilter$ComputerHandler() {
      this.register(MethodData.builder("getFilter", OredictionificatorFilter$ComputerHandler::getFilter_0).threadSafe().returnType(String.class));
      this.register(MethodData.builder("setFilter", OredictionificatorFilter$ComputerHandler::setFilter_1).arguments(this.NAMES_tag, this.TYPES_dbbe1d5d));
      this.register(MethodData.builder("clone", OredictionificatorFilter$ComputerHandler::clone_0).threadSafe().returnType(OredictionificatorFilter.class));
   }

   public static Object getFilter_0(OredictionificatorFilter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getFilterText());
   }

   public static Object setFilter_1(OredictionificatorFilter subject, BaseComputerHelper helper) throws ComputerException {
      subject.computerSetFilter(helper.getResourceLocation(0));
      return helper.voidResult();
   }

   public static Object clone_0(OredictionificatorFilter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.clone());
   }
}
