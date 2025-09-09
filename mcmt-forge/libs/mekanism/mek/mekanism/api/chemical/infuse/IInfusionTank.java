package mekanism.api.chemical.infuse;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.IChemicalTank;
import net.minecraft.nbt.CompoundTag;

@NothingNullByDefault
public interface IInfusionTank extends IChemicalTank<InfuseType, InfusionStack>, IEmptyInfusionProvider {
   default InfusionStack createStack(InfusionStack stored, long size) {
      return new InfusionStack(stored, size);
   }

   default void deserializeNBT(CompoundTag nbt) {
      if (nbt.m_128425_("stored", 10)) {
         this.setStackUnchecked(InfusionStack.readFromNBT(nbt.m_128469_("stored")));
      }
   }
}
