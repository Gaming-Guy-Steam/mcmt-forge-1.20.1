package mekanism.common.tile.interfaces;

import java.util.List;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public interface ISideConfiguration {
   TileComponentConfig getConfig();

   Direction getDirection();

   TileComponentEjector getEjector();

   @Nullable
   default DataType getActiveDataType(Object container) {
      ConfigInfo info = null;
      TileComponentConfig config = this.getConfig();
      if (container instanceof IGasTank && config.supports(TransmissionType.GAS)) {
         info = config.getConfig(TransmissionType.GAS);
      } else if (container instanceof IInfusionTank && config.supports(TransmissionType.INFUSION)) {
         info = config.getConfig(TransmissionType.INFUSION);
      } else if (container instanceof IPigmentTank && config.supports(TransmissionType.PIGMENT)) {
         info = config.getConfig(TransmissionType.PIGMENT);
      } else if (container instanceof ISlurryTank && config.supports(TransmissionType.SLURRY)) {
         info = config.getConfig(TransmissionType.SLURRY);
      } else if (container instanceof IExtendedFluidTank && config.supports(TransmissionType.FLUID)) {
         info = config.getConfig(TransmissionType.FLUID);
      } else if (container instanceof IInventorySlot && config.supports(TransmissionType.ITEM)) {
         info = config.getConfig(TransmissionType.ITEM);
      }

      if (info != null) {
         List<DataType> types = info.getDataTypeForContainer(container);
         int count = types.size();
         if (count > 0 && count < info.getSupportedDataTypes().size()) {
            return types.get(0);
         }
      }

      return null;
   }
}
