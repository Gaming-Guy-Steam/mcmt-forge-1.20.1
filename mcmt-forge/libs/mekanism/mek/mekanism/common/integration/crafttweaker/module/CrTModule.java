package mekanism.common.integration.crafttweaker.module;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeMethod;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import com.blamejared.crafttweaker_annotations.annotations.NativeMethod.Holder;
import mekanism.api.gear.IModule;

@ZenRegister
@Holder({@NativeMethod(
      name = "getData",
      parameters = {},
      getterName = "data"
   ), @NativeMethod(
      name = "getInstalledCount",
      parameters = {},
      getterName = "installed"
   ), @NativeMethod(
      name = "isEnabled",
      parameters = {},
      getterName = "enabled"
   )})
@NativeTypeRegistration(
   value = IModule.class,
   zenCodeName = "mods.mekanism.api.gear.Module"
)
public class CrTModule {
}
