package mekanism.common.integration;

import java.util.function.Supplier;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.providers.IGasProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class LazyGasProvider implements IGasProvider {
   private Supplier<Gas> gasSupplier;
   private Gas gas = MekanismAPI.EMPTY_GAS;

   public LazyGasProvider(ResourceLocation gasRegistryName) {
      this((Supplier<Gas>)(() -> {
         Gas gas = (Gas)MekanismAPI.gasRegistry().getValue(gasRegistryName);
         return gas == null ? MekanismAPI.EMPTY_GAS : gas;
      }));
   }

   public LazyGasProvider(Supplier<Gas> gasSupplier) {
      this.gasSupplier = gasSupplier;
   }

   @NotNull
   public Gas getChemical() {
      if (this.gas.isEmptyType()) {
         this.gas = this.gasSupplier.get().getChemical();
         if (this.gas.isEmptyType()) {
            throw new IllegalStateException("Empty gas used for coolant attribute via a CraftTweaker Script.");
         }

         this.gasSupplier = null;
      }

      return this.gas;
   }
}
