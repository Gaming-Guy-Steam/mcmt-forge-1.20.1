package mekanism.common.world.height;

import java.util.function.IntSupplier;
import java.util.function.Supplier;
import mekanism.common.config.IMekanismConfig;
import mekanism.common.config.value.CachedEnumValue;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.resource.ore.OreAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import org.jetbrains.annotations.Nullable;

public record ConfigurableVerticalAnchor(Supplier<AnchorType> anchorType, IntSupplier value) {
   public static ConfigurableVerticalAnchor create(
      IMekanismConfig config, Builder builder, String path, String comment, OreAnchor defaultAnchor, @Nullable ConfigurableVerticalAnchor minAnchor
   ) {
      builder.comment(comment).push(path);
      CachedEnumValue<AnchorType> type = CachedEnumValue.wrap(
         config,
         builder.comment(
               new String[]{"Type of anchor.", "Absolute -> y = value", "Above Bottom -> y = minY + value", "Below Top -> y = depth - 1 + minY - value"}
            )
            .defineEnum("type", defaultAnchor.type())
      );
      Builder valueBuilder = builder.comment("Value used for calculating y for the anchor based on the type.");
      ConfigValue<Integer> value;
      if (minAnchor == null) {
         value = valueBuilder.define("value", defaultAnchor.value());
      } else {
         value = valueBuilder.define(
            "value",
            defaultAnchor.value(),
            o -> !(o instanceof Integer v) ? false : minAnchor.anchorType.get() != type.get() || v >= minAnchor.value.getAsInt()
         );
      }

      builder.pop();
      return new ConfigurableVerticalAnchor(type, CachedIntValue.wrap(config, value));
   }

   public int resolveY(WorldGenerationContext context) {
      return this.anchorType.get().resolveY(context, this.value.getAsInt());
   }

   @Override
   public String toString() {
      return switch ((AnchorType)this.anchorType.get()) {
         case ABSOLUTE -> this.value.getAsInt() + " absolute";
         case ABOVE_BOTTOM -> this.value.getAsInt() + " above bottom";
         case BELOW_TOP -> this.value.getAsInt() + " below top";
      };
   }
}
