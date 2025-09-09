package mekanism.common.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.registries.MekanismIntProviderTypes;
import mekanism.common.resource.ore.OreType;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviderType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConfigurableConstantInt extends IntProvider {
   public static final Codec<ConfigurableConstantInt> CODEC = RecordCodecBuilder.create(
      builder -> builder.group(OreType.OreVeinType.CODEC.optionalFieldOf("oreVeinType").forGetter(config -> Optional.ofNullable(config.oreVeinType)))
         .apply(builder, oreType -> {
            if (oreType.isPresent()) {
               OreType.OreVeinType type = (OreType.OreVeinType)oreType.get();
               return new ConfigurableConstantInt(type, MekanismConfig.world.getVeinConfig(type).perChunk());
            } else {
               return new ConfigurableConstantInt(null, MekanismConfig.world.salt.perChunk);
            }
         })
   );
   @Nullable
   private final OreType.OreVeinType oreVeinType;
   private final CachedIntValue value;

   public ConfigurableConstantInt(@Nullable OreType.OreVeinType oreVeinType, CachedIntValue value) {
      this.oreVeinType = oreVeinType;
      this.value = value;
   }

   public int getValue() {
      return this.value.getOrDefault();
   }

   public int m_214085_(@NotNull RandomSource random) {
      return this.getValue();
   }

   public int m_142739_() {
      return this.getValue();
   }

   public int m_142737_() {
      return this.getValue();
   }

   @NotNull
   public IntProviderType<?> m_141948_() {
      return MekanismIntProviderTypes.CONFIGURABLE_CONSTANT.get();
   }

   public String toString() {
      return Integer.toString(this.getValue());
   }
}
