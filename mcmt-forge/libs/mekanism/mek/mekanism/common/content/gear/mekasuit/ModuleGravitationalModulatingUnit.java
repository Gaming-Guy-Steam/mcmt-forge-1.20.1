package mekanism.common.content.gear.mekasuit;

import java.util.function.Consumer;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IHUDElement;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.api.gear.config.ModuleEnumData;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.MekanismUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

@ParametersAreNotNullByDefault
public class ModuleGravitationalModulatingUnit implements ICustomModule<ModuleGravitationalModulatingUnit> {
   private static final ResourceLocation icon = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_HUD, "gravitational_modulation_unit.png");
   private IModuleConfigItem<ModuleLocomotiveBoostingUnit.SprintBoost> speedBoost;

   @Override
   public void init(IModule<ModuleGravitationalModulatingUnit> module, ModuleConfigItemCreator configItemCreator) {
      this.speedBoost = configItemCreator.createConfigItem(
         "speed_boost", MekanismLang.MODULE_SPEED_BOOST, new ModuleEnumData<>(ModuleLocomotiveBoostingUnit.SprintBoost.LOW)
      );
   }

   @Override
   public void addHUDElements(IModule<ModuleGravitationalModulatingUnit> module, Player player, Consumer<IHUDElement> hudElementAdder) {
      hudElementAdder.accept(IModuleHelper.INSTANCE.hudElementEnabled(icon, module.isEnabled()));
   }

   @Override
   public boolean canChangeModeWhenDisabled(IModule<ModuleGravitationalModulatingUnit> module) {
      return true;
   }

   @Override
   public void changeMode(IModule<ModuleGravitationalModulatingUnit> module, Player player, ItemStack stack, int shift, boolean displayChangeMessage) {
      module.toggleEnabled(player, MekanismLang.MODULE_GRAVITATIONAL_MODULATION.translate(new Object[0]));
   }

   public float getBoost() {
      return this.speedBoost.get().getBoost();
   }

   @Override
   public void tickClient(IModule<ModuleGravitationalModulatingUnit> module, Player player) {
      if (player.m_150110_().f_35935_
         && MekanismKeyHandler.boostKey.m_90857_()
         && module.canUseEnergy(player, MekanismConfig.gear.mekaSuitEnergyUsageGravitationalModulation.get().multiply(4L), false)) {
         float boost = this.getBoost();
         if (boost > 0.0F) {
            player.m_19920_(boost, new Vec3(0.0, 0.0, 1.0));
         }
      }
   }
}
