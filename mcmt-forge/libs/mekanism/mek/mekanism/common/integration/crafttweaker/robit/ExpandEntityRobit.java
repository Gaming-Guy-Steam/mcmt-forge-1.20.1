package mekanism.common.integration.crafttweaker.robit;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.MekanismAPI;
import mekanism.api.robit.RobitSkin;
import mekanism.common.entity.EntityRobit;
import mekanism.common.registries.MekanismRobitSkins;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.openzen.zencode.java.ZenCodeType.Getter;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Nullable;

@ZenRegister
@NativeTypeRegistration(
   value = EntityRobit.class,
   zenCodeName = "mods.mekanism.api.entity.robit.Robit"
)
public class ExpandEntityRobit {
   @Method
   @Getter("skin")
   public static RobitSkin getSkin(EntityRobit internal) {
      return MekanismRobitSkins.lookup(internal.m_9236_().m_9598_(), internal.getSkin()).skin();
   }

   @Method
   @Getter("skinName")
   public static ResourceLocation getSkinName(EntityRobit internal) {
      return MekanismRobitSkins.lookup(internal.m_9236_().m_9598_(), internal.getSkin()).location();
   }

   @Method
   public static boolean setSkin(EntityRobit internal, RobitSkin skin, @Nullable Player player) {
      ResourceKey<Registry<RobitSkin>> registryName = MekanismAPI.ROBIT_SKIN_REGISTRY_NAME;
      ResourceLocation skinName = internal.m_9236_().m_9598_().m_175515_(registryName).m_7981_(skin);
      if (skinName == null) {
         throw new IllegalArgumentException("Unregistered robit skin");
      } else {
         return internal.setSkin(ResourceKey.m_135785_(registryName, skinName), player);
      }
   }

   @Method
   public static boolean setSkin(EntityRobit internal, ResourceLocation skin, @Nullable Player player) {
      ResourceKey<Registry<RobitSkin>> registryName = MekanismAPI.ROBIT_SKIN_REGISTRY_NAME;
      ResourceKey<RobitSkin> skinKey = ResourceKey.m_135785_(registryName, skin);
      if (!internal.m_9236_().m_9598_().m_175515_(registryName).m_142003_(skinKey)) {
         throw new IllegalArgumentException("Unknown robit skin with name: " + skin);
      } else {
         return internal.setSkin(skinKey, player);
      }
   }
}
