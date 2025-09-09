package mekanism.common.content.transporter;

import mekanism.api.text.EnumColor;
import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.annotation.MethodFactory;

@MethodFactory(
   target = SorterFilter.class
)
public class SorterFilter$ComputerHandler extends ComputerMethodFactory<SorterFilter> {
   private final String[] NAMES_min_max = new String[]{"min", "max"};
   private final String[] NAMES_value = new String[]{"value"};
   private final Class[] TYPES_3301a1 = new Class[]{int.class, int.class};
   private final Class[] TYPES_3db6c47 = new Class[]{boolean.class};
   private final Class[] TYPES_88e2323f = new Class[]{EnumColor.class};

   public SorterFilter$ComputerHandler() {
      this.register(MethodData.builder("getColor", SorterFilter$ComputerHandler::getColor_0).threadSafe().returnType(EnumColor.class));
      this.register(MethodData.builder("setColor", SorterFilter$ComputerHandler::setColor_1).threadSafe().arguments(this.NAMES_value, this.TYPES_88e2323f));
      this.register(MethodData.builder("getAllowDefault", SorterFilter$ComputerHandler::getAllowDefault_0).threadSafe().returnType(boolean.class));
      this.register(
         MethodData.builder("setAllowDefault", SorterFilter$ComputerHandler::setAllowDefault_1).threadSafe().arguments(this.NAMES_value, this.TYPES_3db6c47)
      );
      this.register(MethodData.builder("getSizeMode", SorterFilter$ComputerHandler::getSizeMode_0).threadSafe().returnType(boolean.class));
      this.register(MethodData.builder("setSizeMode", SorterFilter$ComputerHandler::setSizeMode_1).threadSafe().arguments(this.NAMES_value, this.TYPES_3db6c47));
      this.register(MethodData.builder("getMin", SorterFilter$ComputerHandler::getMin_0).threadSafe().returnType(int.class));
      this.register(MethodData.builder("getMax", SorterFilter$ComputerHandler::getMax_0).threadSafe().returnType(int.class));
      this.register(MethodData.builder("setMinMax", SorterFilter$ComputerHandler::setMinMax_2).threadSafe().arguments(this.NAMES_min_max, this.TYPES_3301a1));
      this.register(MethodData.builder("clone", SorterFilter$ComputerHandler::clone_0).threadSafe().returnType(SorterFilter.class));
   }

   public static Object getColor_0(SorterFilter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.color);
   }

   public static Object setColor_1(SorterFilter subject, BaseComputerHelper helper) throws ComputerException {
      subject.color = helper.getEnum(0, EnumColor.class);
      return helper.voidResult();
   }

   public static Object getAllowDefault_0(SorterFilter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.allowDefault);
   }

   public static Object setAllowDefault_1(SorterFilter subject, BaseComputerHelper helper) throws ComputerException {
      subject.allowDefault = helper.getBoolean(0);
      return helper.voidResult();
   }

   public static Object getSizeMode_0(SorterFilter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.sizeMode);
   }

   public static Object setSizeMode_1(SorterFilter subject, BaseComputerHelper helper) throws ComputerException {
      subject.sizeMode = helper.getBoolean(0);
      return helper.voidResult();
   }

   public static Object getMin_0(SorterFilter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.min);
   }

   public static Object getMax_0(SorterFilter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.max);
   }

   public static Object setMinMax_2(SorterFilter subject, BaseComputerHelper helper) throws ComputerException {
      subject.setMinMax(helper.getInt(0), helper.getInt(1));
      return helper.voidResult();
   }

   public static Object clone_0(SorterFilter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.clone());
   }
}
