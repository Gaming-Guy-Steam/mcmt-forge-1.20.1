package mekanism.common.capabilities.proxy;

import mekanism.api.Action;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.ISidedStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.holder.IHolder;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ProxyStrictEnergyHandler extends ProxyHandler implements IStrictEnergyHandler {
   private final ISidedStrictEnergyHandler energyHandler;

   public ProxyStrictEnergyHandler(ISidedStrictEnergyHandler energyHandler, @Nullable Direction side, @Nullable IHolder holder) {
      super(side, holder);
      this.energyHandler = energyHandler;
   }

   @Override
   public int getEnergyContainerCount() {
      return this.energyHandler.getEnergyContainerCount(this.side);
   }

   @Override
   public FloatingLong getEnergy(int container) {
      return this.energyHandler.getEnergy(container, this.side);
   }

   @Override
   public void setEnergy(int container, FloatingLong energy) {
      if (!this.readOnly) {
         this.energyHandler.setEnergy(container, energy, this.side);
      }
   }

   @Override
   public FloatingLong getMaxEnergy(int container) {
      return this.energyHandler.getMaxEnergy(container, this.side);
   }

   @Override
   public FloatingLong getNeededEnergy(int container) {
      return this.energyHandler.getNeededEnergy(container, this.side);
   }

   @Override
   public FloatingLong insertEnergy(int container, FloatingLong amount, Action action) {
      return !this.readOnly && !this.readOnlyInsert.getAsBoolean() ? this.energyHandler.insertEnergy(container, amount, this.side, action) : amount;
   }

   @Override
   public FloatingLong extractEnergy(int container, FloatingLong amount, Action action) {
      return !this.readOnly && !this.readOnlyExtract.getAsBoolean()
         ? this.energyHandler.extractEnergy(container, amount, this.side, action)
         : FloatingLong.ZERO;
   }

   @Override
   public FloatingLong insertEnergy(FloatingLong amount, Action action) {
      return !this.readOnly && !this.readOnlyInsert.getAsBoolean() ? this.energyHandler.insertEnergy(amount, this.side, action) : amount;
   }

   @Override
   public FloatingLong extractEnergy(FloatingLong amount, Action action) {
      return !this.readOnly && !this.readOnlyExtract.getAsBoolean() ? this.energyHandler.extractEnergy(amount, this.side, action) : FloatingLong.ZERO;
   }
}
