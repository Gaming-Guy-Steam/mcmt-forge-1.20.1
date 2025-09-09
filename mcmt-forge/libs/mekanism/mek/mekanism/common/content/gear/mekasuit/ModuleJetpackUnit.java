package mekanism.common.content.gear.mekasuit;

import java.util.function.Consumer;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IHUDElement;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.api.gear.config.ModuleEnumData;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.item.interfaces.IJetpackItem;
import mekanism.common.registries.MekanismGases;
import mekanism.common.util.StorageUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@ParametersAreNotNullByDefault
public class ModuleJetpackUnit implements ICustomModule<ModuleJetpackUnit> {
   private IModuleConfigItem<IJetpackItem.JetpackMode> jetpackMode;

   @Override
   public void init(IModule<ModuleJetpackUnit> module, ModuleConfigItemCreator configItemCreator) {
      this.jetpackMode = configItemCreator.createConfigItem(
         "jetpack_mode", MekanismLang.MODULE_JETPACK_MODE, new ModuleEnumData<>(IJetpackItem.JetpackMode.NORMAL)
      );
   }

   @Override
   public void addHUDElements(IModule<ModuleJetpackUnit> module, Player player, Consumer<IHUDElement> hudElementAdder) {
      if (module.isEnabled()) {
         ItemStack container = module.getContainer();
         GasStack stored = ((ItemMekaSuitArmor)container.m_41720_()).getContainedGas(container, MekanismGases.HYDROGEN.get());
         double ratio = StorageUtils.getRatio(stored.getAmount(), MekanismConfig.gear.mekaSuitJetpackMaxStorage.getAsLong());
         hudElementAdder.accept(IModuleHelper.INSTANCE.hudElementPercent(this.jetpackMode.get().getHUDIcon(), ratio));
      }
   }

   @Override
   public void changeMode(IModule<ModuleJetpackUnit> module, Player player, ItemStack stack, int shift, boolean displayChangeMessage) {
      IJetpackItem.JetpackMode currentMode = this.getMode();
      IJetpackItem.JetpackMode newMode = currentMode.adjust(shift);
      if (currentMode != newMode) {
         this.jetpackMode.set(newMode);
         if (displayChangeMessage) {
            module.displayModeChange(player, MekanismLang.MODULE_JETPACK_MODE.translate(new Object[0]), newMode);
         }
      }
   }

   public IJetpackItem.JetpackMode getMode() {
      return this.jetpackMode.get();
   }
}
