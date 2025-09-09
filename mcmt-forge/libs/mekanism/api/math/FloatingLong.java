package mekanism.api.math;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.network.FriendlyByteBuf;

@NothingNullByDefault
public class FloatingLong extends Number implements Comparable<FloatingLong> {
   private static final DecimalFormat df = new DecimalFormat("0.0000", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
   private static final int DECIMAL_DIGITS = 4;
   private static final short MAX_DECIMAL = 9999;
   private static final short SINGLE_UNIT = 10000;
   private static final long MAX_LONG_SHIFT = Long.divideUnsigned(Long.divideUnsigned(-1L, 10000L), 10000L);
   public static final FloatingLong ZERO = createConst(0L);
   public static final FloatingLong ONE = createConst(1L);
   public static final FloatingLong MAX_VALUE = createConst(-1L, (short)9999);
   private static final double MAX_AS_DOUBLE = Double.parseDouble(MAX_VALUE.toString());
   private final boolean isConstant;
   private long value;
   private short decimal;

   public static FloatingLong create(double value) {
      if (value > MAX_AS_DOUBLE) {
         return MAX_VALUE;
      } else if (value < 0.0) {
         return ZERO;
      } else {
         long lValue = (long)value;
         short decimal = parseDecimal(df.format(value));
         return create(lValue, decimal);
      }
   }

   public static FloatingLong create(long value) {
      return new FloatingLong(value, (short)0, false);
   }

   public static FloatingLong create(long value, short decimal) {
      return new FloatingLong(value, clampDecimal(decimal), false);
   }

   public static FloatingLong createConst(double value) {
      if (value > MAX_AS_DOUBLE) {
         return MAX_VALUE;
      } else if (value < 0.0) {
         return ZERO;
      } else {
         long lValue = (long)value;
         short decimal = parseDecimal(df.format(value));
         return createConst(lValue, decimal);
      }
   }

   public static FloatingLong createConst(long value) {
      return new FloatingLong(value, (short)0, true);
   }

   public static FloatingLong createConst(long value, short decimal) {
      return new FloatingLong(value, clampDecimal(decimal), true);
   }

   public static FloatingLong readFromBuffer(FriendlyByteBuf buffer) {
      return create(buffer.m_130258_(), buffer.readShort());
   }

   private FloatingLong(long value, short decimal, boolean isConstant) {
      this.value = value;
      this.decimal = decimal;
      this.isConstant = isConstant;
   }

   public long getValue() {
      return this.value;
   }

   public short getDecimal() {
      return this.decimal;
   }

   private FloatingLong setAndClampValues(long value, short decimal) {
      if (this.isConstant) {
         return create(value, decimal);
      } else {
         this.value = value;
         this.decimal = clampDecimal(decimal);
         return this;
      }
   }

   private static short clampDecimal(short decimal) {
      if (decimal < 0) {
         return 0;
      } else {
         return decimal > 9999 ? 9999 : decimal;
      }
   }

   public boolean isZero() {
      return this.value == 0L && this.decimal <= 0;
   }

   public FloatingLong copy() {
      return new FloatingLong(this.value, this.decimal, false);
   }

   public FloatingLong copyAsConst() {
      return this.isConstant ? this : new FloatingLong(this.value, this.decimal, true);
   }

   public FloatingLong plusEqual(FloatingLong toAdd) {
      return this.plusEqual(toAdd.value, toAdd.decimal);
   }

   private FloatingLong plusEqual(long toAddValue, short toAddDecimal) {
      if (toAddDecimal == 0) {
         return this.plusEqual(toAddValue);
      } else if ((this.value >= 0L || toAddValue >= 0L) && (this.value >= 0L && toAddValue >= 0L || this.value + toAddValue < 0L)) {
         long newValue = this.value + toAddValue;
         short newDecimal = (short)(this.decimal + toAddDecimal);
         if (newDecimal > 9999) {
            if (newValue == -1L) {
               newDecimal = 9999;
            } else {
               newDecimal = (short)(newDecimal - 10000);
               newValue++;
            }
         }

         return this.setAndClampValues(newValue, newDecimal);
      } else {
         return this.isConstant ? MAX_VALUE : this.setAndClampValues(-1L, (short)9999);
      }
   }

   public FloatingLong plusEqual(long toAdd) {
      if (toAdd == 0L) {
         return this;
      } else if (this.value < 0L && toAdd < 0L || (this.value < 0L || toAdd < 0L) && this.value + toAdd >= 0L) {
         return this.isConstant ? MAX_VALUE : this.setAndClampValues(-1L, (short)9999);
      } else {
         return this.setAndClampValues(this.value + toAdd, this.decimal);
      }
   }

   public FloatingLong minusEqual(FloatingLong toSubtract) {
      if (toSubtract.isZero() || this.isZero()) {
         return this;
      } else if (toSubtract.greaterOrEqual(this)) {
         return this.isConstant ? ZERO : this.setAndClampValues(0L, (short)0);
      } else {
         long newValue = this.value - toSubtract.value;
         short newDecimal = (short)(this.decimal - toSubtract.decimal);
         if (newDecimal < 0) {
            newDecimal = (short)(newDecimal + 10000);
            newValue--;
         }

         return this.setAndClampValues(newValue, newDecimal);
      }
   }

   public FloatingLong minusEqual(long toSubtract) {
      if (toSubtract != 0L && !this.isZero()) {
         long comparison = Long.compareUnsigned(this.value, toSubtract);
         if (comparison < 0L || comparison == 0L && this.decimal == 0) {
            return this.isConstant ? ZERO : this.setAndClampValues(0L, (short)0);
         } else {
            return this.setAndClampValues(this.value - toSubtract, this.decimal);
         }
      } else {
         return this;
      }
   }

   public FloatingLong timesEqual(FloatingLong toMultiply) {
      if (this.isZero() || toMultiply.equals(ONE)) {
         return this;
      } else if (toMultiply.isZero()) {
         return this.isConstant ? ZERO : this.setAndClampValues(0L, (short)0);
      } else if (this.equals(ONE)) {
         return this.setAndClampValues(toMultiply.value, toMultiply.decimal);
      } else if (multiplyLongsWillOverFlow(this.value, toMultiply.value)) {
         return this.isConstant ? MAX_VALUE : this.setAndClampValues(-1L, (short)9999);
      } else {
         FloatingLong temp = multiplyLongAndDecimal(this.value, toMultiply.decimal);
         temp = temp.plusEqual(multiplyLongs(this.value, toMultiply.value));
         temp = addLongAndDecimalMultiplication(temp, toMultiply.value, this.decimal);
         temp = temp.plusEqual(0L, multiplyDecimals(this.decimal, toMultiply.decimal));
         return this.isConstant ? temp : this.setAndClampValues(temp.value, temp.decimal);
      }
   }

   public FloatingLong timesEqual(long toMultiply) {
      if (toMultiply == 1L || this.isZero()) {
         return this;
      } else if (toMultiply == 0L) {
         return this.isConstant ? ZERO : this.setAndClampValues(0L, (short)0);
      } else if (this.equals(ONE)) {
         return this.setAndClampValues(toMultiply, (short)0);
      } else if (multiplyLongsWillOverFlow(this.value, toMultiply)) {
         return this.isConstant ? MAX_VALUE : this.setAndClampValues(-1L, (short)9999);
      } else {
         FloatingLong temp = multiplyLongAndDecimal(toMultiply, this.decimal);
         temp = temp.plusEqual(multiplyLongs(this.value, toMultiply));
         return this.isConstant ? temp : this.setAndClampValues(temp.value, temp.decimal);
      }
   }

   public FloatingLong divideEquals(FloatingLong toDivide) {
      if (toDivide.isZero()) {
         throw new ArithmeticException("Division by zero");
      } else if (this.isZero() || toDivide.equals(ONE)) {
         return this;
      } else if (toDivide.decimal == 0) {
         return this.divideEquals(toDivide.value);
      } else {
         BigDecimal divide = new BigDecimal(this.toString()).divide(new BigDecimal(toDivide.toString()), 4, RoundingMode.HALF_UP);
         long value = divide.longValue();
         short decimal = parseDecimal(divide.toPlainString());
         return this.setAndClampValues(value, decimal);
      }
   }

   public FloatingLong divideEquals(long toDivide) {
      if (toDivide == 0L) {
         throw new ArithmeticException("Division by zero");
      } else if (!this.isZero() && toDivide != 1L) {
         long val = Long.divideUnsigned(this.value, toDivide);
         long rem = Long.remainderUnsigned(this.value, toDivide);
         long dec;
         if (Long.compareUnsigned(rem, MAX_LONG_SHIFT / 10L) >= 0) {
            dec = Long.divideUnsigned(rem, Long.divideUnsigned(toDivide, 100000L));
         } else {
            dec = Long.divideUnsigned(rem * 10000L * 10L, toDivide);
            dec += Long.divideUnsigned(this.decimal * 10L, toDivide);
         }

         if (Long.remainderUnsigned(dec, 10L) >= 5L) {
            dec += 10L;
            if (dec >= 100000L) {
               val++;
               dec -= 100000L;
            }
         }

         dec /= 10L;
         return this.setAndClampValues(val, (short)dec);
      } else {
         return this;
      }
   }

   public long divideToUnsignedLong(FloatingLong toDivide) {
      if (toDivide.isZero()) {
         throw new ArithmeticException("Division by zero");
      } else if (toDivide.equals(ONE)) {
         return this.value;
      } else if (this.smallerThan(toDivide)) {
         return 0L;
      } else if (toDivide.greaterThan(ONE)) {
         if (Long.compareUnsigned(toDivide.value, MAX_LONG_SHIFT) <= 0) {
            long div = toDivide.value * 10000L + toDivide.decimal;
            return Long.divideUnsigned(this.value, div) * 10000L + Long.divideUnsigned(Long.remainderUnsigned(this.value, div) * 10000L, div);
         } else if (Long.compareUnsigned(toDivide.value, Long.divideUnsigned(-1L, 2L) + 1L) >= 0) {
            return 1L;
         } else {
            long q = Long.divideUnsigned(this.value, toDivide.value);
            return q != Long.divideUnsigned(this.value, toDivide.value + 1L)
                  && toDivide.value * q + Long.divideUnsigned(toDivide.decimal * q, 9999L) > this.value
               ? q - 1L
               : q;
         }
      } else if (Long.compareUnsigned(this.value, MAX_LONG_SHIFT) >= 0) {
         return Long.divideUnsigned(this.value, toDivide.decimal) * 9999L
            + Long.divideUnsigned(Long.remainderUnsigned(this.value, toDivide.decimal) * 9999L, toDivide.decimal)
            + this.decimal * 9999L / toDivide.decimal;
      } else {
         long d = this.value * 9999L;
         return Long.divideUnsigned(d, toDivide.decimal) + this.decimal * 9999L / toDivide.decimal;
      }
   }

   public long divideToLong(FloatingLong toDivide) {
      return MathUtils.clampUnsignedToLong(this.divideToUnsignedLong(toDivide));
   }

   public int divideToInt(FloatingLong toDivide) {
      return MathUtils.clampUnsignedToInt(this.divideToLong(toDivide));
   }

   public FloatingLong add(FloatingLong toAdd) {
      return this.copy().plusEqual(toAdd);
   }

   public FloatingLong add(long toAdd) {
      return this.copy().plusEqual(toAdd);
   }

   public FloatingLong add(double toAdd) {
      if (toAdd < 0.0) {
         throw new IllegalArgumentException("Addition called with negative number, this is not supported. FloatingLongs are always positive.");
      } else {
         return this.add(create(toAdd));
      }
   }

   public FloatingLong subtract(FloatingLong toSubtract) {
      return this.copy().minusEqual(toSubtract);
   }

   public FloatingLong subtract(long toSubtract) {
      return this.copy().minusEqual(toSubtract);
   }

   public FloatingLong subtract(double toSubtract) {
      if (toSubtract < 0.0) {
         throw new IllegalArgumentException("Subtraction called with negative number, this is not supported. FloatingLongs are always positive.");
      } else {
         return this.subtract(create(toSubtract));
      }
   }

   public FloatingLong multiply(FloatingLong toMultiply) {
      return this.copy().timesEqual(toMultiply);
   }

   public FloatingLong multiply(long toMultiply) {
      return this.copy().timesEqual(toMultiply);
   }

   public FloatingLong multiply(double toMultiply) {
      if (toMultiply < 0.0) {
         throw new IllegalArgumentException("Multiply called with negative number, this is not supported. FloatingLongs are always positive.");
      } else {
         return this.multiply(createConst(toMultiply));
      }
   }

   public FloatingLong divide(FloatingLong toDivide) {
      return this.copy().divideEquals(toDivide);
   }

   public FloatingLong divide(long toDivide) {
      return this.copy().divideEquals(toDivide);
   }

   public FloatingLong divide(double toDivide) {
      if (toDivide < 0.0) {
         throw new IllegalArgumentException("Division called with negative number, this is not supported. FloatingLongs are always positive.");
      } else {
         return this.divide(create(toDivide));
      }
   }

   public double divideToLevel(FloatingLong toDivide) {
      return !toDivide.isZero() && !this.greaterThan(toDivide) ? this.divide(toDivide).doubleValue() : 1.0;
   }

   public FloatingLong max(FloatingLong other) {
      return this.smallerThan(other) ? other : this;
   }

   public FloatingLong min(FloatingLong other) {
      return this.greaterThan(other) ? other : this;
   }

   public FloatingLong ceil() {
      if (this.decimal == 0) {
         return this;
      } else {
         return this.value == -1L ? new FloatingLong(this.value, (short)0, false) : new FloatingLong(this.value + 1L, (short)0, false);
      }
   }

   public FloatingLong ceilSelf() {
      if (this.decimal == 0) {
         return this;
      } else {
         return this.value == -1L ? this.setAndClampValues(this.value, (short)0) : this.setAndClampValues(this.value + 1L, (short)0);
      }
   }

   public FloatingLong floor() {
      return this.decimal == 0 ? this : new FloatingLong(this.value, (short)0, false);
   }

   public FloatingLong floorSelf() {
      return this.decimal == 0 ? this : this.setAndClampValues(this.value, (short)0);
   }

   public boolean smallerThan(FloatingLong toCompare) {
      return this.compareTo(toCompare) < 0;
   }

   public boolean smallerOrEqual(FloatingLong toCompare) {
      return this.compareTo(toCompare) <= 0;
   }

   public boolean greaterThan(FloatingLong toCompare) {
      return this.compareTo(toCompare) > 0;
   }

   public boolean greaterOrEqual(FloatingLong toCompare) {
      return this.compareTo(toCompare) >= 0;
   }

   public int compareTo(FloatingLong toCompare) {
      int valueCompare = Long.compareUnsigned(this.value, toCompare.value);
      if (valueCompare == 0) {
         if (this.decimal < toCompare.decimal) {
            return -2;
         } else {
            return this.decimal > toCompare.decimal ? 2 : 0;
         }
      } else {
         return valueCompare;
      }
   }

   public boolean equals(FloatingLong other) {
      return this.value == other.value && this.decimal == other.decimal;
   }

   @Override
   public boolean equals(Object o) {
      return this == o || o instanceof FloatingLong other && this.equals(other);
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.value, this.decimal);
   }

