package mekanism.common.integration.computer.opencomputers2;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.util.Map;
import li.cil.oc2.api.bus.device.rpc.RPCInvocation;
import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import org.jetbrains.annotations.NotNull;

public class OC2ComputerHelper extends BaseComputerHelper {
   private final RPCInvocation invocation;

   public OC2ComputerHelper(RPCInvocation invocation) {
      this.invocation = invocation;
   }

   private JsonElement getParam(int param) throws ComputerException {
      JsonArray parameters = this.invocation.getParameters();
      if (parameters.size() <= param) {
         throw new ComputerException("Missing argument in position " + param);
      } else {
         return parameters.get(param);
      }
   }

   @Override
   public boolean getBoolean(int param) throws ComputerException {
      return this.getParam(param).getAsBoolean();
   }

   @Override
   public byte getByte(int param) throws ComputerException {
      return this.getParam(param).getAsByte();
   }

   @Override
   public short getShort(int param) throws ComputerException {
      return this.getParam(param).getAsShort();
   }

   @Override
   public int getInt(int param) throws ComputerException {
      return this.getParam(param).getAsInt();
   }

   @Override
   public long getLong(int param) throws ComputerException {
      return this.getParam(param).getAsLong();
   }

   @Override
   public char getChar(int param) throws ComputerException {
      return this.getParam(param).getAsString().charAt(0);
   }

   @Override
   public float getFloat(int param) throws ComputerException {
      return this.getParam(param).getAsFloat();
   }

   @Override
   public double getDouble(int param) throws ComputerException {
      return this.getParam(param).getAsDouble();
   }

   @NotNull
   @Override
   public String getString(int param) throws ComputerException {
      return this.getParam(param).getAsString();
   }

   @NotNull
   @Override
   public Map<?, ?> getMap(int param) throws ComputerException {
      return this.getParam(param).getAsJsonObject().asMap();
   }
}
