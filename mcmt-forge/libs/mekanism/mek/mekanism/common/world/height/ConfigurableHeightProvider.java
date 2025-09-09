package mekanism.common.world.height;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.WorldConfig;
import mekanism.common.registries.MekanismHeightProviderTypes;
import mekanism.common.resource.ore.OreType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.HeightProviderType;
import org.jetbrains.annotations.NotNull;

public class ConfigurableHeightProvider extends HeightProvider {
   public static final Codec<ConfigurableHeightProvider> CODEC = RecordCodecBuilder.create(
      builder -> builder.group(OreType.OreVeinType.CODEC.fieldOf("oreVeinType").forGetter(config -> config.oreVeinType))
         .apply(builder, type -> new ConfigurableHeightProvider(type, MekanismConfig.world.getVeinConfig(type)))
   );
   private final OreType.OreVeinType oreVeinType;
   private final ConfigurableHeightRange range;
   private LongSet warnedFor;

   private ConfigurableHeightProvider(OreType.OreVeinType oreVeinType, WorldConfig.OreVeinConfig oreConfig) {
      this.oreVeinType = oreVeinType;
      this.range = oreConfig.range();
   }

   public static ConfigurableHeightProvider of(OreType.OreVeinType type, WorldConfig.OreVeinConfig oreConfig) {
      return new ConfigurableHeightProvider(type, oreConfig);
   }

   public int m_213859_(@NotNull RandomSource random, @NotNull WorldGenerationContext context) {
      int min = this.range.minInclusive().resolveY(context);
      int max = this.range.maxInclusive().resolveY(context);
      if (min > max) {
         if (this.warnedFor == null) {
            this.warnedFor = new LongOpenHashSet();
         }

         if (this.warnedFor.add((long)min << 32 | max)) {
            Mekanism.logger.warn("Empty height range: {}", this);
         }

         return min;
      } else {
         return switch ((HeightShape)this.range.shape().get()) {
            case TRAPEZOID -> this.sampleTrapezoid(random, min, max);
            case UNIFORM -> Mth.m_216287_(random, min, max);
         };
      }
   }

   private int sampleTrapezoid(@NotNull RandomSource random, int min, int max) {
      int plateau = this.range.plateau().getAsInt();
      int range = max - min;
      if (plateau >= range) {
         return Mth.m_216287_(random, min, max);
      } else {
         int middle = (range - plateau) / 2;
         return min + Mth.m_216287_(random, 0, range - middle) + Mth.m_216287_(random, 0, middle);
      }
   }

   @NotNull
   public HeightProviderType<?> m_142002_() {
      return MekanismHeightProviderTypes.CONFIGURABLE.get();
   }

   public String toString() {
      switch ((HeightShape)this.range.shape().get()) {
         case TRAPEZOID:
            int plateau = this.range.plateau().getAsInt();
            if (plateau == 0) {
               return this.oreVeinType.name() + " triangle [" + this.range.minInclusive() + "-" + this.range.maxInclusive() + "]";
            }

            return this.oreVeinType.name() + " trapezoid(" + plateau + ") in [" + this.range.minInclusive() + "-" + this.range.maxInclusive() + "]";
         case UNIFORM:
            return this.oreVeinType.name() + " uniform [" + this.range.minInclusive() + "-" + this.range.maxInclusive() + "]";
         default:
            return this.oreVeinType.name();
      }
   }
}
