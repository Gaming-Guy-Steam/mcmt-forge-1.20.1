package mekanism.common.registration.impl;

import mekanism.api.chemical.gas.Gas;
import mekanism.api.providers.IGasProvider;
import mekanism.common.registration.WrappedRegistryObject;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

public class GasRegistryObject<GAS extends Gas> extends WrappedRegistryObject<GAS> implements IGasProvider {
   public GasRegistryObject(RegistryObject<GAS> registryObject) {
      super(registryObject);
   }

   @NotNull
   public GAS getChemical() {
      return this.get();
   }
}
