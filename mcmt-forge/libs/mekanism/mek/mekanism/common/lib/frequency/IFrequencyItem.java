package mekanism.common.lib.frequency;

import mekanism.common.util.ItemDataUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface IFrequencyItem {
   @Nullable
   default Frequency.FrequencyIdentity getFrequencyIdentity(ItemStack stack) {
      return this.hasFrequency(stack) ? Frequency.FrequencyIdentity.load(this.getFrequencyType(), ItemDataUtils.getCompound(stack, "frequency")) : null;
   }

   default boolean hasFrequency(ItemStack stack) {
      return ItemDataUtils.hasData(stack, "frequency", 10);
   }

   @Nullable
   default Frequency getFrequency(ItemStack stack) {
      if (this.hasFrequency(stack)) {
         CompoundTag frequencyCompound = ItemDataUtils.getCompound(stack, "frequency");
         Frequency.FrequencyIdentity identity = Frequency.FrequencyIdentity.load(this.getFrequencyType(), frequencyCompound);
         if (identity != null && frequencyCompound.m_128403_("owner")) {
            return this.getFrequencyType().getManager(identity, frequencyCompound.m_128342_("owner")).getFrequency(identity.key());
         }
      }

      return null;
   }

   default void setFrequency(ItemStack stack, Frequency frequency) {
      if (frequency == null) {
         ItemDataUtils.removeData(stack, "frequency");
      } else {
         ItemDataUtils.setCompound(stack, "frequency", frequency.serializeIdentityWithOwner());
      }
   }

   FrequencyType<?> getFrequencyType();
}
