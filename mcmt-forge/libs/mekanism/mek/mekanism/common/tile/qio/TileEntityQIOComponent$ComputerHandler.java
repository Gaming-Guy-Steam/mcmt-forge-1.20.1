package mekanism.common.tile.qio;

import java.util.Collection;
import mekanism.api.text.EnumColor;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.annotation.MethodFactory;

@MethodFactory(
   target = TileEntityQIOComponent.class
)
public class TileEntityQIOComponent$ComputerHandler extends ComputerMethodFactory<TileEntityQIOComponent> {
   private final String[] NAMES_color = new String[]{"color"};
   private final String[] NAMES_name = new String[]{"name"};
   private final Class[] TYPES_473e3684 = new Class[]{String.class};
   private final Class[] TYPES_88e2323f = new Class[]{EnumColor.class};

   public TileEntityQIOComponent$ComputerHandler() {
      this.register(
         MethodData.builder("getFrequencies", TileEntityQIOComponent$ComputerHandler::getFrequencies_0)
            .returnType(Collection.class)
            .returnExtra(QIOFrequency.class)
            .methodDescription("Lists public frequencies")
      );
      this.register(MethodData.builder("hasFrequency", TileEntityQIOComponent$ComputerHandler::hasFrequency_0).returnType(boolean.class));
      this.register(
         MethodData.builder("getFrequency", TileEntityQIOComponent$ComputerHandler::getFrequency_0)
            .returnType(QIOFrequency.class)
            .methodDescription("Requires a frequency to be selected")
      );
      this.register(
         MethodData.builder("setFrequency", TileEntityQIOComponent$ComputerHandler::setFrequency_1)
            .methodDescription("Requires a public frequency to exist")
            .requiresPublicSecurity()
            .arguments(this.NAMES_name, this.TYPES_473e3684)
      );
      this.register(
         MethodData.builder("createFrequency", TileEntityQIOComponent$ComputerHandler::createFrequency_1)
            .methodDescription(
               "Requires frequency to not already exist and for it to be public so that it can make it as the player who owns the block. Also sets the frequency after creation"
            )
            .requiresPublicSecurity()
            .arguments(this.NAMES_name, this.TYPES_473e3684)
      );
      this.register(
         MethodData.builder("getFrequencyColor", TileEntityQIOComponent$ComputerHandler::getFrequencyColor_0)
            .returnType(EnumColor.class)
            .methodDescription("Requires a frequency to be selected")
      );
      this.register(
         MethodData.builder("setFrequencyColor", TileEntityQIOComponent$ComputerHandler::setFrequencyColor_1)
            .methodDescription("Requires a frequency to be selected")
            .requiresPublicSecurity()
            .arguments(this.NAMES_color, this.TYPES_88e2323f)
      );
      this.register(
         MethodData.builder("incrementFrequencyColor", TileEntityQIOComponent$ComputerHandler::incrementFrequencyColor_0)
            .methodDescription("Requires a frequency to be selected")
            .requiresPublicSecurity()
      );
      this.register(
         MethodData.builder("decrementFrequencyColor", TileEntityQIOComponent$ComputerHandler::decrementFrequencyColor_0)
            .methodDescription("Requires a frequency to be selected")
            .requiresPublicSecurity()
      );
   }

   public static Object getFrequencies_0(TileEntityQIOComponent subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getFrequencies(), helper::convert);
   }

   public static Object hasFrequency_0(TileEntityQIOComponent subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.hasFrequency());
   }

   public static Object getFrequency_0(TileEntityQIOComponent subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.computerGetFrequency());
   }

   public static Object setFrequency_1(TileEntityQIOComponent subject, BaseComputerHelper helper) throws ComputerException {
      subject.setFrequency(helper.getString(0));
      return helper.voidResult();
   }

   public static Object createFrequency_1(TileEntityQIOComponent subject, BaseComputerHelper helper) throws ComputerException {
      subject.createFrequency(helper.getString(0));
      return helper.voidResult();
   }

   public static Object getFrequencyColor_0(TileEntityQIOComponent subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getFrequencyColor());
   }

   public static Object setFrequencyColor_1(TileEntityQIOComponent subject, BaseComputerHelper helper) throws ComputerException {
      subject.setFrequencyColor(helper.getEnum(0, EnumColor.class));
      return helper.voidResult();
   }

   public static Object incrementFrequencyColor_0(TileEntityQIOComponent subject, BaseComputerHelper helper) throws ComputerException {
      subject.incrementFrequencyColor();
      return helper.voidResult();
   }

   public static Object decrementFrequencyColor_0(TileEntityQIOComponent subject, BaseComputerHelper helper) throws ComputerException {
      subject.decrementFrequencyColor();
      return helper.voidResult();
   }
}
