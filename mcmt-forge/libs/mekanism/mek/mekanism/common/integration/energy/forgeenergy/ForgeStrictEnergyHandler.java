package mekanism.common.integration.energy.forgeenergy;

import mekanism.api.Action;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.math.FloatingLong;
import mekanism.common.util.UnitDisplayUtils;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class ForgeStrictEnergyHandler implements IStrictEnergyHandler {
   private final IEnergyStorage storage;

   public ForgeStrictEnergyHandler(IEnergyStorage storage) {
      this.storage = storage;
   }

   @Override
   public int getEnergyContainerCount() {
      return 1;
   }

   @Override
   public FloatingLong getEnergy(int container) {
      return container == 0 ? UnitDisplayUtils.EnergyUnit.FORGE_ENERGY.convertFrom(this.storage.getEnergyStored()) : FloatingLong.ZERO;
   }

   @Override
   public void setEnergy(int container, FloatingLong energy) {
   }

   @Override
   public FloatingLong getMaxEnergy(int container) {
      return container == 0 ? UnitDisplayUtils.EnergyUnit.FORGE_ENERGY.convertFrom(this.storage.getMaxEnergyStored()) : FloatingLong.ZERO;
   }

   @Override
   public FloatingLong getNeededEnergy(int container) {
      return container == 0
         ? UnitDisplayUtils.EnergyUnit.FORGE_ENERGY.convertFrom(Math.max(0, this.storage.getMaxEnergyStored() - this.storage.getEnergyStored()))
         : FloatingLong.ZERO;
   }

   @Override
   public FloatingLong insertEnergy(int container, FloatingLong amount, @NotNull Action action) {
      if (container == 0 && this.storage.canReceive()) {
         int toInsert = UnitDisplayUtils.EnergyUnit.FORGE_ENERGY.convertToAsInt(amount);
         if (toInsert > 0) {
            int inserted = this.storage.receiveEnergy(toInsert, action.simulate());
            if (inserted > 0) {
               return amount.subtract(UnitDisplayUtils.EnergyUnit.FORGE_ENERGY.convertFrom(inserted));
            }
         }
      }

      return amount;
   }

   @Override
   public FloatingLong extractEnergy(int container, FloatingLong amount, @NotNull Action action) {
      if (container == 0 && this.storage.canExtract()) {
         int toExtract = UnitDisplayUtils.EnergyUnit.FORGE_ENERGY.convertToAsInt(amount);
         if (toExtract > 0) {
            int extracted = this.storage.extractEnergy(toExtract, action.simulate());
            return UnitDisplayUtils.EnergyUnit.FORGE_ENERGY.convertFrom(extracted);
         }
      }

      return FloatingLong.ZERO;
   }
}
