package mekanism.common.registration.impl;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import mekanism.common.registration.WrappedDeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviderType;

public class IntProviderTypeDeferredRegister extends WrappedDeferredRegister<IntProviderType<?>> {
   public IntProviderTypeDeferredRegister(String modid) {
      super(modid, Registries.f_256949_);
   }

   public <PROVIDER extends IntProvider> IntProviderTypeRegistryObject<PROVIDER> register(String name, Codec<PROVIDER> codec) {
      return this.register(name, (Supplier<? extends IntProviderType<PROVIDER>>)(() -> () -> codec));
   }

   public <PROVIDER extends IntProvider> IntProviderTypeRegistryObject<PROVIDER> register(String name, Supplier<? extends IntProviderType<PROVIDER>> sup) {
      return this.register(name, sup, IntProviderTypeRegistryObject::new);
   }
}
