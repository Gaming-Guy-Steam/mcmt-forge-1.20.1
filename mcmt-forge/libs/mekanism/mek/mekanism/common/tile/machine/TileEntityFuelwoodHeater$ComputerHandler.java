package mekanism.common.tile.machine;

import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.MethodFactory;
import net.minecraft.world.item.ItemStack;

@MethodFactory(
   target = TileEntityFuelwoodHeater.class
)
public class TileEntityFuelwoodHeater$ComputerHandler extends ComputerMethodFactory<TileEntityFuelwoodHeater> {
   public TileEntityFuelwoodHeater$ComputerHandler() {
      this.register(
         MethodData.builder("getFuelItem", TileEntityFuelwoodHeater$ComputerHandler::fuelSlot$getFuelItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the fuel slot.")
      );
      this.register(
         MethodData.builder("getTemperature", TileEntityFuelwoodHeater$ComputerHandler::heatCapacitor$getTemperature)
            .returnType(double.class)
            .methodDescription("Get the temperature of the heater in Kelvin.")
      );
      this.register(MethodData.builder("getTransferLoss", TileEntityFuelwoodHeater$ComputerHandler::getTransferLoss_0).returnType(double.class));
      this.register(MethodData.builder("getEnvironmentalLoss", TileEntityFuelwoodHeater$ComputerHandler::getEnvironmentalLoss_0).returnType(double.class));
   }

   public static Object fuelSlot$getFuelItem(TileEntityFuelwoodHeater subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.fuelSlot));
   }

   public static Object heatCapacitor$getTemperature(TileEntityFuelwoodHeater subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerHeatCapacitorWrapper.getTemperature(subject.heatCapacitor));
   }

   public static Object getTransferLoss_0(TileEntityFuelwoodHeater subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getLastTransferLoss());
   }

   public static Object getEnvironmentalLoss_0(TileEntityFuelwoodHeater subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getLastEnvironmentLoss());
   }
}
