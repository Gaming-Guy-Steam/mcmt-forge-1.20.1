package mekanism.common.registration.impl;

import java.util.function.UnaryOperator;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryBuilder;
import mekanism.common.registration.WrappedDeferredRegister;
import mekanism.common.resource.PrimaryResource;

public class SlurryDeferredRegister extends WrappedDeferredRegister<Slurry> {
   public SlurryDeferredRegister(String modid) {
      super(modid, MekanismAPI.SLURRY_REGISTRY_NAME);
   }

   public SlurryRegistryObject<Slurry, Slurry> register(PrimaryResource resource) {
      return this.register(resource.getRegistrySuffix(), builder -> builder.tint(resource.getTint()).ore(resource.getOreTag()));
   }

   public SlurryRegistryObject<Slurry, Slurry> register(String baseName, UnaryOperator<SlurryBuilder> builderModifier) {
      return new SlurryRegistryObject<>(
         this.internal.register("dirty_" + baseName, () -> new Slurry(builderModifier.apply(SlurryBuilder.dirty()))),
         this.internal.register("clean_" + baseName, () -> new Slurry(builderModifier.apply(SlurryBuilder.clean())))
      );
   }
}
