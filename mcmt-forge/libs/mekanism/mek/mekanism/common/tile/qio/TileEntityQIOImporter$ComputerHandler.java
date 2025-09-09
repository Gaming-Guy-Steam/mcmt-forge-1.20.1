package mekanism.common.tile.qio;

import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.annotation.MethodFactory;

@MethodFactory(
   target = TileEntityQIOImporter.class
)
public class TileEntityQIOImporter$ComputerHandler extends ComputerMethodFactory<TileEntityQIOImporter> {
   private final String[] NAMES_value = new String[]{"value"};
   private final Class[] TYPES_3db6c47 = new Class[]{boolean.class};

   public TileEntityQIOImporter$ComputerHandler() {
      this.register(MethodData.builder("getImportWithoutFilter", TileEntityQIOImporter$ComputerHandler::getImportWithoutFilter_0).returnType(boolean.class));
      this.register(
         MethodData.builder("setImportsWithoutFilter", TileEntityQIOImporter$ComputerHandler::setImportsWithoutFilter_1)
            .requiresPublicSecurity()
            .arguments(this.NAMES_value, this.TYPES_3db6c47)
      );
   }

   public static Object getImportWithoutFilter_0(TileEntityQIOImporter subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getImportWithoutFilter());
   }

   public static Object setImportsWithoutFilter_1(TileEntityQIOImporter subject, BaseComputerHelper helper) throws ComputerException {
      subject.setImportsWithoutFilter(helper.getBoolean(0));
      return helper.voidResult();
   }
}
