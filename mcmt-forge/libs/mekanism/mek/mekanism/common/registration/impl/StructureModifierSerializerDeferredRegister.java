package mekanism.common.registration.impl;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import mekanism.common.registration.WrappedDatapackDeferredRegister;
import net.minecraftforge.common.world.StructureModifier;
import net.minecraftforge.registries.ForgeRegistries.Keys;

public class StructureModifierSerializerDeferredRegister extends WrappedDatapackDeferredRegister<StructureModifier> {
   public StructureModifierSerializerDeferredRegister(String modid) {
      super(modid, Keys.STRUCTURE_MODIFIER_SERIALIZERS, Keys.STRUCTURE_MODIFIERS);
   }

   public <T extends StructureModifier> StructureModifierSerializerRegistryObject<T> register(String name, Supplier<Codec<T>> sup) {
      return this.register(name, sup, StructureModifierSerializerRegistryObject::new);
   }
}
