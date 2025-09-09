package mekanism.common.content.gear.shared;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleColorData;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.common.MekanismLang;
import mekanism.common.lib.Color;

@ParametersAreNotNullByDefault
public class ModuleColorModulationUnit implements ICustomModule<ModuleColorModulationUnit> {
   public static final String COLOR_CONFIG_KEY = "color";
   private IModuleConfigItem<Integer> color;

   @Override
   public void init(IModule<ModuleColorModulationUnit> module, ModuleConfigItemCreator configItemCreator) {
      this.color = configItemCreator.createConfigItem("color", MekanismLang.MODULE_COLOR, ModuleColorData.argb());
   }

   public Color getColor() {
      return Color.argb(this.color.get());
   }
}
