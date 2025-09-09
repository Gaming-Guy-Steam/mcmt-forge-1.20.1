package mekanism.common.registration.impl;

import mekanism.common.registration.WrappedDeferredRegister;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class SoundEventDeferredRegister extends WrappedDeferredRegister<SoundEvent> {
   private final String modid;

   public SoundEventDeferredRegister(String modid) {
      super(modid, ForgeRegistries.SOUND_EVENTS);
      this.modid = modid;
   }

   public SoundEventRegistryObject<SoundEvent> register(String name) {
      return this.register(name, () -> SoundEvent.m_262824_(new ResourceLocation(this.modid, name)), SoundEventRegistryObject::new);
   }
}
