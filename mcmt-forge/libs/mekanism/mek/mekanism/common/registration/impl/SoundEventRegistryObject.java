package mekanism.common.registration.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.ILangEntry;
import mekanism.common.registration.WrappedRegistryObject;
import net.minecraft.Util;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;

@NothingNullByDefault
public class SoundEventRegistryObject<SOUND extends SoundEvent> extends WrappedRegistryObject<SOUND> implements ILangEntry {
   private final String translationKey = Util.m_137492_("sound_event", this.registryObject.getId());

   public SoundEventRegistryObject(RegistryObject<SOUND> registryObject) {
      super(registryObject);
   }

   @Override
   public String getTranslationKey() {
      return this.translationKey;
   }
}