   @Override
   public byte byteValue() {
      int v = this.intValue();
      return v < 127 ? (byte)v : 127;
   }

   @Override
   public short shortValue() {
      int v = this.intValue();
      return v < 32767 ? (short)v : 32767;
   }

   @Override
   public int intValue() {
      return MathUtils.clampUnsignedToInt(this.value);
   }

   @Override
   public long longValue() {
      return MathUtils.clampUnsignedToLong(this.value);
   }

   @Override
   public float floatValue() {
      return MathUtils.unsignedLongToFloat(this.value) + this.decimal / 10000.0F;
   }

   @Override
   public double doubleValue() {
      return MathUtils.unsignedLongToDouble(this.value) + this.decimal / 10000.0;
   }

   public FloatingLong absDifference(FloatingLong other) {
      return this.greaterThan(other) ? this.subtract(other) : this.add(other);
   }

   public void writeToBuffer(FriendlyByteBuf buffer) {
      buffer.m_130103_(this.value);
      buffer.writeShort(this.decimal);
   }

   @Override
   public String toString() {
      return this.toString(4);
   }

   public String toString(int decimalPlaces) {
      if (this.decimal == 0) {
         return Long.toUnsignedString(this.value);
      } else {
         if (decimalPlaces > 4) {
            decimalPlaces = 4;
         }

         String valueAsString = Long.toUnsignedString(this.value) + ".";
         String decimalAsString = Short.toString(this.decimal);
         int numberDigits = decimalAsString.length();
         if (numberDigits < 4) {
            decimalAsString = getZeros(4 - numberDigits) + decimalAsString;
            numberDigits = 4;
         }

         if (numberDigits > decimalPlaces) {
            decimalAsString = decimalAsString.substring(0, decimalPlaces);
         }

         return valueAsString + decimalAsString;
      }
   }

