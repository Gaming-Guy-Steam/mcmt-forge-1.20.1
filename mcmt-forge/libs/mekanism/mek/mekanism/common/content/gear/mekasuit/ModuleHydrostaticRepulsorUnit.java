package mekanism.common.content.gear.mekasuit;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.EnchantmentBasedModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jetbrains.annotations.NotNull;

@ParametersAreNotNullByDefault
public class ModuleHydrostaticRepulsorUnit extends EnchantmentBasedModule<ModuleHydrostaticRepulsorUnit> {
   public static final int BOOST_STACKS = 4;
   private IModuleConfigItem<Boolean> swimBoost;

   @Override
   public void init(IModule<ModuleHydrostaticRepulsorUnit> module, ModuleConfigItemCreator configItemCreator) {
      this.swimBoost = configItemCreator.createDisableableConfigItem("swim_boost", MekanismLang.MODULE_SWIM_BOOST, true, () -> module.getInstalledCount() >= 4);
   }

   @NotNull
   @Override
   public Enchantment getEnchantment() {
      return Enchantments.f_44973_;
   }

   @Override
   public void tickServer(IModule<ModuleHydrostaticRepulsorUnit> module, Player player) {
      if (this.isSwimBoost(module, player)) {
         module.useEnergy(player, MekanismConfig.gear.mekaSuitEnergyUsageHydrostaticRepulsion.get());
      }
   }

   public boolean isSwimBoost(IModule<ModuleHydrostaticRepulsorUnit> module, Player player) {
      return this.swimBoost.get()
         && module.getInstalledCount() >= 4
         && !player.getMaxHeightFluidType().isAir()
         && module.hasEnoughEnergy(MekanismConfig.gear.mekaSuitEnergyUsageHydrostaticRepulsion);
   }
}
