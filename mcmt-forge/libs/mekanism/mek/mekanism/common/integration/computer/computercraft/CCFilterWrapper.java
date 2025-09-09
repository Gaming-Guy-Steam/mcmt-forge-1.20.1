package mekanism.common.integration.computer.computercraft;

import dan200.computercraft.api.lua.IDynamicLuaObject;
import mekanism.common.content.filter.IFilter;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.FactoryRegistry;

public class CCFilterWrapper<FILTER extends IFilter<?>> extends CCMethodCaller implements IDynamicLuaObject {
   private final FILTER filter;

   public CCFilterWrapper(FILTER filter) {
      this.filter = (FILTER)filter.clone();
      FactoryRegistry.bindTo(this, this.filter);
   }

   public <EXPECTED extends IFilter<EXPECTED>> EXPECTED getAs(Class<EXPECTED> expectedType) throws ComputerException {
      if (!expectedType.isInstance(this.filter)) {
         throw new ComputerException(
            "Wrong filter type supplied - expected " + expectedType.getSimpleName() + " but found " + this.filter.getClass().getSimpleName()
         );
      } else {
         return expectedType.cast(this.filter.clone());
      }
   }
}
