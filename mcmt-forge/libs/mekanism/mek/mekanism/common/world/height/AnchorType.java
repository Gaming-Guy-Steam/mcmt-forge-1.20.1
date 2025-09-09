package mekanism.common.world.height;

import net.minecraft.world.level.levelgen.WorldGenerationContext;

public enum AnchorType {
   ABSOLUTE((context, value) -> value),
   ABOVE_BOTTOM((context, value) -> context.m_142201_() + value),
   BELOW_TOP((context, value) -> context.m_142208_() - 1 + context.m_142201_() - value);

   private final AnchorType.YResolver yResolver;

   private AnchorType(AnchorType.YResolver yResolver) {
      this.yResolver = yResolver;
   }

   public int resolveY(WorldGenerationContext context, int value) {
      return this.yResolver.resolve(context, value);
   }

   @FunctionalInterface
   private interface YResolver {
      int resolve(WorldGenerationContext context, int value);
   }
}
