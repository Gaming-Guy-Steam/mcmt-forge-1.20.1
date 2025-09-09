package mekanism.common.registries;

import com.mojang.serialization.Codec;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import mekanism.api.MekanismAPI;
import mekanism.api.robit.AdvancementBasedRobitSkin;
import mekanism.api.robit.RobitSkin;
import mekanism.api.robit.RobitSkinSerializationHelper;
import mekanism.common.entity.RobitPrideSkinData;
import mekanism.common.registration.impl.RobitSkinDeferredRegister;
import mekanism.common.registration.impl.RobitSkinSerializerRegistryObject;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;

public class MekanismRobitSkins {
   private static final RobitSkinDeferredRegister ROBIT_SKINS = new RobitSkinDeferredRegister("mekanism");
   private static Codec<RobitSkin> DIRECT_CODEC;
   public static final RobitSkinSerializerRegistryObject<RobitSkin> BASIC_SERIALIZER = ROBIT_SKINS.registerSerializer(
      "basic", () -> RobitSkinSerializationHelper.NETWORK_CODEC
   );
   public static final RobitSkinSerializerRegistryObject<AdvancementBasedRobitSkin> ADVANCEMENT_BASED_SERIALIZER = ROBIT_SKINS.registerSerializer(
      "advancement_based", () -> RobitSkinSerializationHelper.ADVANCEMENT_BASED_ROBIT_SKIN_CODEC
   );
   public static final ResourceKey<RobitSkin> BASE = ROBIT_SKINS.dataKey("robit");
   public static final ResourceKey<RobitSkin> ALLAY = ROBIT_SKINS.dataKey("allay");
   public static final Map<RobitPrideSkinData, ResourceKey<RobitSkin>> PRIDE_SKINS = (Map<RobitPrideSkinData, ResourceKey<RobitSkin>>)Util.m_137537_(() -> {
      Map<RobitPrideSkinData, ResourceKey<RobitSkin>> internal = new EnumMap<>(RobitPrideSkinData.class);

      for (RobitPrideSkinData data : RobitPrideSkinData.values()) {
         internal.put(data, ROBIT_SKINS.dataKey(data.lowerCaseName()));
      }

      return Collections.unmodifiableMap(internal);
   });

   private MekanismRobitSkins() {
   }

   public static void createAndRegisterDatapack(IEventBus modEventBus) {
      DIRECT_CODEC = ROBIT_SKINS.createAndRegisterDatapack(modEventBus, RobitSkin::codec, RobitSkinSerializationHelper.NETWORK_CODEC);
   }

   public static Codec<RobitSkin> getDirectCodec() {
      return DIRECT_CODEC;
   }

   public static MekanismRobitSkins.SkinLookup lookup(RegistryAccess registryAccess, ResourceKey<RobitSkin> key) {
      Registry<RobitSkin> robitSkins = registryAccess.m_175515_(MekanismAPI.ROBIT_SKIN_REGISTRY_NAME);
      RobitSkin skin = (RobitSkin)robitSkins.m_6246_(key);
      return skin == null ? new MekanismRobitSkins.SkinLookup(BASE, (RobitSkin)robitSkins.m_123013_(BASE)) : new MekanismRobitSkins.SkinLookup(key, skin);
   }

   public record SkinLookup(ResourceKey<RobitSkin> name, RobitSkin skin) {
      public ResourceLocation location() {
         return this.name.m_135782_();
      }
   }
}
