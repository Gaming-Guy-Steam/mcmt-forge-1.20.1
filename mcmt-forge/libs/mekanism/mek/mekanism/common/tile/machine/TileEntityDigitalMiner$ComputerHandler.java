package mekanism.common.tile.machine;

import java.util.List;
import mekanism.api.math.FloatingLong;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.content.miner.ThreadMinerSearch;
import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.MethodFactory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@MethodFactory(
   target = TileEntityDigitalMiner.class
)
public class TileEntityDigitalMiner$ComputerHandler extends ComputerMethodFactory<TileEntityDigitalMiner> {
   private final String[] NAMES_pull = new String[]{"pull"};
   private final String[] NAMES_eject = new String[]{"eject"};
   private final String[] NAMES_silk = new String[]{"silk"};
   private final String[] NAMES_minY = new String[]{"minY"};
   private final String[] NAMES_maxY = new String[]{"maxY"};
   private final String[] NAMES_slot = new String[]{"slot"};
   private final String[] NAMES_target = new String[]{"target"};
   private final String[] NAMES_requiresReplacement = new String[]{"requiresReplacement"};
   private final String[] NAMES_filter = new String[]{"filter"};
   private final String[] NAMES_enabled = new String[]{"enabled"};
   private final String[] NAMES_radius = new String[]{"radius"};
   private final Class[] TYPES_b987be9f = new Class[]{Item.class};
   private final Class[] TYPES_38f0baba = new Class[]{MinerFilter.class};
   private final Class[] TYPES_3db6c47 = new Class[]{boolean.class};
   private final Class[] TYPES_1980e = new Class[]{int.class};

