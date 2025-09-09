package mekanism.common.capabilities.chemical.item;

import java.util.function.Consumer;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
import org.jetbrains.annotations.NotNull;

public abstract class ItemStackMekanismPigmentHandler
   extends ItemStackMekanismChemicalHandler<Pigment, PigmentStack, IPigmentTank>
   implements IPigmentHandler.IMekanismPigmentHandler {
   @NotNull
   @Override
   protected String getNbtKey() {
      return "PigmentTanks";
   }

   @Override
   protected void gatherCapabilityResolvers(Consumer<ICapabilityResolver> consumer) {
      consumer.accept(BasicCapabilityResolver.constant(Capabilities.PIGMENT_HANDLER, this));
   }
}
