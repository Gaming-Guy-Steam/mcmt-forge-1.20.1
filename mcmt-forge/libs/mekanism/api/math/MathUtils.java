package mekanism.api.math;

import java.util.List;

public class MathUtils {
   private static final long UNSIGNED_MASK = Long.MAX_VALUE;

   private MathUtils() {
   }

   public static int clampToInt(double d) {
      return d < 2.147483647E9 ? (int)d : Integer.MAX_VALUE;
   }

   public static int clampToInt(long l) {
      return l < 2147483647L ? (int)l : Integer.MAX_VALUE;
   }

   public static long clampToLong(double d) {
      return d < 9.223372E18F ? (long)d : Long.MAX_VALUE;
   }

   public static int clampUnsignedToInt(long l) {
      return l >= 0L && l <= 2147483647L ? (int)l : Integer.MAX_VALUE;
   }

   public static long clampUnsignedToLong(long l) {
      return l < 0L ? Long.MAX_VALUE : l;
   }

   public static float unsignedLongToFloat(long l) {
      float fValue = (float)(l & Long.MAX_VALUE);
      if (l < 0L) {
         fValue += 9.223372E18F;
      }

      return fValue;
   }

   public static double unsignedLongToDouble(long l) {
      double dValue = l & Long.MAX_VALUE;
      if (l < 0L) {
         dValue += 9.223372E18F;
      }

      return dValue;
   }

   public static <TYPE> TYPE getByIndexMod(TYPE[] elements, int index) {
      return index < 0 ? elements[Math.floorMod(index, elements.length)] : elements[index % elements.length];
   }

   public static <TYPE> TYPE getByIndexMod(List<TYPE> elements, int index) {
      return index < 0 ? elements.get(Math.floorMod(index, elements.size())) : elements.get(index % elements.size());
   }
}
