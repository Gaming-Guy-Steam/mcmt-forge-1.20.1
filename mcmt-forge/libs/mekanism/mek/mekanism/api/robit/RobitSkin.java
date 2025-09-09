package mekanism.api.robit;

import com.mojang.serialization.Codec;
import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.TextComponentUtil;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public interface RobitSkin {
   Codec<? extends RobitSkin> codec();

   @Nullable
   default ResourceLocation customModel() {
      return null;
   }

   List<ResourceLocation> textures();

   default boolean isUnlocked(@NotNull Player player) {
      return true;
   }

   static String getTranslationKey(ResourceKey<? extends RobitSkin> key) {
      return Util.m_137492_("robit_skin", key.m_135782_());
   }

   static Component getTranslatedName(ResourceKey<? extends RobitSkin> key) {
      return TextComponentUtil.translate(getTranslationKey(key));
   }
}
