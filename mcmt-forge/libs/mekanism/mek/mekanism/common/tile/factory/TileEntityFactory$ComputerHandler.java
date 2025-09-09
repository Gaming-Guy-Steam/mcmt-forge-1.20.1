package mekanism.common.tile.factory;

import mekanism.api.math.FloatingLong;
import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.MethodFactory;
import net.minecraft.world.item.ItemStack;

@MethodFactory(
   target = TileEntityFactory.class
)
public class TileEntityFactory$ComputerHandler extends ComputerMethodFactory<TileEntityFactory> {
   private final String[] NAMES_process = new String[]{"process"};
   private final String[] NAMES_enabled = new String[]{"enabled"};
   private final Class[] TYPES_3db6c47 = new Class[]{boolean.class};
   private final Class[] TYPES_1980e = new Class[]{int.class};

   public TileEntityFactory$ComputerHandler() {
      this.register(
         MethodData.builder("getEnergyItem", TileEntityFactory$ComputerHandler::energySlot$getEnergyItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the energy slot.")
      );
      this.register(MethodData.builder("isAutoSortEnabled", TileEntityFactory$ComputerHandler::isAutoSortEnabled_0).returnType(boolean.class));
      this.register(
         MethodData.builder("getEnergyUsage", TileEntityFactory$ComputerHandler::getEnergyUsage_0)
            .returnType(FloatingLong.class)
            .methodDescription("Get the energy used in the last tick by the machine")
      );
      this.register(
         MethodData.builder("getTicksRequired", TileEntityFactory$ComputerHandler::getTicksRequired_0)
            .returnType(int.class)
            .methodDescription("Total number of ticks it takes currently for the recipe to complete")
      );
      this.register(
         MethodData.builder("setAutoSort", TileEntityFactory$ComputerHandler::setAutoSort_1)
            .requiresPublicSecurity()
            .arguments(this.NAMES_enabled, this.TYPES_3db6c47)
      );
      this.register(
         MethodData.builder("getRecipeProgress", TileEntityFactory$ComputerHandler::getRecipeProgress_1)
            .returnType(int.class)
            .arguments(this.NAMES_process, this.TYPES_1980e)
      );
      this.register(
         MethodData.builder("getInput", TileEntityFactory$ComputerHandler::getInput_1)
            .returnType(ItemStack.class)
            .arguments(this.NAMES_process, this.TYPES_1980e)
      );
      this.register(
         MethodData.builder("getOutput", TileEntityFactory$ComputerHandler::getOutput_1)
            .returnType(ItemStack.class)
            .arguments(this.NAMES_process, this.TYPES_1980e)
      );
   }

   public static Object energySlot$getEnergyItem(TileEntityFactory subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.energySlot));
   }

   public static Object isAutoSortEnabled_0(TileEntityFactory subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.isSorting());
   }

   public static Object getEnergyUsage_0(TileEntityFactory subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getLastUsage());
   }

   public static Object getTicksRequired_0(TileEntityFactory subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getTicksRequired());
   }

   public static Object setAutoSort_1(TileEntityFactory subject, BaseComputerHelper helper) throws ComputerException {
      subject.setAutoSort(helper.getBoolean(0));
      return helper.voidResult();
   }

   public static Object getRecipeProgress_1(TileEntityFactory subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getRecipeProgress(helper.getInt(0)));
   }

   public static Object getInput_1(TileEntityFactory subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getInput(helper.getInt(0)));
   }

   public static Object getOutput_1(TileEntityFactory subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getOutput(helper.getInt(0)));
   }
}
