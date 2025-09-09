package mekanism.common.world.height;

import java.util.function.IntSupplier;
import java.util.function.Supplier;
import mekanism.common.config.IMekanismConfig;
import mekanism.common.config.value.CachedEnumValue;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.resource.ore.BaseOreConfig;
import net.minecraftforge.common.ForgeConfigSpec.Builder;

public record ConfigurableHeightRange(
   Supplier<HeightShape> shape, ConfigurableVerticalAnchor minInclusive, ConfigurableVerticalAnchor maxInclusive, IntSupplier plateau
) {
   public static ConfigurableHeightRange create(IMekanismConfig config, Builder builder, String veinType, BaseOreConfig baseConfig) {
      CachedEnumValue<HeightShape> shape = CachedEnumValue.wrap(
         config, builder.comment("Distribution shape for placing " + veinType + "s.").defineEnum("shape", baseConfig.shape())
      );
      ConfigurableVerticalAnchor minInclusive = ConfigurableVerticalAnchor.create(
         config, builder, "minInclusive", "Minimum (inclusive) height anchor for " + veinType + "s.", baseConfig.min(), null
      );
      return new ConfigurableHeightRange(
         shape,
         minInclusive,
         ConfigurableVerticalAnchor.create(
            config, builder, "maxInclusive", "Maximum (inclusive) height anchor for " + veinType + "s.", baseConfig.max(), minInclusive
         ),
         CachedIntValue.wrap(
            config,
            builder.comment("Half length of short side of trapezoid, only used if shape is TRAPEZOID. A value of zero means the shape is a triangle.")
               .define("plateau", baseConfig.plateau(), o -> {
                  if (o instanceof Integer value) {
                     return value == 0 ? true : value > 0 && shape.get() == HeightShape.TRAPEZOID;
                  } else {
                     return false;
                  }
               })
         )
      );
   }
}
