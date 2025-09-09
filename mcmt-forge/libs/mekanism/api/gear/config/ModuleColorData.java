package mekanism.api.gear.config;

import mekanism.api.annotations.NothingNullByDefault;

@NothingNullByDefault
public final class ModuleColorData extends ModuleIntegerData {
   private final boolean handlesAlpha;

   public static ModuleColorData argb() {
      return argb(-1);
   }

   public static ModuleColorData argb(int defaultColor) {
      return new ModuleColorData(defaultColor, true);
   }

   public static ModuleColorData rgb() {
      return rgb(-1);
   }

   public static ModuleColorData rgb(int defaultColor) {
      return new ModuleColorData(defaultColor, false);
   }

   private ModuleColorData(int defaultColor, boolean handlesAlpha) {
      super(handlesAlpha ? defaultColor : defaultColor | 0xFF000000);
      this.handlesAlpha = handlesAlpha;
   }

   @Override
   protected int sanitizeValue(int value) {
      return this.handlesAlpha ? value : value | 0xFF000000;
   }

   public boolean handlesAlpha() {
      return this.handlesAlpha;
   }
}
