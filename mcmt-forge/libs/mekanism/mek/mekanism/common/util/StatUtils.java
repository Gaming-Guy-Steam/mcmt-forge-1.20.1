package mekanism.common.util;

import java.util.Random;

public class StatUtils {
   private static final Random rand = new Random();
   private static final double STIRLING_COEFF = 1.0 / Math.sqrt(Math.PI * 2);

   private StatUtils() {
   }

   public static int inversePoisson(double mean) {
      double r = rand.nextDouble() * Math.exp(mean);
      int m = 0;
      double p = 1.0;
      double stirlingValue = mean * Math.E;

      for (double mBound = 3.0 * Math.ceil(mean); p < r && m < mBound; p += STIRLING_COEFF / Math.sqrt(m) * Math.pow(stirlingValue / m, m)) {
         m++;
      }

      return m;
   }

   public static double min(double... vals) {
      double min = vals[0];

      for (int i = 1; i < vals.length; i++) {
         min = Math.min(min, vals[i]);
      }

      return min;
   }

   public static double max(double... vals) {
      double max = vals[0];

      for (int i = 1; i < vals.length; i++) {
         max = Math.max(max, vals[i]);
      }

      return max;
   }

   public static float wrapDegrees(float angle) {
      angle %= 360.0F;
      if (angle < 0.0F) {
         angle += 360.0F;
      }

      return angle;
   }
}
