package mekanism.common.tile.machine;

import mekanism.api.math.FloatingLong;
import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.MethodFactory;
import net.minecraft.world.item.ItemStack;

@MethodFactory(
   target = TileEntityResistiveHeater.class
)
public class TileEntityResistiveHeater$ComputerHandler extends ComputerMethodFactory<TileEntityResistiveHeater> {
   private final String[] NAMES_usage = new String[]{"usage"};
   private final Class[] TYPES_6a7f69c8 = new Class[]{FloatingLong.class};

   public TileEntityResistiveHeater$ComputerHandler() {
      this.register(
         MethodData.builder("getTemperature", TileEntityResistiveHeater$ComputerHandler::heatCapacitor$getTemperature)
            .returnType(double.class)
            .methodDescription("Get the temperature of the heater in Kelvin.")
      );
      this.register(
         MethodData.builder("getEnergyItem", TileEntityResistiveHeater$ComputerHandler::energySlot$getEnergyItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the energy slot.")
      );
      this.register(MethodData.builder("getEnergyUsed", TileEntityResistiveHeater$ComputerHandler::getEnergyUsed_0).returnType(FloatingLong.class));
      this.register(MethodData.builder("getTransferLoss", TileEntityResistiveHeater$ComputerHandler::getTransferLoss_0).returnType(double.class));
      this.register(MethodData.builder("getEnvironmentalLoss", TileEntityResistiveHeater$ComputerHandler::getEnvironmentalLoss_0).returnType(double.class));
      this.register(
         MethodData.builder("getEnergyUsage", TileEntityResistiveHeater$ComputerHandler::getEnergyUsage_0)
            .returnType(FloatingLong.class)
            .methodDescription("Get the energy used in the last tick by the machine")
      );
      this.register(
         MethodData.builder("setEnergyUsage", TileEntityResistiveHeater$ComputerHandler::setEnergyUsage_1)
            .requiresPublicSecurity()
            .arguments(this.NAMES_usage, this.TYPES_6a7f69c8)
      );
   }

   public static Object heatCapacitor$getTemperature(TileEntityResistiveHeater subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerHeatCapacitorWrapper.getTemperature(subject.heatCapacitor));
   }

   public static Object energySlot$getEnergyItem(TileEntityResistiveHeater subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.energySlot));
   }

   public static Object getEnergyUsed_0(TileEntityResistiveHeater subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getEnergyUsed());
   }

   public static Object getTransferLoss_0(TileEntityResistiveHeater subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getLastTransferLoss());
   }

   public static Object getEnvironmentalLoss_0(TileEntityResistiveHeater subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getLastEnvironmentLoss());
   }

   public static Object getEnergyUsage_0(TileEntityResistiveHeater subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getEnergyUsage());
   }

   public static Object setEnergyUsage_1(TileEntityResistiveHeater subject, BaseComputerHelper helper) throws ComputerException {
      subject.setEnergyUsage(helper.getFloatingLong(0));
      return helper.voidResult();
   }
}
