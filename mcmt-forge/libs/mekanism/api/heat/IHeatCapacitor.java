package mekanism.api.heat;

import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

@NothingNullByDefault
public interface IHeatCapacitor extends INBTSerializable<CompoundTag>, IContentsListener {
   double getTemperature();

   double getInverseConduction();

   double getInverseInsulation();

   double getHeatCapacity();

   double getHeat();

   void setHeat(double var1);

   void handleHeat(double var1);

   default CompoundTag serializeNBT() {
      CompoundTag nbt = new CompoundTag();
      nbt.m_128347_("stored", this.getHeat());
      return nbt;
   }
}
