package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import mekanism.api.math.FloatingLong;
import org.openzen.zencode.java.ZenCodeType.Caster;
import org.openzen.zencode.java.ZenCodeType.Expansion;

public class CrTNumberExpansion {
   private CrTNumberExpansion() {
   }

   @ZenRegister
   @Expansion("byte")
   public static class ByteExpansion {
      private ByteExpansion() {
      }

      @Caster(
         implicit = true
      )
      public static FloatingLong asFloatingLong(byte _this) {
         return CrTFloatingLong.create((long)_this);
      }
   }

   @ZenRegister
   @Expansion("double")
   public static class DoubleExpansion {
      private DoubleExpansion() {
      }

      @Caster(
         implicit = true
      )
      public static FloatingLong asFloatingLong(double _this) {
         return CrTFloatingLong.create(_this);
      }
   }

   @ZenRegister
   @Expansion("float")
   public static class FloatExpansion {
      private FloatExpansion() {
      }

      @Caster(
         implicit = true
      )
      public static FloatingLong asFloatingLong(float _this) {
         return CrTFloatingLong.create((double)_this);
      }
   }

   @ZenRegister
   @Expansion("int")
   public static class IntExpansion {
      private IntExpansion() {
      }

      @Caster(
         implicit = true
      )
      public static FloatingLong asFloatingLong(int _this) {
         return CrTFloatingLong.create((long)_this);
      }
   }

   @ZenRegister
   @Expansion("long")
   public static class LongExpansion {
      private LongExpansion() {
      }

      @Caster(
         implicit = true
      )
      public static FloatingLong asFloatingLong(long _this) {
         return CrTFloatingLong.create(_this);
      }
   }

   @ZenRegister
   @Expansion("short")
   public static class ShortExpansion {
      private ShortExpansion() {
      }

      @Caster(
         implicit = true
      )
      public static FloatingLong asFloatingLong(short _this) {
         return CrTFloatingLong.create((long)_this);
      }
   }

   @ZenRegister
   @Expansion("string")
   public static class StringExpansion {
      private StringExpansion() {
      }

      @Caster(
         implicit = true
      )
      public static FloatingLong asFloatingLong(String _this) {
         return CrTFloatingLong.create(_this);
      }
   }
}
