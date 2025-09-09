package mekanism.api.energy;

import java.util.List;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.FloatingLong;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public interface IMekanismStrictEnergyHandler extends ISidedStrictEnergyHandler, IContentsListener {
   default boolean canHandleEnergy() {
      return true;
   }

   List<IEnergyContainer> getEnergyContainers(@Nullable Direction var1);

   @Nullable
   default IEnergyContainer getEnergyContainer(int container, @Nullable Direction side) {
      List<IEnergyContainer> containers = this.getEnergyContainers(side);
      return container >= 0 && container < containers.size() ? containers.get(container) : null;
   }

   @Override
   default int getEnergyContainerCount(@Nullable Direction side) {
      return this.getEnergyContainers(side).size();
   }

   @Override
   default FloatingLong getEnergy(int container, @Nullable Direction side) {
      IEnergyContainer energyContainer = this.getEnergyContainer(container, side);
      return energyContainer == null ? FloatingLong.ZERO : energyContainer.getEnergy();
   }

   @Override
   default void setEnergy(int container, FloatingLong energy, @Nullable Direction side) {
      IEnergyContainer energyContainer = this.getEnergyContainer(container, side);
      if (energyContainer != null) {
         energyContainer.setEnergy(energy);
      }
   }

   @Override
   default FloatingLong getMaxEnergy(int container, @Nullable Direction side) {
      IEnergyContainer energyContainer = this.getEnergyContainer(container, side);
      return energyContainer == null ? FloatingLong.ZERO : energyContainer.getMaxEnergy();
   }

   @Override
   default FloatingLong getNeededEnergy(int container, @Nullable Direction side) {
      IEnergyContainer energyContainer = this.getEnergyContainer(container, side);
      return energyContainer == null ? FloatingLong.ZERO : energyContainer.getNeeded();
   }

   @Override
   default FloatingLong insertEnergy(int container, FloatingLong amount, @Nullable Direction side, Action action) {
      IEnergyContainer energyContainer = this.getEnergyContainer(container, side);
      return energyContainer == null ? amount : energyContainer.insert(amount, action, side == null ? AutomationType.INTERNAL : AutomationType.EXTERNAL);
   }

   @Override
   default FloatingLong extractEnergy(int container, FloatingLong amount, @Nullable Direction side, Action action) {
      IEnergyContainer energyContainer = this.getEnergyContainer(container, side);
      return energyContainer == null
         ? FloatingLong.ZERO
         : energyContainer.extract(amount, action, side == null ? AutomationType.INTERNAL : AutomationType.EXTERNAL);
   }
}
