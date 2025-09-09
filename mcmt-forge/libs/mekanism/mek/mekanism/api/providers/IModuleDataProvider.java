package mekanism.api.providers;

import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.ModuleData;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;

@MethodsReturnNonnullByDefault
public interface IModuleDataProvider<MODULE extends ICustomModule<MODULE>> extends IBaseProvider {
   ModuleData<MODULE> getModuleData();

   @Override
   default ResourceLocation getRegistryName() {
      return this.getModuleData().getRegistryName();
   }

   @Override
   default String getTranslationKey() {
      return this.getModuleData().getTranslationKey();
   }
}
