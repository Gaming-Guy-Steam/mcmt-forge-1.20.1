package mekanism.common.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismPlacementModifiers;
import mekanism.common.resource.ore.OreType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DisableableFeaturePlacement extends PlacementFilter {
   public static final Codec<DisableableFeaturePlacement> CODEC = RecordCodecBuilder.create(
      builder -> builder.group(
            OreType.OreVeinType.CODEC.optionalFieldOf("oreVeinType").forGetter(config -> Optional.ofNullable(config.oreVeinType)),
            Codec.BOOL.fieldOf("retroGen").forGetter(config -> config.retroGen)
         )
         .apply(builder, (oreType, retroGen) -> {
            if (oreType.isPresent()) {
               OreType.OreVeinType type = (OreType.OreVeinType)oreType.get();
               return new DisableableFeaturePlacement(type, MekanismConfig.world.getVeinConfig(type).shouldGenerate(), retroGen);
            } else {
               return new DisableableFeaturePlacement(null, MekanismConfig.world.salt.shouldGenerate, retroGen);
            }
         })
   );
   private final BooleanSupplier enabledSupplier;
   @Nullable
   private final OreType.OreVeinType oreVeinType;
   private final boolean retroGen;

   public DisableableFeaturePlacement(@Nullable OreType.OreVeinType oreVeinType, BooleanSupplier enabledSupplier, boolean retroGen) {
      this.oreVeinType = oreVeinType;
      this.enabledSupplier = enabledSupplier;
      this.retroGen = retroGen;
   }

   protected boolean m_213917_(@NotNull PlacementContext context, @NotNull RandomSource random, @NotNull BlockPos pos) {
      return !this.enabledSupplier.getAsBoolean() ? false : !this.retroGen || MekanismConfig.world.enableRegeneration.get();
   }

   @NotNull
   public PlacementModifierType<?> m_183327_() {
      return (PlacementModifierType<?>)MekanismPlacementModifiers.DISABLEABLE.get();
   }
}
