package mekanism.api.energy;

import mekanism.api.Action;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongTransferUtils;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public interface ISidedStrictEnergyHandler extends IStrictEnergyHandler {
   @Nullable
   default Direction getEnergySideFor() {
      return null;
   }

   int getEnergyContainerCount(@Nullable Direction var1);

   @Override
   default int getEnergyContainerCount() {
      return this.getEnergyContainerCount(this.getEnergySideFor());
   }

   FloatingLong getEnergy(int var1, @Nullable Direction var2);

   @Override
   default FloatingLong getEnergy(int container) {
      return this.getEnergy(container, this.getEnergySideFor());
   }

   void setEnergy(int var1, FloatingLong var2, @Nullable Direction var3);

   @Override
   default void setEnergy(int container, FloatingLong energy) {
      this.setEnergy(container, energy, this.getEnergySideFor());
   }

   FloatingLong getMaxEnergy(int var1, @Nullable Direction var2);

   @Override
   default FloatingLong getMaxEnergy(int container) {
      return this.getMaxEnergy(container, this.getEnergySideFor());
   }

   FloatingLong getNeededEnergy(int var1, @Nullable Direction var2);

   @Override
   default FloatingLong getNeededEnergy(int container) {
      return this.getNeededEnergy(container, this.getEnergySideFor());
   }

   FloatingLong insertEnergy(int var1, FloatingLong var2, @Nullable Direction var3, Action var4);

   @Override
   default FloatingLong insertEnergy(int container, FloatingLong amount, Action action) {
      return this.insertEnergy(container, amount, this.getEnergySideFor(), action);
   }

   FloatingLong extractEnergy(int var1, FloatingLong var2, @Nullable Direction var3, Action var4);

   @Override
   default FloatingLong extractEnergy(int container, FloatingLong amount, Action action) {
      return this.extractEnergy(container, amount, this.getEnergySideFor(), action);
   }

   default FloatingLong insertEnergy(FloatingLong amount, @Nullable Direction side, Action action) {
      return FloatingLongTransferUtils.insert(
         amount,
         action,
         () -> this.getEnergyContainerCount(side),
         container -> this.getEnergy(container, side),
         (container, a, act) -> this.insertEnergy(container, a, side, act)
      );
   }

   default FloatingLong extractEnergy(FloatingLong amount, @Nullable Direction side, Action action) {
      return FloatingLongTransferUtils.extract(
         amount, action, () -> this.getEnergyContainerCount(side), (container, a, act) -> this.extractEnergy(container, a, side, act)
      );
   }
}
