package mekanism.common.tile.qio;

import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.annotation.MethodFactory;

@MethodFactory(
   target = TileEntityQIOExporter.class
)
public class TileEntityQIOExporter$ComputerHandler extends ComputerMethodFactory<TileEntityQIOExporter> {
   private final String[] NAMES_value = new String[]{"value"};
   private final Class[] TYPES_3db6c47 = new Class[]{boolean.class};

   public TileEntityQIOExporter$ComputerHandler() {
      this.register(MethodData.builder("getExportWithoutFilter", TileEntityQIOExporter$ComputerHandler::getExportWithoutFilter_0).returnType(boolean.class));
      this.register(
         MethodData.builder("setExportsWithoutFilter", TileEntityQIOExporter$ComputerHandler::setExportsWithoutFilter_1)
            .requiresPublicSecurity()
            .arguments(this.NAMES_value, this.TYPES_3db6c47)
      );
   }

   public static Object getExportWithoutFilter_0(TileEntityQIOExporter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getExportWithoutFilter());
   }

   public static Object setExportsWithoutFilter_1(TileEntityQIOExporter subject, BaseComputerHelper helper) throws ComputerException {
      subject.setExportsWithoutFilter(helper.getBoolean(0));
      return helper.voidResult();
   }
}
