package mekanism.common.lib.frequency;

import java.util.List;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;

public interface IFrequencyHandler {
   TileComponentFrequency getFrequencyComponent();

   default <FREQ extends Frequency> FREQ getFrequency(FrequencyType<FREQ> type) {
      return this.getFrequencyComponent().getFrequency(type);
   }

   default void setFrequency(FrequencyType<?> type, CompoundTag frequencyCompound) {
      Frequency.FrequencyIdentity freq = Frequency.FrequencyIdentity.load(type, frequencyCompound);
      if (freq != null && frequencyCompound.m_128403_("owner")) {
         this.setFrequency(type, freq, frequencyCompound.m_128342_("owner"));
      }
   }

   default void setFrequency(FrequencyType<?> type, Frequency.FrequencyIdentity data, UUID player) {
      this.getFrequencyComponent().setFrequencyFromData(type, data, player);
   }

   default void removeFrequency(FrequencyType<?> type, Frequency.FrequencyIdentity data, UUID player) {
      this.getFrequencyComponent().removeFrequencyFromData(type, data, player);
   }

   default <FREQ extends Frequency> List<FREQ> getPublicCache(FrequencyType<FREQ> type) {
      return this.getFrequencyComponent().getPublicCache(type);
   }

   default <FREQ extends Frequency> List<FREQ> getPrivateCache(FrequencyType<FREQ> type) {
      return this.getFrequencyComponent().getPrivateCache(type);
   }
}
