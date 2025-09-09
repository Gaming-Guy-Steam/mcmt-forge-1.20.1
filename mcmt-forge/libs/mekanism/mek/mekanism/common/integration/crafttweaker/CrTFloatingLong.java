package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.math.FloatingLong;
import org.openzen.zencode.java.ZenCodeType.Caster;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Operator;
import org.openzen.zencode.java.ZenCodeType.OperatorType;
import org.openzen.zencode.java.ZenCodeType.StaticExpansionMethod;
import org.openzen.zencode.java.ZenCodeType.Unsigned;

@ZenRegister
@NativeTypeRegistration(
   value = FloatingLong.class,
   zenCodeName = "mods.mekanism.api.FloatingLong"
)
public class CrTFloatingLong {
   private CrTFloatingLong() {
   }

   @StaticExpansionMethod
   public static FloatingLong create(long value) {
      if (value < 0L) {
         throw new IllegalArgumentException("Floating Longs do not support negative numbers.");
      } else {
         return FloatingLong.createConst(value);
      }
   }

   @StaticExpansionMethod
   public static FloatingLong createFromUnsigned(@Unsigned long value) {
      return FloatingLong.createConst(value);
   }

   @StaticExpansionMethod
   public static FloatingLong create(double value) {
      if (value < 0.0) {
         throw new IllegalArgumentException("Floating Longs do not support negative numbers.");
      } else {
         return FloatingLong.createConst(value);
      }
   }

   @StaticExpansionMethod
   public static FloatingLong create(String value) {
      return FloatingLong.parseFloatingLong(value, true);
   }

   @Method
   @Caster(
      implicit = true
   )
   public static String asString(FloatingLong _this) {
      return _this.toString();
   }

   @Method
   @Operator(OperatorType.ADD)
   public static FloatingLong add(FloatingLong _this, FloatingLong toAdd) {
      return _this.add(toAdd);
   }

   @Method
   @Operator(OperatorType.SUB)
   public static FloatingLong subtract(FloatingLong _this, FloatingLong toSubtract) {
      return _this.subtract(toSubtract);
   }

   @Method
   @Operator(OperatorType.MUL)
   public static FloatingLong multiply(FloatingLong _this, FloatingLong toMultiply) {
      return _this.multiply(toMultiply);
   }

   @Method
   @Operator(OperatorType.DIV)
   public static FloatingLong divide(FloatingLong _this, FloatingLong toDivide) {
      return _this.divide(toDivide);
   }

   @Method
   @Operator(OperatorType.EQUALS)
   public static boolean isEqual(FloatingLong _this, FloatingLong toCompare) {
      return _this.equals(toCompare);
   }

   @Method
   @Operator(OperatorType.COMPARE)
   public static int compareTo(FloatingLong _this, FloatingLong toCompare) {
      return _this.compareTo(toCompare);
   }

   @Method
   public static FloatingLong max(FloatingLong _this, FloatingLong other) {
      return _this.max(other);
   }

   @Method
   public static FloatingLong min(FloatingLong _this, FloatingLong other) {
      return _this.min(other);
   }

   @Method
   public static FloatingLong ceil(FloatingLong _this) {
      return _this.ceil().copyAsConst();
   }

   @Method
   public static FloatingLong floor(FloatingLong _this) {
      return _this.floor().copyAsConst();
   }

   @Method
   @Caster
   public static byte byteValue(FloatingLong _this) {
      return _this.byteValue();
   }

   @Method
   @Caster
   public static short shortValue(FloatingLong _this) {
      return _this.shortValue();
   }

   @Method
   @Caster
   public static int intValue(FloatingLong _this) {
      return _this.intValue();
   }

   @Method
   @Caster
   public static long longValue(FloatingLong _this) {
      return _this.longValue();
   }

   @Method
   @Caster
   public static float floatValue(FloatingLong _this) {
      return _this.floatValue();
   }

   @Method
   @Caster
   public static double doubleValue(FloatingLong _this) {
      return _this.doubleValue();
   }
}