   public TileEntityDigitalMiner$ComputerHandler() {
      this.register(
         MethodData.builder("getEnergyItem", TileEntityDigitalMiner$ComputerHandler::energySlot$getEnergyItem)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the energy slot.")
      );
      this.register(
         MethodData.builder("getSilkTouch", TileEntityDigitalMiner$ComputerHandler::getSilkTouch_0)
            .returnType(boolean.class)
            .methodDescription("Whether Silk Touch mode is enabled or not")
      );
      this.register(
         MethodData.builder("getRadius", TileEntityDigitalMiner$ComputerHandler::getRadius_0)
            .returnType(int.class)
            .methodDescription("Get the current radius configured (blocks)")
      );
      this.register(
         MethodData.builder("getMinY", TileEntityDigitalMiner$ComputerHandler::getMinY_0)
            .returnType(int.class)
            .methodDescription("Gets the configured minimum Y level for mining")
      );
      this.register(
         MethodData.builder("getMaxY", TileEntityDigitalMiner$ComputerHandler::getMaxY_0)
            .returnType(int.class)
            .methodDescription("Gets the configured maximum Y level for mining")
      );
      this.register(
         MethodData.builder("getInverseMode", TileEntityDigitalMiner$ComputerHandler::getInverseMode_0)
            .returnType(boolean.class)
            .methodDescription("Whether Inverse Mode is enabled or not")
      );
      this.register(
         MethodData.builder("getInverseModeRequiresReplacement", TileEntityDigitalMiner$ComputerHandler::getInverseModeRequiresReplacement_0)
            .returnType(boolean.class)
            .methodDescription("Whether Inverse Mode Require Replacement is turned on")
      );
      this.register(
         MethodData.builder("getInverseModeReplaceTarget", TileEntityDigitalMiner$ComputerHandler::getInverseModeReplaceTarget_0)
            .returnType(Item.class)
            .methodDescription("Get the configured Replacement target item")
      );
      this.register(
         MethodData.builder("getToMine", TileEntityDigitalMiner$ComputerHandler::getToMine_0)
            .returnType(int.class)
            .methodDescription("Get the count of block found but not yet mined")
      );
      this.register(
         MethodData.builder("isRunning", TileEntityDigitalMiner$ComputerHandler::isRunning_0)
            .returnType(boolean.class)
            .methodDescription("Whether the miner is currently running")
      );
      this.register(
         MethodData.builder("getAutoEject", TileEntityDigitalMiner$ComputerHandler::getAutoEject_0)
            .returnType(boolean.class)
            .methodDescription("Whether Auto Eject is turned on")
      );
      this.register(
         MethodData.builder("getAutoPull", TileEntityDigitalMiner$ComputerHandler::getAutoPull_0)
            .returnType(boolean.class)
            .methodDescription("Whether Auto Pull is turned on")
      );
      this.register(
         MethodData.builder("getEnergyUsage", TileEntityDigitalMiner$ComputerHandler::getEnergyUsage_0)
            .returnType(FloatingLong.class)
            .methodDescription("Get the energy used in the last tick by the machine")
      );
      this.register(
         MethodData.builder("getSlotCount", TileEntityDigitalMiner$ComputerHandler::getSlotCount_0)
            .returnType(int.class)
            .methodDescription("Get the size of the Miner's internal inventory")
      );
      this.register(
         MethodData.builder("getItemInSlot", TileEntityDigitalMiner$ComputerHandler::getItemInSlot_1)
            .returnType(ItemStack.class)
            .methodDescription("Get the contents of the internal inventory slot. 0 based.")
            .arguments(this.NAMES_slot, this.TYPES_1980e)
      );
      this.register(
         MethodData.builder("getState", TileEntityDigitalMiner$ComputerHandler::getState_0)
            .returnType(ThreadMinerSearch.State.class)
            .methodDescription("Get the state of the Miner's search")
      );
      this.register(
         MethodData.builder("setAutoEject", TileEntityDigitalMiner$ComputerHandler::setAutoEject_1)
            .methodDescription("Update the Auto Eject setting")
            .requiresPublicSecurity()
            .arguments(this.NAMES_eject, this.TYPES_3db6c47)
      );
      this.register(
         MethodData.builder("setAutoPull", TileEntityDigitalMiner$ComputerHandler::setAutoPull_1)
            .methodDescription("Update the Auto Pull setting")
            .requiresPublicSecurity()
            .arguments(this.NAMES_pull, this.TYPES_3db6c47)
      );
      this.register(
         MethodData.builder("setSilkTouch", TileEntityDigitalMiner$ComputerHandler::setSilkTouch_1)
            .methodDescription("Update the Silk Touch setting")
            .requiresPublicSecurity()
            .arguments(this.NAMES_silk, this.TYPES_3db6c47)
      );
      this.register(
         MethodData.builder("start", TileEntityDigitalMiner$ComputerHandler::start_0)
            .methodDescription("Attempt to start the mining process")
            .requiresPublicSecurity()
      );
      this.register(
         MethodData.builder("stop", TileEntityDigitalMiner$ComputerHandler::stop_0)
            .methodDescription("Attempt to stop the mining process")
            .requiresPublicSecurity()
      );
      this.register(
         MethodData.builder("reset", TileEntityDigitalMiner$ComputerHandler::reset_0)
            .methodDescription("Stop the mining process and reset the Miner to be able to change settings")
            .requiresPublicSecurity()
      );
      this.register(
         MethodData.builder("getMaxRadius", TileEntityDigitalMiner$ComputerHandler::getMaxRadius_0)
            .returnType(int.class)
            .methodDescription("Get the maximum allowable Radius value, determined from the mod's config")
      );
      this.register(
         MethodData.builder("setRadius", TileEntityDigitalMiner$ComputerHandler::setRadius_1)
            .methodDescription("Update the mining radius (blocks). Requires miner to be stopped/reset first")
            .requiresPublicSecurity()
            .arguments(this.NAMES_radius, this.TYPES_1980e)
      );
      this.register(
         MethodData.builder("setMinY", TileEntityDigitalMiner$ComputerHandler::setMinY_1)
            .methodDescription("Update the minimum Y level for mining. Requires miner to be stopped/reset first")
            .requiresPublicSecurity()
            .arguments(this.NAMES_minY, this.TYPES_1980e)
      );
      this.register(
         MethodData.builder("setMaxY", TileEntityDigitalMiner$ComputerHandler::setMaxY_1)
            .methodDescription("Update the maximum Y level for mining. Requires miner to be stopped/reset first")
            .requiresPublicSecurity()
            .arguments(this.NAMES_maxY, this.TYPES_1980e)
      );
      this.register(
         MethodData.builder("setInverseMode", TileEntityDigitalMiner$ComputerHandler::setInverseMode_1)
            .methodDescription("Update the Inverse Mode setting. Requires miner to be stopped/reset first")
            .requiresPublicSecurity()
            .arguments(this.NAMES_enabled, this.TYPES_3db6c47)
      );
      this.register(
         MethodData.builder("setInverseModeRequiresReplacement", TileEntityDigitalMiner$ComputerHandler::setInverseModeRequiresReplacement_1)
            .methodDescription("Update the Inverse Mode Requires Replacement setting. Requires miner to be stopped/reset first")
            .requiresPublicSecurity()
            .arguments(this.NAMES_requiresReplacement, this.TYPES_3db6c47)
      );
      this.register(
         MethodData.builder("setInverseModeReplaceTarget", TileEntityDigitalMiner$ComputerHandler::setInverseModeReplaceTarget_1)
            .methodDescription("Update the target for Replacement in Inverse Mode. Requires miner to be stopped/reset first")
            .requiresPublicSecurity()
            .arguments(this.NAMES_target, this.TYPES_b987be9f)
      );
      this.register(
         MethodData.builder("clearInverseModeReplaceTarget", TileEntityDigitalMiner$ComputerHandler::clearInverseModeReplaceTarget_0)
            .methodDescription("Remove the target for Replacement in Inverse Mode. Requires miner to be stopped/reset first")
            .requiresPublicSecurity()
      );
      this.register(
         MethodData.builder("getFilters", TileEntityDigitalMiner$ComputerHandler::getFilters_0)
            .returnType(List.class)
            .returnExtra(MinerFilter.class)
            .methodDescription("Get the current list of Miner Filters")
      );
      this.register(
         MethodData.builder("addFilter", TileEntityDigitalMiner$ComputerHandler::addFilter_1)
            .returnType(boolean.class)
            .methodDescription("Add a new filter to the miner. Requires miner to be stopped/reset first")
            .requiresPublicSecurity()
            .arguments(this.NAMES_filter, this.TYPES_38f0baba)
      );
      this.register(
         MethodData.builder("removeFilter", TileEntityDigitalMiner$ComputerHandler::removeFilter_1)
            .returnType(boolean.class)
            .methodDescription("Removes the exactly matching filter from the miner. Requires miner to be stopped/reset first")
            .requiresPublicSecurity()
            .arguments(this.NAMES_filter, this.TYPES_38f0baba)
      );
   }

