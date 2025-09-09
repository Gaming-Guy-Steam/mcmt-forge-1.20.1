package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.providers.IBaseProvider;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType.Getter;
import org.openzen.zencode.java.ZenCodeType.Method;

@ZenRegister
@NativeTypeRegistration(
   value = IBaseProvider.class,
   zenCodeName = "mods.mekanism.api.provider.BaseProvider"
)
public class CrTBaseProvider {
   @Method
   @Getter("registryName")
   public static ResourceLocation getRegistryName(IBaseProvider internal) {
      return internal.getRegistryName();
   }
}
