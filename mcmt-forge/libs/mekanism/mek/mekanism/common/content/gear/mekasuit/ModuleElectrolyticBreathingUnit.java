package mekanism.common.content.gear.mekasuit;

import java.util.Map;
import java.util.Optional;
import mekanism.api.Action;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleBooleanData;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.api.math.FloatingLong;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismGases;
import mekanism.common.registries.MekanismItems;
import mekanism.common.registries.MekanismModules;
import mekanism.common.util.MekanismUtils;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fluids.FluidType;

@ParametersAreNotNullByDefault
public class ModuleElectrolyticBreathingUnit implements ICustomModule<ModuleElectrolyticBreathingUnit> {
   private IModuleConfigItem<Boolean> fillHeld;

   @Override
   public void init(IModule<ModuleElectrolyticBreathingUnit> module, ModuleConfigItemCreator configItemCreator) {
      this.fillHeld = configItemCreator.createConfigItem("fill_held", MekanismLang.MODULE_BREATHING_HELD, new ModuleBooleanData());
   }

   @Override
   public void tickServer(IModule<ModuleElectrolyticBreathingUnit> module, Player player) {
      int productionRate = 0;
      float eyeHeight = player.m_20192_();
      Map<FluidType, MekanismUtils.FluidInDetails> fluidsIn = MekanismUtils.getFluidsIn(
         player,
         bb -> {
            double centerX = (bb.f_82288_ + bb.f_82291_) / 2.0;
            double centerZ = (bb.f_82290_ + bb.f_82293_) / 2.0;
            return new AABB(
               centerX, Math.min(bb.f_82289_ + eyeHeight - 0.27, bb.f_82292_), centerZ, centerX, Math.min(bb.f_82289_ + eyeHeight - 0.14, bb.f_82292_), centerZ
            );
         }
      );
      if (fluidsIn.entrySet().stream().anyMatch(entry -> entry.getKey() == ForgeMod.WATER_TYPE.get() && entry.getValue().getMaxHeight() >= 0.11)) {
         productionRate = this.getMaxRate(module);
      } else if (player.m_20285_()) {
         productionRate = this.getMaxRate(module) / 2;
      }

      if (productionRate > 0) {
         FloatingLong usage = MekanismConfig.general.FROM_H2.get().multiply(2L);
         int maxRate = Math.min(productionRate, module.getContainerEnergy().divideToInt(usage));
         long hydrogenUsed = 0L;
         GasStack hydrogenStack = MekanismGases.HYDROGEN.getStack(maxRate * 2L);
         ItemStack chestStack = player.m_6844_(EquipmentSlot.CHEST);
         if (this.checkChestPlate(chestStack)) {
            Optional<IGasHandler> chestCapability = chestStack.getCapability(Capabilities.GAS_HANDLER).resolve();
            if (chestCapability.isPresent()) {
               hydrogenUsed = maxRate * 2L - chestCapability.get().insertChemical(hydrogenStack, Action.EXECUTE).getAmount();
               hydrogenStack.shrink(hydrogenUsed);
            }
         }

         if (this.fillHeld.get()) {
            ItemStack handStack = player.m_6844_(EquipmentSlot.MAINHAND);
            Optional<IGasHandler> handCapability = handStack.getCapability(Capabilities.GAS_HANDLER).resolve();
            if (handCapability.isPresent()) {
               hydrogenUsed = maxRate * 2L - handCapability.get().insertChemical(hydrogenStack, Action.EXECUTE).getAmount();
            }
         }

         int oxygenUsed = Math.min(maxRate, player.m_6062_() - player.m_20146_());
         long used = Math.max((int)Math.ceil(hydrogenUsed / 2.0), oxygenUsed);
         module.useEnergy(player, usage.multiply(used));
         player.m_20301_(player.m_20146_() + oxygenUsed);
      }
   }

   private boolean checkChestPlate(ItemStack chestPlate) {
      return chestPlate.m_41720_() == MekanismItems.MEKASUIT_BODYARMOR.get()
         ? IModuleHelper.INSTANCE.<ModuleJetpackUnit>load(chestPlate, MekanismModules.JETPACK_UNIT) != null
         : true;
   }

   private int getMaxRate(IModule<ModuleElectrolyticBreathingUnit> module) {
      return (int)Math.pow(2.0, module.getInstalledCount());
   }
}
