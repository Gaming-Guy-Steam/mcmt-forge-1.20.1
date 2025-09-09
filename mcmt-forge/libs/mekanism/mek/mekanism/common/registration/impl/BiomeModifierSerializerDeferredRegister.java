package mekanism.common.registration.impl;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import mekanism.common.registration.WrappedDatapackDeferredRegister;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.registries.ForgeRegistries.Keys;

public class BiomeModifierSerializerDeferredRegister extends WrappedDatapackDeferredRegister<BiomeModifier> {
   public BiomeModifierSerializerDeferredRegister(String modid) {
      super(modid, Keys.BIOME_MODIFIER_SERIALIZERS, Keys.BIOME_MODIFIERS);
   }

   public <T extends BiomeModifier> BiomeModifierSerializerRegistryObject<T> register(String name, Supplier<Codec<T>> sup) {
      return this.register(name, sup, BiomeModifierSerializerRegistryObject::new);
   }
}
