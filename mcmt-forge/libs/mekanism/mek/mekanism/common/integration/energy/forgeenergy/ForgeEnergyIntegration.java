package mekanism.common.integration.energy.forgeenergy;

import mekanism.api.Action;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.math.FloatingLong;
import mekanism.common.util.UnitDisplayUtils;
import net.minecraftforge.energy.IEnergyStorage;

public class ForgeEnergyIntegration implements IEnergyStorage {
   private final IStrictEnergyHandler handler;

   public ForgeEnergyIntegration(IStrictEnergyHandler handler) {
      this.handler = handler;
   }

   public int receiveEnergy(int maxReceive, boolean simulate) {
      if (maxReceive <= 0) {
         return 0;
      } else {
         Action action = Action.get(!simulate);
         FloatingLong toInsert = UnitDisplayUtils.EnergyUnit.FORGE_ENERGY.convertFrom(maxReceive);
         if (action.execute()) {
            FloatingLong simulatedRemainder = this.handler.insertEnergy(toInsert, Action.SIMULATE);
            if (simulatedRemainder.equals(toInsert)) {
               return 0;
            }

            FloatingLong simulatedInserted = toInsert.subtract(simulatedRemainder);
            toInsert = this.convertToAndBack(simulatedInserted);
            if (toInsert.isZero()) {
               return 0;
            }
         }

         FloatingLong remainder = this.handler.insertEnergy(toInsert, action);
         if (remainder.equals(toInsert)) {
            return 0;
         } else {
            FloatingLong inserted = toInsert.subtract(remainder);
            return UnitDisplayUtils.EnergyUnit.FORGE_ENERGY.convertToAsInt(inserted);
         }
      }
   }

   public int extractEnergy(int maxExtract, boolean simulate) {
      if (maxExtract <= 0) {
         return 0;
      } else {
         Action action = Action.get(!simulate);
         FloatingLong toExtract = UnitDisplayUtils.EnergyUnit.FORGE_ENERGY.convertFrom(maxExtract);
         if (action.execute()) {
            FloatingLong simulatedExtracted = this.handler.extractEnergy(toExtract, Action.SIMULATE);
            toExtract = this.convertToAndBack(simulatedExtracted);
            if (toExtract.isZero()) {
               return 0;
            }
         }

         FloatingLong extracted = this.handler.extractEnergy(toExtract, action);
         return UnitDisplayUtils.EnergyUnit.FORGE_ENERGY.convertToAsInt(extracted);
      }
   }

   private FloatingLong convertToAndBack(FloatingLong value) {
      return UnitDisplayUtils.EnergyUnit.FORGE_ENERGY.convertFrom(UnitDisplayUtils.EnergyUnit.FORGE_ENERGY.convertToAsInt(value));
   }

   public int getEnergyStored() {
      int energy = 0;
      int container = 0;

      for (int containers = this.handler.getEnergyContainerCount(); container < containers; container++) {
         int total = UnitDisplayUtils.EnergyUnit.FORGE_ENERGY.convertToAsInt(this.handler.getEnergy(container));
         if (total > Integer.MAX_VALUE - energy) {
            return Integer.MAX_VALUE;
         }

         energy += total;
      }

      return energy;
   }

   public int getMaxEnergyStored() {
      int maxEnergy = 0;
      int container = 0;

      for (int containers = this.handler.getEnergyContainerCount(); container < containers; container++) {
         int max = UnitDisplayUtils.EnergyUnit.FORGE_ENERGY.convertToAsInt(this.handler.getMaxEnergy(container));
         if (max > Integer.MAX_VALUE - maxEnergy) {
            return Integer.MAX_VALUE;
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
