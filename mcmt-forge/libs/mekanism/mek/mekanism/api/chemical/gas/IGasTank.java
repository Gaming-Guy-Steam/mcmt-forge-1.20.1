package mekanism.api.chemical.gas;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.IChemicalTank;
import net.minecraft.nbt.CompoundTag;

@NothingNullByDefault
public interface IGasTank extends IChemicalTank<Gas, GasStack>, IEmptyGasProvider {
   default GasStack createStack(GasStack stored, long size) {
      return new GasStack(stored, size);
   }

   default void deserializeNBT(CompoundTag nbt) {
      if (nbt.m_128425_("stored", 10)) {
         this.setStackUnchecked(GasStack.readFromNBT(nbt.m_128469_("stored")));
      }
   }
}
