package mekanism.common.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.IntSupplier;
import mekanism.api.functions.FloatSupplier;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.WorldConfig;
import mekanism.common.resource.ore.OreType;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration.TargetBlockState;

public record ResizableOreFeatureConfig(
   List<TargetBlockState> targetStates, OreType.OreVeinType oreVeinType, IntSupplier size, FloatSupplier discardChanceOnAirExposure
) implements FeatureConfiguration {
   public static final Codec<ResizableOreFeatureConfig> CODEC = RecordCodecBuilder.create(
      builder -> builder.group(
            Codec.list(TargetBlockState.f_161031_).fieldOf("targets").forGetter(config -> config.targetStates),
            OreType.OreVeinType.CODEC.fieldOf("oreVeinType").forGetter(config -> config.oreVeinType)
         )
         .apply(builder, (targetStates, oreVeinType) -> {
            WorldConfig.OreVeinConfig veinConfig = MekanismConfig.world.getVeinConfig(oreVeinType);
            return new ResizableOreFeatureConfig(targetStates, oreVeinType, veinConfig.maxVeinSize(), veinConfig.discardChanceOnAirExposure());
         })
   );
}
