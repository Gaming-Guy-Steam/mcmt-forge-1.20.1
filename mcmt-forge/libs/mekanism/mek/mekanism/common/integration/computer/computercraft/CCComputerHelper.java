package mekanism.common.integration.computer.computercraft;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.MethodResult;
import java.util.Map;
import mekanism.api.math.FloatingLong;
import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import org.jetbrains.annotations.NotNull;

public class CCComputerHelper extends BaseComputerHelper {
   private final IArguments arguments;

   public CCComputerHelper(IArguments arguments) {
      this.arguments = arguments;
   }

   @NotNull
   @Override
   public <T extends Enum<T>> T getEnum(int param, Class<T> enumClazz) throws ComputerException {
      try {
         return (T)this.arguments.getEnum(param, enumClazz);
      } catch (LuaException var4) {
         throw new ComputerException(var4);
      }
   }

   @Override
   public boolean getBoolean(int param) throws ComputerException {
      try {
         return this.arguments.getBoolean(param);
      } catch (LuaException var3) {
         throw new ComputerException(var3);
      }
   }

   @Override
   public byte getByte(int param) throws ComputerException {
      try {
         return (byte)this.arguments.getInt(param);
      } catch (LuaException var3) {
         throw new ComputerException(var3);
      }
   }

   @Override
   public short getShort(int param) throws ComputerException {
      try {
         return (short)this.arguments.getInt(param);
      } catch (LuaException var3) {
         throw new ComputerException(var3);
      }
   }

   @Override
   public int getInt(int param) throws ComputerException {
      try {
         return this.arguments.getInt(param);
      } catch (LuaException var3) {
         throw new ComputerException(var3);
      }
   }

   @Override
   public long getLong(int param) throws ComputerException {
      try {
         return this.arguments.getLong(param);
      } catch (LuaException var3) {
         throw new ComputerException(var3);
      }
   }

   @Override
   public char getChar(int param) throws ComputerException {
      try {
         return this.arguments.getString(param).charAt(0);
      } catch (LuaException var3) {
         throw new ComputerException(var3);
      }
   }

   @Override
   public float getFloat(int param) throws ComputerException {
      try {
         return (float)this.arguments.getDouble(param);
      } catch (LuaException var3) {
         throw new ComputerException(var3);
      }
   }

   @Override
   public double getDouble(int param) throws ComputerException {
      try {
         return this.arguments.getDouble(param);
      } catch (LuaException var3) {
         throw new ComputerException(var3);
      }
   }

   @Override
   public FloatingLong getFloatingLong(int param) throws ComputerException {
      try {
         if (this.arguments.get(param) instanceof String s) {
            return FloatingLong.parseFloatingLong(s);
         } else {
            double finiteDouble = this.arguments.getFiniteDouble(param);
            return finiteDouble < 0.0 ? FloatingLong.ZERO : FloatingLong.createConst(finiteDouble);
         }
      } catch (LuaException var5) {
         throw new ComputerException(var5);
      }
   }

   @NotNull
   @Override
   public Map<?, ?> getMap(int param) throws ComputerException {
      try {
         return this.arguments.getTable(param);
      } catch (LuaException var3) {
         throw new ComputerException(var3);
      }
   }

   @NotNull
   @Override
   public String getString(int param) throws ComputerException {
      try {
         return this.arguments.getString(param);
      } catch (LuaException var3) {
         throw new ComputerException(var3);
      }
   }

   @Override
   public Object voidResult() {
      return MethodResult.of();
   }
}
