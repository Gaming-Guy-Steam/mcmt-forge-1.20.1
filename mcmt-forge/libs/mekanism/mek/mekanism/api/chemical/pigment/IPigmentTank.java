package mekanism.api.chemical.pigment;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.IChemicalTank;
import net.minecraft.nbt.CompoundTag;

@NothingNullByDefault
public interface IPigmentTank extends IChemicalTank<Pigment, PigmentStack>, IEmptyPigmentProvider {
   default PigmentStack createStack(PigmentStack stored, long size) {
      return new PigmentStack(stored, size);
   }

   default void deserializeNBT(CompoundTag nbt) {
      if (nbt.m_128425_("stored", 10)) {
         this.setStackUnchecked(PigmentStack.readFromNBT(nbt.m_128469_("stored")));
      }
   }
}
