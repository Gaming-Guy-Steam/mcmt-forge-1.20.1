package mekanism.common.registration.impl;

import java.util.function.Supplier;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentBuilder;
import mekanism.common.registration.WrappedDeferredRegister;
import net.minecraft.resources.ResourceLocation;

public class PigmentDeferredRegister extends WrappedDeferredRegister<Pigment> {
   public PigmentDeferredRegister(String modid) {
      super(modid, MekanismAPI.PIGMENT_REGISTRY_NAME);
   }

   public PigmentRegistryObject<Pigment> register(String name, int tint) {
      return this.register(name, (Supplier<? extends Pigment>)(() -> new Pigment(PigmentBuilder.builder().tint(tint))));
   }

   public PigmentRegistryObject<Pigment> register(String name, ResourceLocation texture) {
      return this.register(name, (Supplier<? extends Pigment>)(() -> new Pigment(PigmentBuilder.builder(texture))));
   }

   public <PIGMENT extends Pigment> PigmentRegistryObject<PIGMENT> register(String name, Supplier<? extends PIGMENT> sup) {
      return this.register(name, sup, PigmentRegistryObject::new);
   }
}
