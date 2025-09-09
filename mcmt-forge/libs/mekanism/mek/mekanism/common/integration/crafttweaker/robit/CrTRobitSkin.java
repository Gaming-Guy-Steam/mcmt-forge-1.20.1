package mekanism.common.integration.crafttweaker.robit;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.MekanismAPI;
import mekanism.api.robit.RobitSkin;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType.Method;

@ZenRegister
@NativeTypeRegistration(
   value = RobitSkin.class,
   zenCodeName = "mods.mekanism.api.entity.robit.RobitSkin"
)
public class CrTRobitSkin {
   @Method
   public static ResourceLocation getRegistryName(RobitSkin _this) {
      ResourceLocation skinName = CraftTweakerAPI.getAccessibleElementsProvider()
         .registryAccess()
         .m_175515_(MekanismAPI.ROBIT_SKIN_REGISTRY_NAME)
         .m_7981_(_this);
      if (skinName == null) {
         throw new IllegalArgumentException("Unregistered robit skin");
      } else {
         return skinName;
      }
   }
}
