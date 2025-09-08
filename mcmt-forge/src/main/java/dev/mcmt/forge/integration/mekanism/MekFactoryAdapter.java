package dev.mcmt.forge.integration.mekanism;

import dev.mcmt.core.scheduling.UnsafeParallelAdapter;
import mekanism.api.Action;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.tile.factory.TileEntityFactory;
import net.minecraft.server.level.ServerLevel;

import java.util.List;
import java.util.Optional;

public class MekFactoryAdapter implements UnsafeParallelAdapter<TileEntityFactory, MekFactoryAdapter.FactorySnapshot, MekFactoryAdapter.FactoryResult> {

    @Override
    public boolean matches(Object be) {
        return be instanceof TileEntityFactory;
    }

    @Override
    public Optional<FactorySnapshot> snapshot(TileEntityFactory be) {
        try {
            MachineEnergyContainer<TileEntityFactory> energyContainer = be.getEnergyContainer();
            long energy = energyContainer.getEnergy();
            int[] progress = be.getProgress().clone();
            List<IInventorySlot> inputs = be.getInputSlots();
            List<IInventorySlot> outputs = be.getOutputSlots();
            CachedRecipe<?>[] cached = be.getCachedRecipes();

            String dimId = "minecraft:overworld";
            if (be.getLevel() instanceof ServerLevel sl) {
                dimId = sl.dimension().location().toString();
            }

            return Optional.of(new FactorySnapshot(dimId, energy, progress, inputs, outputs, cached));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public FactoryResult compute(FactorySnapshot snap) {
        int[] newProgress = snap.progress().clone();
        long energyDelta = 0;

        for (int i = 0; i < newProgress.length; i++) {
            CachedRecipe<?> cached = snap.cachedRecipes()[i];
            if (cached == null) continue;

            long perTickUsage = cached.getEnergyRequired();
            if (snap.energy() + energyDelta < perTickUsage) continue;

            newProgress[i]++;
            energyDelta -= perTickUsage;

            if (newProgress[i] >= cached.getTicksRequired()) {
                newProgress[i] = 0;
                cached.setFinished(true);
            }
        }

        return new FactoryResult(newProgress, energyDelta, snap.cachedRecipes());
    }

    @Override
    public void commit(TileEntityFactory be, FactoryResult result) {
        try {
            be.setProgress(result.newProgress());

            MachineEnergyContainer<TileEntityFactory> energyContainer = be.getEnergyContainer();
            if (result.energyDelta() < 0) {
                energyContainer.extract(-result.energyDelta(), Action.EXECUTE);
            } else if (result.energyDelta() > 0) {
                energyContainer.insert(result.energyDelta(), Action.EXECUTE);
            }

            for (CachedRecipe<?> cached : result.cachedRecipes()) {
                if (cached != null && cached.isFinished()) {
                    cached.finishProcessing(be);
                }
            }

            be.setChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public record FactorySnapshot(String dimId, long energy, int[] progress,
                                  List<IInventorySlot> inputs, List<IInventorySlot> outputs,
                                  CachedRecipe<?>[] cachedRecipes) {}
    public record FactoryResult(int[] newProgress, long energyDelta, CachedRecipe<?>[] cachedRecipes) {}
}
