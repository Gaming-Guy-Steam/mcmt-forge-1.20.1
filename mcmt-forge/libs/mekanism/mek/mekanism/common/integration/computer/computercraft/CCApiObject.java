package mekanism.common.integration.computer.computercraft;

import dan200.computercraft.api.lua.IComputerSystem;
import dan200.computercraft.api.lua.IDynamicLuaObject;
import dan200.computercraft.api.lua.ILuaAPI;
import dan200.computercraft.api.lua.ILuaAPIFactory;
import mekanism.common.integration.computer.FactoryRegistry;
import org.jetbrains.annotations.Nullable;

public class CCApiObject extends CCMethodCaller implements IDynamicLuaObject, ILuaAPI {
   private final String[] apiNames;

   static ILuaAPIFactory create(Class<?> source, String... apiNames) {
      return new CCApiObject.Factory(source, apiNames);
   }

   private CCApiObject(String[] apiNames) {
      this.apiNames = apiNames;
   }

   public String[] getNames() {
      return this.apiNames;
   }

   private static class Factory implements ILuaAPIFactory {
      private final Class<?> source;
      private final String[] apiNames;
      private CCApiObject instance;

      Factory(Class<?> source, String[] apiNames) {
         this.source = source;
         this.apiNames = apiNames;
      }

      @Nullable
      public ILuaAPI create(IComputerSystem computer) {
         if (this.instance == null) {
            this.instance = new CCApiObject(this.apiNames);
            FactoryRegistry.bindTo(this.instance, null, this.source);
         }

         return this.instance;
      }
   }
}
