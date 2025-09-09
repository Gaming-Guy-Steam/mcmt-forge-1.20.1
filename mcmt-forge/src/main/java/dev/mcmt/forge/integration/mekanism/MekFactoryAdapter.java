package dev.mcmt.forge.integration.mekanism;

import dev.mcmt.core.scheduling.UnsafeParallelAdapter;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.recipe.lookup.monitor.FactoryRecipeCacheLookupMonitor;
import mekanism.common.tile.factory.TileEntityFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MekFactoryAdapter implements UnsafeParallelAdapter<TileEntityFactory<?>, MekFactoryAdapter.FactorySnapshot, MekFactoryAdapter.FactoryResult> {

    @Override
    public boolean matches(Object be) {
        return be instanceof TileEntityFactory<?>;
    }

    @Override
    public Optional<FactorySnapshot> snapshot(TileEntityFactory<?> be) {
        try {
            MachineEnergyContainer<?> energyContainer = be.getEnergyContainer();
            FloatingLong energy = energyContainer.getEnergy();

            int slots = be.getSlots();
            int[] progressCopy = new int[slots];
            List<CachedRecipe<?>> cachedCopy = new ArrayList<>(slots);

            for (int i = 0; i < slots; i++) {
                FactoryRecipeCacheLookupMonitor<?> monitor = be.recipeCacheLookupMonitors[i];
                CachedRecipe<?> cached = monitor != null ? monitor.cachedRecipe : null;
                cachedCopy.add(cached);
                progressCopy[i] = be.progress[i];
            }

            return Optional.of(new FactorySnapshot(energy, progressCopy, cachedCopy));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public FactoryResult compute(FactorySnapshot snap) {
        int[] newProgress = snap.progress().clone();
        FloatingLong energyDelta = FloatingLong.ZERO;

        for (int i = 0; i < newProgress.length; i++) {
            CachedRecipe<?> cached = snap.cachedRecipes().get(i);
            if (cached != null && cached.getRecipe() != null) {
                FloatingLong perTickUsage = cached.perTickEnergy.get();
                if (snap.energy().greaterOrEqual(perTickUsage)) {
                    newProgress[i]++;
                    energyDelta = energyDelta.subtract(perTickUsage);
                    if (newProgress[i] >= cached.requiredTicks.getAsInt()) {
                        newProgress[i] = 0;
                    }
                }
            }
        }
        return new FactoryResult(newProgress, energyDelta);
    }

    @Override
    public void commit(TileEntityFactory<?> be, FactoryResult result) {
        MachineEnergyContainer<?> energyContainer = be.getEnergyContainer();

        if (result.energyDelta().compareTo(FloatingLong.ZERO) < 0) {
            energyContainer.extract(FloatingLong.create(Math.abs(result.energyDelta().longValue())), Action.EXECUTE, AutomationType.INTERNAL);
        } else if (result.energyDelta().compareTo(FloatingLong.ZERO) > 0) {
            energyContainer.insert(result.energyDelta(), Action.EXECUTE, AutomationType.INTERNAL);
        }

        for (int i = 0; i < result.newProgress().length; i++) {
            be.progress[i] = result.newProgress()[i];
        }
    }

    public record FactorySnapshot(FloatingLong energy, int[] progress, List<CachedRecipe<?>> cachedRecipes) {}
    public record FactoryResult(int[] newProgress, FloatingLong energyDelta) {}
}
