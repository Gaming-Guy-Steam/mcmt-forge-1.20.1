package mekanism.api.energy;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.FloatingLong;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

@NothingNullByDefault
public interface IEnergyContainer extends INBTSerializable<CompoundTag>, IContentsListener {
   FloatingLong getEnergy();

   void setEnergy(FloatingLong var1);

   default FloatingLong insert(FloatingLong amount, Action action, AutomationType automationType) {
      if (amount.isZero()) {
         return amount;
      } else {
         FloatingLong needed = this.getNeeded();
         if (needed.isZero()) {
            return amount;
         } else {
            FloatingLong toAdd = amount.min(needed);
            if (!toAdd.isZero() && action.execute()) {
               this.setEnergy(this.getEnergy().add(toAdd));
            }

            return amount.subtract(toAdd);
         }
      }
   }

   default FloatingLong extract(FloatingLong amount, Action action, AutomationType automationType) {
      if (!this.isEmpty() && !amount.isZero()) {
         FloatingLong ret = this.getEnergy().min(amount).copy();
         if (!ret.isZero() && action.execute()) {
            this.setEnergy(this.getEnergy().subtract(ret));
         }

         return ret;
      } else {
         return FloatingLong.ZERO;
      }
   }

   FloatingLong getMaxEnergy();

   default boolean isEmpty() {
      return this.getEnergy().isZero();
   }

   default void setEmpty() {
      this.setEnergy(FloatingLong.ZERO);
   }

   default FloatingLong getNeeded() {
      return FloatingLong.ZERO.max(this.getMaxEnergy().subtract(this.getEnergy()));
   }

   default CompoundTag serializeNBT() {
      CompoundTag nbt = new CompoundTag();
      if (!this.isEmpty()) {
         nbt.m_128359_("stored", this.getEnergy().toString());
      }

      return nbt;
   }
}
