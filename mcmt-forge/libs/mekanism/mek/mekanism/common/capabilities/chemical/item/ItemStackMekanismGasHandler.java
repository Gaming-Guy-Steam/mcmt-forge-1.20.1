package mekanism.common.capabilities.chemical.item;

import java.util.function.Consumer;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
import org.jetbrains.annotations.NotNull;

public abstract class ItemStackMekanismGasHandler extends ItemStackMekanismChemicalHandler<Gas, GasStack, IGasTank> implements IGasHandler.IMekanismGasHandler {
   @NotNull
   @Override
   protected String getNbtKey() {
      return "GasTanks";
   }

   @Override
   protected void gatherCapabilityResolvers(Consumer<ICapabilityResolver> consumer) {
      consumer.accept(BasicCapabilityResolver.constant(Capabilities.GAS_HANDLER, this));
   }
}
