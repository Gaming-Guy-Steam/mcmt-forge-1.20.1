package mekanism.common.integration.crafttweaker.module;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeMethod;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import com.blamejared.crafttweaker_annotations.annotations.NativeMethod.Holder;
import mekanism.api.gear.ModuleData;

@ZenRegister
@Holder({@NativeMethod(
      name = "getRarity",
      parameters = {},
      getterName = "rarity"
   ), @NativeMethod(
      name = "getMaxStackSize",
      parameters = {},
      getterName = "maxStackSize"
   ), @NativeMethod(
      name = "isExclusive",
      parameters = {},
      getterName = "exclusive"
   )})
@NativeTypeRegistration(
   value = ModuleData.class,
   zenCodeName = "mods.mekanism.api.gear.ModuleData"
)
public class CrTModuleData {
}
