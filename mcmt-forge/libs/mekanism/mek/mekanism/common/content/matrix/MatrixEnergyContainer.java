package mekanism.common.content.matrix;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Map;
import java.util.Set;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.tier.InductionProviderTier;
import mekanism.common.tile.multiblock.TileEntityInductionCell;
import mekanism.common.tile.multiblock.TileEntityInductionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

@NothingNullByDefault
public class MatrixEnergyContainer implements IEnergyContainer {
   private final Map<BlockPos, InductionProviderTier> providers = new Object2ObjectOpenHashMap();
   private final Map<BlockPos, IEnergyContainer> cells = new Object2ObjectOpenHashMap();
   private final Set<BlockPos> invalidPositions = new ObjectOpenHashSet();
   private FloatingLong queuedOutput = FloatingLong.ZERO;
   private FloatingLong queuedInput = FloatingLong.ZERO;
   private FloatingLong lastOutput = FloatingLong.ZERO;
   private FloatingLong lastInput = FloatingLong.ZERO;
   private FloatingLong cachedTotal = FloatingLong.ZERO;
   private FloatingLong transferCap = FloatingLong.ZERO;
   private FloatingLong storageCap = FloatingLong.ZERO;
   private final MatrixMultiblockData multiblock;

   public MatrixEnergyContainer(MatrixMultiblockData multiblock) {
      this.multiblock = multiblock;
   }

   public void addCell(BlockPos pos, TileEntityInductionCell cell) {
      MachineEnergyContainer<TileEntityInductionCell> energyContainer = cell.getEnergyContainer();
      this.cells.put(pos, energyContainer);
      this.storageCap = this.storageCap.plusEqual(energyContainer.getMaxEnergy());
      this.cachedTotal = this.cachedTotal.plusEqual(energyContainer.getEnergy());
   }

   public void addProvider(BlockPos pos, TileEntityInductionProvider provider) {
      this.providers.put(pos, provider.tier);
      this.transferCap = this.transferCap.plusEqual(provider.tier.getOutput());
   }

   public void removeInternal(BlockPos pos) {
      if (this.invalidPositions.add(pos)) {
         if (this.providers.containsKey(pos)) {
            this.transferCap = this.transferCap.minusEqual(this.providers.get(pos).getOutput());
         } else if (this.cells.containsKey(pos)) {
            IEnergyContainer cellContainer = this.cells.get(pos);
            this.storageCap = this.storageCap.plusEqual(cellContainer.getMaxEnergy());
            this.cachedTotal = this.cachedTotal.minusEqual(cellContainer.getEnergy());
         }
      }
   }

   public void invalidate() {
      this.tick();
      this.cells.clear();
      this.providers.clear();
      this.queuedOutput = FloatingLong.ZERO;
      this.queuedInput = FloatingLong.ZERO;
      this.lastOutput = FloatingLong.ZERO;
      this.lastInput = FloatingLong.ZERO;
      this.cachedTotal = FloatingLong.ZERO;
      this.transferCap = FloatingLong.ZERO;
      this.storageCap = FloatingLong.ZERO;
   }

   public void tick() {
      if (!this.invalidPositions.isEmpty()) {
         for (BlockPos invalidPosition : this.invalidPositions) {
            this.cells.remove(invalidPosition);
            this.providers.remove(invalidPosition);
         }

         this.invalidPositions.clear();
      }

      int compare = this.queuedInput.compareTo(this.queuedOutput);
      if (compare < 0) {
         this.removeEnergy(this.queuedOutput.subtract(this.queuedInput));
      } else if (compare > 0) {
         this.addEnergy(this.queuedInput.subtract(this.queuedOutput));
      }

      this.lastInput = this.queuedInput;
      this.lastOutput = this.queuedOutput;
      this.queuedInput = FloatingLong.ZERO;
      this.queuedOutput = FloatingLong.ZERO;
   }

   private void addEnergy(FloatingLong energy) {
      this.cachedTotal = this.cachedTotal.plusEqual(energy);

      for (IEnergyContainer container : this.cells.values()) {
         FloatingLong remainder = container.insert(energy, Action.EXECUTE, AutomationType.INTERNAL);
         if (remainder.smallerThan(energy)) {
            if (remainder.isZero()) {
               break;
            }

            energy = remainder;
         }
      }
   }

   private void removeEnergy(FloatingLong energy) {
      this.cachedTotal = this.cachedTotal.minusEqual(energy);

      for (IEnergyContainer container : this.cells.values()) {
         FloatingLong extracted = container.extract(energy, Action.EXECUTE, AutomationType.INTERNAL);
         if (!extracted.isZero()) {
            energy = energy.minusEqual(extracted);
            if (energy.isZero()) {
               break;
            }
         }
      }
   }

   @Override
   public FloatingLong getEnergy() {
      return this.cachedTotal.add(this.queuedInput).subtract(this.queuedOutput);
   }

   @Override
   public void setEnergy(FloatingLong energy) {
      throw new RuntimeException("Unexpected call to setEnergy. The matrix energy container does not support directly setting the energy.");
   }

   @Override
   public FloatingLong insert(FloatingLong amount, Action action, AutomationType automationType) {
      if (!amount.isZero() && this.multiblock.isFormed()) {
         FloatingLong toAdd = amount.min(this.getRemainingInput()).min(this.getNeeded());
         if (toAdd.isZero()) {
            return amount;
         } else {
            if (action.execute()) {
               this.queuedInput = this.queuedInput.plusEqual(toAdd);
            }

            return amount.subtract(toAdd);
         }
      } else {
         return amount;
      }
   }

   @Override
   public FloatingLong extract(FloatingLong amount, Action action, AutomationType automationType) {
      if (!this.isEmpty() && !amount.isZero() && this.multiblock.isFormed()) {
         amount = amount.min(this.getRemainingOutput()).min(this.getEnergy());
         if (!amount.isZero() && action.execute()) {
            this.queuedOutput = this.queuedOutput.plusEqual(amount);
         }

         return amount;
      } else {
         return FloatingLong.ZERO;
      }
   }

   @Override
   public FloatingLong getMaxEnergy() {
      return this.storageCap;
   }

   @Override
   public void onContentsChanged() {
   }

   @Override
   public CompoundTag serializeNBT() {
      return new CompoundTag();
   }

   public void deserializeNBT(CompoundTag nbt) {
   }

   private FloatingLong getRemainingInput() {
      return this.transferCap.subtract(this.queuedInput);
   }

   private FloatingLong getRemainingOutput() {
      return this.transferCap.subtract(this.queuedOutput);
   }

   public FloatingLong getMaxTransfer() {
      return this.transferCap;
   }

   public FloatingLong getLastInput() {
      return this.lastInput;
   }

   public FloatingLong getLastOutput() {
      return this.lastOutput;
   }

   public int getCells() {
      return this.cells.size();
   }

   public int getProviders() {
      return this.providers.size();
   }
}
