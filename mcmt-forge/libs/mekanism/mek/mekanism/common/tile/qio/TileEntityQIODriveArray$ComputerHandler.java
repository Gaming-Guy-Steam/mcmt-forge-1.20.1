package mekanism.common.tile.qio;

import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.annotation.MethodFactory;
import net.minecraft.world.item.ItemStack;

@MethodFactory(
   target = TileEntityQIODriveArray.class
)
public class TileEntityQIODriveArray$ComputerHandler extends ComputerMethodFactory<TileEntityQIODriveArray> {
   private final String[] NAMES_slot = new String[]{"slot"};
   private final Class[] TYPES_1980e = new Class[]{int.class};

   public TileEntityQIODriveArray$ComputerHandler() {
      this.register(MethodData.builder("getSlotCount", TileEntityQIODriveArray$ComputerHandler::getSlotCount_0).returnType(int.class));
      this.register(
         MethodData.builder("getDrive", TileEntityQIODriveArray$ComputerHandler::getDrive_1)
            .returnType(ItemStack.class)
            .arguments(this.NAMES_slot, this.TYPES_1980e)
      );
      this.register(
         MethodData.builder("getDriveStatus", TileEntityQIODriveArray$ComputerHandler::getDriveStatus_1)
            .returnType(TileEntityQIODriveArray.DriveStatus.class)
            .arguments(this.NAMES_slot, this.TYPES_1980e)
      );
      this.register(
         MethodData.builder("getFrequencyItemCount", TileEntityQIODriveArray$ComputerHandler::getFrequencyItemCount_0)
            .returnType(long.class)
            .methodDescription("Requires a frequency to be selected")
      );
      this.register(
         MethodData.builder("getFrequencyItemCapacity", TileEntityQIODriveArray$ComputerHandler::getFrequencyItemCapacity_0)
            .returnType(long.class)
            .methodDescription("Requires a frequency to be selected")
      );
      this.register(
         MethodData.builder("getFrequencyItemPercentage", TileEntityQIODriveArray$ComputerHandler::getFrequencyItemPercentage_0)
            .returnType(double.class)
            .methodDescription("Requires a frequency to be selected")
      );
      this.register(
         MethodData.builder("getFrequencyItemTypeCount", TileEntityQIODriveArray$ComputerHandler::getFrequencyItemTypeCount_0)
            .returnType(long.class)
            .methodDescription("Requires a frequency to be selected")
      );
      this.register(
         MethodData.builder("getFrequencyItemTypeCapacity", TileEntityQIODriveArray$ComputerHandler::getFrequencyItemTypeCapacity_0)
            .returnType(long.class)
            .methodDescription("Requires a frequency to be selected")
      );
      this.register(
         MethodData.builder("getFrequencyItemTypePercentage", TileEntityQIODriveArray$ComputerHandler::getFrequencyItemTypePercentage_0)
            .returnType(double.class)
            .methodDescription("Requires a frequency to be selected")
      );
   }

   public static Object getSlotCount_0(TileEntityQIODriveArray subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getSlotCount());
   }

   public static Object getDrive_1(TileEntityQIODriveArray subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getDrive(helper.getInt(0)));
   }

   public static Object getDriveStatus_1(TileEntityQIODriveArray subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getDriveStatus(helper.getInt(0)));
   }

   public static Object getFrequencyItemCount_0(TileEntityQIODriveArray subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getFrequencyItemCount());
   }

   public static Object getFrequencyItemCapacity_0(TileEntityQIODriveArray subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getFrequencyItemCapacity());
   }

   public static Object getFrequencyItemPercentage_0(TileEntityQIODriveArray subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getFrequencyItemPercentage());
   }

   public static Object getFrequencyItemTypeCount_0(TileEntityQIODriveArray subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getFrequencyItemTypeCount());
   }

   public static Object getFrequencyItemTypeCapacity_0(TileEntityQIODriveArray subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getFrequencyItemTypeCapacity());
   }

   public static Object getFrequencyItemTypePercentage_0(TileEntityQIODriveArray subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getFrequencyItemTypePercentage());
   }
}
