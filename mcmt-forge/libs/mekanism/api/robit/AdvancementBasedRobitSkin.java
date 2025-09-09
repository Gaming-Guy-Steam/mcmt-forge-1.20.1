package mekanism.api.robit;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public record AdvancementBasedRobitSkin(List<ResourceLocation> textures, @Nullable ResourceLocation customModel, ResourceLocation advancement)
   implements RobitSkin {
   public AdvancementBasedRobitSkin(List<ResourceLocation> textures, @Nullable ResourceLocation customModel, ResourceLocation advancement) {
      Objects.requireNonNull(advancement, "Required advancement cannot be null.");
      Objects.requireNonNull(textures, "Textures cannot be null.");
      if (textures.isEmpty()) {
         throw new IllegalArgumentException("There must be at least one texture specified.");
      } else {
         textures = List.copyOf(textures);
         this.textures = textures;
         this.customModel = customModel;
         this.advancement = advancement;
      }
   }

   public AdvancementBasedRobitSkin(List<ResourceLocation> textures, ResourceLocation advancement) {
      this(textures, null, advancement);
   }

   @Override
   public Codec<? extends RobitSkin> codec() {
      return RobitSkinSerializationHelper.ADVANCEMENT_BASED_ROBIT_SKIN_CODEC;
   }

   @Override
   public boolean isUnlocked(@NotNull Player player) {
      if (player instanceof ServerPlayer serverPlayer) {
         MinecraftServer server = serverPlayer.m_20194_();
         if (server != null) {
            Advancement advancement = server.m_129889_().m_136041_(this.advancement());
            return advancement != null && serverPlayer.m_8960_().m_135996_(advancement).m_8193_();
         }
      }

      return true;
   }
}
