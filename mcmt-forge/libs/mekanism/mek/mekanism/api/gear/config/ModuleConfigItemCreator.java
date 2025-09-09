package mekanism.api.gear.config;

import java.util.function.BooleanSupplier;
import mekanism.api.text.ILangEntry;

public interface ModuleConfigItemCreator {
   <TYPE> IModuleConfigItem<TYPE> createConfigItem(String var1, ILangEntry var2, ModuleConfigData<TYPE> var3);

   IModuleConfigItem<Boolean> createDisableableConfigItem(String var1, ILangEntry var2, boolean var3, BooleanSupplier var4);
}