   public static Object energySlot$getEnergyItem(TileEntityDigitalMiner subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.getStack(subject.energySlot));
   }

   public static Object getSilkTouch_0(TileEntityDigitalMiner subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getSilkTouch());
   }

   public static Object getRadius_0(TileEntityDigitalMiner subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getRadius());
   }

   public static Object getMinY_0(TileEntityDigitalMiner subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getMinY());
   }

   public static Object getMaxY_0(TileEntityDigitalMiner subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getMaxY());
   }

   public static Object getInverseMode_0(TileEntityDigitalMiner subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getInverse());
   }

   public static Object getInverseModeRequiresReplacement_0(TileEntityDigitalMiner subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getInverseRequiresReplacement());
   }

   public static Object getInverseModeReplaceTarget_0(TileEntityDigitalMiner subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getInverseReplaceTarget());
   }

   public static Object getToMine_0(TileEntityDigitalMiner subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getToMine());
   }

   public static Object isRunning_0(TileEntityDigitalMiner subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.isRunning());
   }

   public static Object getAutoEject_0(TileEntityDigitalMiner subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getDoEject());
   }

   public static Object getAutoPull_0(TileEntityDigitalMiner subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getDoPull());
   }

   public static Object getEnergyUsage_0(TileEntityDigitalMiner subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getEnergyUsage());
   }

   public static Object getSlotCount_0(TileEntityDigitalMiner subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getSlotCount());
   }

   public static Object getItemInSlot_1(TileEntityDigitalMiner subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getItemInSlot(helper.getInt(0)));
   }

   public static Object getState_0(TileEntityDigitalMiner subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getState());
   }

   public static Object setAutoEject_1(TileEntityDigitalMiner subject, BaseComputerHelper helper) throws ComputerException {
      subject.setAutoEject(helper.getBoolean(0));
      return helper.voidResult();
   }

   public static Object setAutoPull_1(TileEntityDigitalMiner subject, BaseComputerHelper helper) throws ComputerException {
      subject.setAutoPull(helper.getBoolean(0));
      return helper.voidResult();
   }

   public static Object setSilkTouch_1(TileEntityDigitalMiner subject, BaseComputerHelper helper) throws ComputerException {
      subject.computerSetSilkTouch(helper.getBoolean(0));
      return helper.voidResult();
   }

   public static Object start_0(TileEntityDigitalMiner subject, BaseComputerHelper helper) throws ComputerException {
      subject.computerStart();
      return helper.voidResult();
   }

   public static Object stop_0(TileEntityDigitalMiner subject, BaseComputerHelper helper) throws ComputerException {
      subject.computerStop();
      return helper.voidResult();
   }

   public static Object reset_0(TileEntityDigitalMiner subject, BaseComputerHelper helper) throws ComputerException {
      subject.computerReset();
      return helper.voidResult();
   }

   public static Object getMaxRadius_0(TileEntityDigitalMiner subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getMaxRadius());
   }

   public static Object setRadius_1(TileEntityDigitalMiner subject, BaseComputerHelper helper) throws ComputerException {
      subject.computerSetRadius(helper.getInt(0));
      return helper.voidResult();
   }

   public static Object setMinY_1(TileEntityDigitalMiner subject, BaseComputerHelper helper) throws ComputerException {
      subject.computerSetMinY(helper.getInt(0));
      return helper.voidResult();
   }

   public static Object setMaxY_1(TileEntityDigitalMiner subject, BaseComputerHelper helper) throws ComputerException {
      subject.computerSetMaxY(helper.getInt(0));
      return helper.voidResult();
   }

   public static Object setInverseMode_1(TileEntityDigitalMiner subject, BaseComputerHelper helper) throws ComputerException {
      subject.setInverseMode(helper.getBoolean(0));
      return helper.voidResult();
   }

   public static Object setInverseModeRequiresReplacement_1(TileEntityDigitalMiner subject, BaseComputerHelper helper) throws ComputerException {
      subject.setInverseModeRequiresReplacement(helper.getBoolean(0));
      return helper.voidResult();
   }

   public static Object setInverseModeReplaceTarget_1(TileEntityDigitalMiner subject, BaseComputerHelper helper) throws ComputerException {
      subject.setInverseModeReplaceTarget(helper.getItem(0));
      return helper.voidResult();
   }

   public static Object clearInverseModeReplaceTarget_0(TileEntityDigitalMiner subject, BaseComputerHelper helper) throws ComputerException {
      subject.clearInverseModeReplaceTarget();
      return helper.voidResult();
   }

   public static Object getFilters_0(TileEntityDigitalMiner subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getFilters(), helper::convert);
   }

   public static Object addFilter_1(TileEntityDigitalMiner subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.addFilter(helper.getFilter(0, MinerFilter.class)));
   }

   public static Object removeFilter_1(TileEntityDigitalMiner subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.removeFilter(helper.getFilter(0, MinerFilter.class)));
   }
}
