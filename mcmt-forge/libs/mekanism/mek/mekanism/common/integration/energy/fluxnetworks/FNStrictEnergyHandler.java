package mekanism.common.integration.energy.fluxnetworks;

import mekanism.api.Action;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.math.FloatingLong;
import mekanism.common.util.UnitDisplayUtils;
import org.jetbrains.annotations.NotNull;
import sonar.fluxnetworks.api.energy.IFNEnergyStorage;

@NothingNullByDefault
public class FNStrictEnergyHandler implements IStrictEnergyHandler {
   private final IFNEnergyStorage storage;

   public FNStrictEnergyHandler(IFNEnergyStorage storage) {
      this.storage = storage;
   }

   @Override
   public int getEnergyContainerCount() {
      return 1;
   }

   @Override
   public FloatingLong getEnergy(int container) {
      return container == 0 ? UnitDisplayUtils.EnergyUnit.FORGE_ENERGY.convertFrom(this.storage.getEnergyStoredL()) : FloatingLong.ZERO;
   }

   @Override
   public void setEnergy(int container, FloatingLong energy) {
   }

   @Override
   public FloatingLong getMaxEnergy(int container) {
      return container == 0 ? UnitDisplayUtils.EnergyUnit.FORGE_ENERGY.convertFrom(this.storage.getMaxEnergyStoredL()) : FloatingLong.ZERO;
   }

   @Override
   public FloatingLong getNeededEnergy(int container) {
      return container == 0
         ? UnitDisplayUtils.EnergyUnit.FORGE_ENERGY.convertFrom(Math.max(0L, this.storage.getMaxEnergyStoredL() - this.storage.getEnergyStoredL()))
         : FloatingLong.ZERO;
   }

   @Override
   public FloatingLong insertEnergy(int container, FloatingLong amount, @NotNull Action action) {
      if (container == 0 && this.storage.canReceive()) {
         long toInsert = UnitDisplayUtils.EnergyUnit.FORGE_ENERGY.convertToAsLong(amount);
         if (toInsert > 0L) {
            long inserted = this.storage.receiveEnergyL(toInsert, action.simulate());
            if (inserted > 0L) {
               return amount.subtract(UnitDisplayUtils.EnergyUnit.FORGE_ENERGY.convertFrom(inserted));
            }
         }
      }

      return amount;
   }

   @Override
   public FloatingLong extractEnergy(int container, FloatingLong amount, @NotNull Action action) {
      if (container == 0 && this.storage.canExtract()) {
         long toExtract = UnitDisplayUtils.EnergyUnit.FORGE_ENERGY.convertToAsLong(amount);
         if (toExtract > 0L) {
            long extracted = this.storage.extractEnergyL(toExtract, action.simulate());
            return UnitDisplayUtils.EnergyUnit.FORGE_ENERGY.convertFrom(extracted);
         }
      }

      return FloatingLong.ZERO;
   }
}
