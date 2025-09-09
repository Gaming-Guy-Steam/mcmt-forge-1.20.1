package mekanism.common.integration.computer.computercraft;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.MethodResult;
import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;
import mekanism.common.integration.computer.BoundMethodHolder;
import mekanism.common.integration.computer.ComputerException;
import org.jetbrains.annotations.NotNull;

public class CCMethodCaller extends BoundMethodHolder {
   public String[] getMethodNames() {
      return (String[])this.methodNames.get();
   }

   public MethodResult callMethod(ILuaContext context, int methodIdx, IArguments arguments) throws LuaException {
      String[] methodNames = this.getMethodNames();
      if (methodIdx >= methodNames.length) {
         throw new LuaException(
            String.format(Locale.ROOT, "Method index '%d' is out of bounds. This handler only has '%d' methods.", methodIdx, methodNames.length)
         );
      } else {
         Collection<BoundMethodHolder.BoundMethodData<?>> methodDataCollection = this.methods.get(methodNames[methodIdx]);
         int argCount = arguments.count();
         BoundMethodHolder.BoundMethodData<?> methodToCall = methodDataCollection.stream()
            .filter(md -> md.argumentNames().length == argCount)
            .findAny()
            .orElseThrow(
               () -> new LuaException(
                  String.format(
                     Locale.ROOT,
                     "Found %d arguments, expected %s",
                     argCount,
                     methodDataCollection.stream().map(it -> String.valueOf(it.argumentNames().length)).collect(Collectors.joining(" or "))
                  )
               )
            );
         if (methodToCall.threadSafe()) {
            return callHandler(arguments, methodToCall);
         } else {
            IArguments escaped = arguments.escapes();
            return context.executeMainThreadTask(() -> callHandler(escaped, methodToCall).getResult());
         }
      }
   }

   @NotNull
   private static MethodResult callHandler(IArguments arguments, BoundMethodHolder.BoundMethodData<?> methodToCall) throws LuaException {
      Object result;
      try {
         result = methodToCall.call(new CCComputerHelper(arguments));
      } catch (ComputerException var6) {
         if (var6.getCause() instanceof LuaException luaException) {
            throw luaException;
         }

         throw (LuaException)new LuaException(var6.getMessage()).initCause(var6);
      }

      return result instanceof MethodResult mr ? mr : MethodResult.of(result);
   }
}
