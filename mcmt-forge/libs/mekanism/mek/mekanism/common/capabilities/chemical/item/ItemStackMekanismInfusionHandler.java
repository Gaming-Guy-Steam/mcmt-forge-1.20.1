package mekanism.common.capabilities.chemical.item;

import java.util.function.Consumer;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
import org.jetbrains.annotations.NotNull;

public abstract class ItemStackMekanismInfusionHandler
   extends ItemStackMekanismChemicalHandler<InfuseType, InfusionStack, IInfusionTank>
   implements IInfusionHandler.IMekanismInfusionHandler {
   @NotNull
   @Override
   protected String getNbtKey() {
      return "InfusionTanks";
   }

   @Override
   protected void gatherCapabilityResolvers(Consumer<ICapabilityResolver> consumer) {
      consumer.accept(BasicCapabilityResolver.constant(Capabilities.INFUSION_HANDLER, this));
   }
}
