package mekanism.api.robit;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public record BasicRobitSkin(List<ResourceLocation> textures, @Nullable ResourceLocation customModel) implements RobitSkin {
   public BasicRobitSkin(List<ResourceLocation> textures, @Nullable ResourceLocation customModel) {
      Objects.requireNonNull(textures, "Textures cannot be null.");
      if (textures.isEmpty()) {
         throw new IllegalArgumentException("There must be at least one texture specified.");
      } else {
         textures = List.copyOf(textures);
         this.textures = textures;
         this.customModel = customModel;
      }
   }

   public BasicRobitSkin(List<ResourceLocation> textures) {
      this(textures, null);
   }

   @Override
   public Codec<? extends RobitSkin> codec() {
      return RobitSkinSerializationHelper.NETWORK_CODEC;
   }
}
