package mekanism.api.robit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import mekanism.api.IMekanismAccess;
import mekanism.api.MekanismAPI;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

public class RobitSkinSerializationHelper {
   public static final Codec<RobitSkin> DIRECT_CODEC = ExtraCodecs.m_184415_(IMekanismAccess.INSTANCE::robitSkinCodec);
   public static final Codec<Holder<RobitSkin>> REFERENCE_CODEC = RegistryFileCodec.m_135589_(MekanismAPI.ROBIT_SKIN_REGISTRY_NAME, DIRECT_CODEC);
   public static final Codec<HolderSet<RobitSkin>> LIST_CODEC = RegistryCodecs.m_206279_(MekanismAPI.ROBIT_SKIN_REGISTRY_NAME, DIRECT_CODEC);
   public static final Codec<RobitSkin> NETWORK_CODEC = RecordCodecBuilder.create(
      builder -> builder.group(
            ExtraCodecs.m_144637_(ResourceLocation.f_135803_.listOf()).fieldOf("textures").forGetter(RobitSkin::textures),
            ResourceLocation.f_135803_.optionalFieldOf("customModel").forGetter(skin -> Optional.ofNullable(skin.customModel()))
         )
         .apply(builder, (textures, model) -> new BasicRobitSkin(textures, (ResourceLocation)model.orElse(null)))
   );
   public static final Codec<AdvancementBasedRobitSkin> ADVANCEMENT_BASED_ROBIT_SKIN_CODEC = RecordCodecBuilder.create(
      builder -> builder.group(
            ExtraCodecs.m_144637_(ResourceLocation.f_135803_.listOf()).fieldOf("textures").forGetter(RobitSkin::textures),
            ResourceLocation.f_135803_.optionalFieldOf("customModel").forGetter(skin -> Optional.ofNullable(skin.customModel())),
            ResourceLocation.f_135803_.fieldOf("advancement").forGetter(AdvancementBasedRobitSkin::advancement)
         )
         .apply(builder, (textures, model, advancement) -> new AdvancementBasedRobitSkin(textures, (ResourceLocation)model.orElse(null), advancement))
   );

   private RobitSkinSerializationHelper() {
   }
}