   public static FloatingLong parseFloatingLong(String string) {
      return parseFloatingLong(string, false);
   }

   public static FloatingLong parseFloatingLong(String string, boolean isConstant) {
      int index = string.indexOf(46);
      long value;
      if (index == -1) {
         value = Long.parseUnsignedLong(string);
      } else {
         value = Long.parseUnsignedLong(string.substring(0, index));
      }

      short decimal = parseDecimal(string, index);
      return isConstant ? createConst(value, decimal) : create(value, decimal);
   }

   private static short parseDecimal(String string) {
      return parseDecimal(string, string.indexOf(46));
   }

   private static short parseDecimal(String string, int index) {
      if (index == -1) {
         return 0;
      } else {
         String decimalAsString = string.substring(index + 1);
         int numberDigits = decimalAsString.length();
         if (numberDigits < 4) {
            decimalAsString = decimalAsString + getZeros(4 - numberDigits);
         } else if (numberDigits > 4) {
            decimalAsString = decimalAsString.substring(0, 4);
         }

         return Short.parseShort(decimalAsString);
      }
   }

   private static String getZeros(int number) {
      return "0".repeat(Math.max(0, number));
   }

   private static boolean multiplyLongsWillOverFlow(long a, long b) {
      return a != 0L && b != 0L && Long.compareUnsigned(b, Long.divideUnsigned(-1L, a)) > 0;
   }

