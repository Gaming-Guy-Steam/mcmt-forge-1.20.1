package mekanism.common.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.Color;
import net.minecraft.util.Mth;

public class HeatUtils {
   public static final Int2ObjectMap<Color> colorCache = new Int2ObjectOpenHashMap();
   public static final double BASE_BOIL_TEMP = UnitDisplayUtils.TemperatureUnit.CELSIUS.zeroOffset + 100.0;
   public static final double HEATED_COOLANT_TEMP = 100000.0;

   private HeatUtils() {
   }

   public static double getWaterThermalEnthalpy() {
      return MekanismConfig.general.maxEnergyPerSteam.get().doubleValue();
   }

   public static double getSteamEnergyEfficiency() {
      return 0.2;
   }

   public static Color getColorFromTemp(double temperature, Color baseColor) {
      double absTemp = temperature + 300.0;
      absTemp /= 100.0;
      if (colorCache.containsKey((int)absTemp)) {
         return ((Color)colorCache.get((int)absTemp)).blendOnto(baseColor);
      } else {
         double effectiveTemp = absTemp;
         if (absTemp < 10.0) {
            effectiveTemp = 10.0;
         }

         if (effectiveTemp > 400.0) {
            effectiveTemp = 400.0;
         }

         double red;
         if (effectiveTemp <= 66.0) {
            red = 1.0;
         } else {
            double tmpCalc = effectiveTemp - 60.0;
            tmpCalc = 329.698727446 * Math.pow(tmpCalc, -0.1332047592);
            red = tmpCalc / 255.0;
         }

         double tmpCalc;
         if (effectiveTemp <= 66.0) {
            tmpCalc = 99.4708025861 * Math.log(effectiveTemp) - 161.1195681661;
         } else {
            tmpCalc = effectiveTemp - 60.0;
            tmpCalc = 288.1221695283 * Math.pow(tmpCalc, -0.0755148492);
         }

         double green = tmpCalc / 255.0;
         double blue;
         if (effectiveTemp >= 66.0) {
            blue = 1.0;
         } else if (effectiveTemp <= 19.0) {
            blue = 0.0;
         } else {
            tmpCalc = effectiveTemp - 10.0;
            tmpCalc = 138.5177312231 * Math.log(tmpCalc) - 305.0447927307;
            blue = tmpCalc / 255.0;
         }

         double alpha = temperature / 1000.0;
         red = Mth.m_14008_(red, 0.0, 1.0);
         green = Mth.m_14008_(green, 0.0, 1.0);
         blue = Mth.m_14008_(blue, 0.0, 1.0);
         alpha = Mth.m_14008_(alpha, 0.0, 1.0);
         Color colorTemperature = Color.rgbad(red, green, blue, alpha);
         colorCache.put((int)absTemp, colorTemperature);
         return colorTemperature.blendOnto(baseColor);
      }
   }
}
