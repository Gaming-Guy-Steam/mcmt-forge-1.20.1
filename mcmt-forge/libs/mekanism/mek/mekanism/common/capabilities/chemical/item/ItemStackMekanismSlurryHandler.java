package mekanism.common.capabilities.chemical.item;

import java.util.function.Consumer;
import mekanism.api.chemical.slurry.ISlurryHandler;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
import org.jetbrains.annotations.NotNull;

public abstract class ItemStackMekanismSlurryHandler
   extends ItemStackMekanismChemicalHandler<Slurry, SlurryStack, ISlurryTank>
   implements ISlurryHandler.IMekanismSlurryHandler {
   @NotNull
   @Override
   protected String getNbtKey() {
      return "SlurryTanks";
   }

   @Override
   protected void gatherCapabilityResolvers(Consumer<ICapabilityResolver> consumer) {
      consumer.accept(BasicCapabilityResolver.constant(Capabilities.SLURRY_HANDLER, this));
   }
}