   private static long multiplyLongs(long a, long b) {
      if (a == 0L || b == 0L) {
         return 0L;
      } else {
         return multiplyLongsWillOverFlow(a, b) ? -1L : a * b;
      }
   }

   private static FloatingLong multiplyLongAndDecimal(long value, short decimal) {
      if (value == 0L || decimal == 0) {
         return ZERO;
      } else {
         return Long.compareUnsigned(value, Long.divideUnsigned(-1L, 10000L)) > 0
            ? create(Long.divideUnsigned(value, 10000L) * decimal, (short)(value % 10000L * decimal))
            : new FloatingLong(Long.divideUnsigned(value * decimal, 10000L), (short)(value * decimal % 10000L), false);
      }
   }

   private static FloatingLong addLongAndDecimalMultiplication(FloatingLong base, long value, short decimal) {
      if (value == 0L || decimal == 0) {
         return base;
      } else {
         return Long.compareUnsigned(value, Long.divideUnsigned(-1L, 10000L)) > 0
            ? base.plusEqual(Long.divideUnsigned(value, 10000L) * decimal, clampDecimal((short)(value % 10000L * decimal)))
            : base.plusEqual(Long.divideUnsigned(value * decimal, 10000L), (short)(value * decimal % 10000L));
      }
   }

   private static short multiplyDecimals(short a, short b) {
      long temp = (long)a * b / 10000L;
      return clampDecimal((short)temp);
   }
}
