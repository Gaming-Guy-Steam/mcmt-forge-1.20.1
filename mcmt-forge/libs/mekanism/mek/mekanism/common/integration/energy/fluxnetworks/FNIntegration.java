package mekanism.common.integration.energy.fluxnetworks;

import mekanism.api.Action;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.math.FloatingLong;
import mekanism.common.util.UnitDisplayUtils;
import sonar.fluxnetworks.api.energy.IFNEnergyStorage;

public class FNIntegration implements IFNEnergyStorage {
   private final IStrictEnergyHandler handler;

   public FNIntegration(IStrictEnergyHandler handler) {
      this.handler = handler;
   }

   public long receiveEnergyL(long maxReceive, boolean simulate) {
      if (maxReceive <= 0L) {
         return 0L;
      } else {
         Action action = Action.get(!simulate);
         FloatingLong toInsert = UnitDisplayUtils.EnergyUnit.FORGE_ENERGY.convertFrom(maxReceive);
         if (action.execute()) {
            FloatingLong simulatedRemainder = this.handler.insertEnergy(toInsert, Action.SIMULATE);
            if (simulatedRemainder.equals(toInsert)) {
               return 0L;
            }

            FloatingLong simulatedInserted = toInsert.subtract(simulatedRemainder);
            toInsert = this.convertToAndBack(simulatedInserted);
            if (toInsert.isZero()) {
               return 0L;
            }
         }

         FloatingLong remainder = this.handler.insertEnergy(toInsert, action);
         if (remainder.equals(toInsert)) {
            return 0L;
         } else {
            FloatingLong inserted = toInsert.subtract(remainder);
            return UnitDisplayUtils.EnergyUnit.FORGE_ENERGY.convertToAsLong(inserted);
         }
      }
   }

   public long extractEnergyL(long maxExtract, boolean simulate) {
      if (maxExtract <= 0L) {
         return 0L;
      } else {
         Action action = Action.get(!simulate);
         FloatingLong toExtract = UnitDisplayUtils.EnergyUnit.FORGE_ENERGY.convertFrom(maxExtract);
         if (action.execute()) {
            FloatingLong simulatedExtracted = this.handler.extractEnergy(toExtract, Action.SIMULATE);
            toExtract = this.convertToAndBack(simulatedExtracted);
            if (toExtract.isZero()) {
               return 0L;
            }
         }

         FloatingLong extracted = this.handler.extractEnergy(toExtract, action);
         return UnitDisplayUtils.EnergyUnit.FORGE_ENERGY.convertToAsLong(extracted);
      }
   }

   private FloatingLong convertToAndBack(FloatingLong value) {
      return UnitDisplayUtils.EnergyUnit.FORGE_ENERGY.convertFrom(UnitDisplayUtils.EnergyUnit.FORGE_ENERGY.convertToAsLong(value));
   }

   public long getEnergyStoredL() {
      long energy = 0L;
      int container = 0;

      for (int containers = this.handler.getEnergyContainerCount(); container < containers; container++) {
         long total = UnitDisplayUtils.EnergyUnit.FORGE_ENERGY.convertToAsLong(this.handler.getEnergy(container));
         if (total > Long.MAX_VALUE - energy) {
            return Long.MAX_VALUE;
         }

         energy += total;
      }

      return energy;
   }

   public long getMaxEnergyStoredL() {
      long maxEnergy = 0L;
      int container = 0;

      for (int containers = this.handler.getEnergyContainerCount(); container < containers; container++) {
         long max = UnitDisplayUtils.EnergyUnit.FORGE_ENERGY.convertToAsLong(this.handler.getMaxEnergy(container));
         if (max > Long.MAX_VALUE - maxEnergy) {
            return Long.MAX_VALUE;
         }

         maxEnergy += max;
      }

      return maxEnergy;
   }

   public boolean canExtract() {
      if (!this.handler.extractEnergy(FloatingLong.ONE, Action.SIMULATE).isZero()) {
         return true;
      } else {
         int container = 0;

         for (int containers = this.handler.getEnergyContainerCount(); container < containers; container++) {
            if (!this.handler.getEnergy(container).isZero()) {
               return false;
            }
         }

         return true;
      }
   }

   public boolean canReceive() {
      if (this.handler.insertEnergy(FloatingLong.ONE, Action.SIMULATE).smallerThan(FloatingLong.ONE)) {
         return true;
      } else {
         int container = 0;

         for (int containers = this.handler.getEnergyContainerCount(); container < containers; container++) {
            if (!this.handler.getNeededEnergy(container).isZero()) {
               return false;
            }
         }

         return true;
      }
   }
}
