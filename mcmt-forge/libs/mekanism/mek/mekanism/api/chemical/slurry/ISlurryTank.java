package mekanism.api.chemical.slurry;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.IChemicalTank;
import net.minecraft.nbt.CompoundTag;

@NothingNullByDefault
public interface ISlurryTank extends IChemicalTank<Slurry, SlurryStack>, IEmptySlurryProvider {
   default SlurryStack createStack(SlurryStack stored, long size) {
      return new SlurryStack(stored, size);
   }

   default void deserializeNBT(CompoundTag nbt) {
      if (nbt.m_128425_("stored", 10)) {
         this.setStackUnchecked(SlurryStack.readFromNBT(nbt.m_128469_("stored")));
      }
   }
}
