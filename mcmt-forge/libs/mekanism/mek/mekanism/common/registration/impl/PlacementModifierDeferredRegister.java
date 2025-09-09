package mekanism.common.registration.impl;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import mekanism.common.registration.WrappedDeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class PlacementModifierDeferredRegister extends WrappedDeferredRegister<PlacementModifierType<?>> {
   public PlacementModifierDeferredRegister(String modid) {
      super(modid, Registries.f_256843_);
   }

   public <PROVIDER extends PlacementModifier> PlacementModifierRegistryObject<PROVIDER> register(String name, Codec<PROVIDER> codec) {
      return this.register(name, (Supplier<? extends PlacementModifierType<PROVIDER>>)(() -> () -> codec));
   }

   public <PROVIDER extends PlacementModifier> PlacementModifierRegistryObject<PROVIDER> register(
      String name, Supplier<? extends PlacementModifierType<PROVIDER>> sup
   ) {
      return this.register(name, sup, PlacementModifierRegistryObject::new);
   }
}
