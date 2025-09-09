package dev.mcmt.forge.integration.mekanism;

import dev.mcmt.core.scheduling.UnsafeParallelAdapter;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.recipe.lookup.monitor.FactoryRecipeCacheLookupMonitor;
import mekanism.common.tile.factory.TileEntityFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.IntSupplier;

public class MekFactoryAdapter implements UnsafeParallelAdapter<TileEntityFactory<?>, MekFactoryAdapter.FactorySnapshot, MekFactoryAdapter.FactoryResult> {

    private static Field F_recipeMonitors;
    private static Field F_cachedRecipe;
    private static Field F_perTickEnergy;
    private static Field F_requiredTicks;

    static {
        try {
            Class<?> clsFactory = Class.forName("mekanism.common.tile.factory.TileEntityFactory");
            F_recipeMonitors = clsFactory.getDeclaredField("recipeCacheLookupMonitors");
            F_recipeMonitors.setAccessible(true);

            Class<?> clsMonitor = Class.forName("mekanism.common.recipe.lookup.monitor.FactoryRecipeCacheLookupMonitor");
            F_cachedRecipe = clsMonitor.getDeclaredField("cachedRecipe");
            F_cachedRecipe.setAccessible(true);

            Class<?> clsCached = Class.forName("mekanism.api.recipes.cache.CachedRecipe");
            F_perTickEnergy = clsCached.getDeclaredField("perTickEnergy");
            F_perTickEnergy.setAccessible(true);
            F_requiredTicks = clsCached.getDeclaredField("requiredTicks");
            F_requiredTicks.setAccessible(true);
        } catch (Throwable t) {
            throw new RuntimeException("[MCMT-Mek] Reflectie initialisatie mislukt", t);
        }
    }

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
            List<Object> cachedCopy = new ArrayList<>(slots);

            @SuppressWarnings("unchecked")
            FactoryRecipeCacheLookupMonitor<?>[] monitors =
                (FactoryRecipeCacheLookupMonitor<?>[]) F_recipeMonitors.get(be);

            for (int i = 0; i < slots; i++) {
                FactoryRecipeCacheLookupMonitor<?> mon = monitors != null && i < monitors.length ? monitors[i] : null;
                Object cached = mon != null ? F_cachedRecipe.get(mon) : null;
                cachedCopy.add(cached);
                progressCopy[i] = be.progress[i];
            }

            return Optional.of(new FactorySnapshot(energy, progressCopy, cachedCopy));
        } catch (Throwable t) {
            return Optional.empty();
        }
    }

    @Override
    public FactoryResult compute(FactorySnapshot snap) {
        int[] newProgress = snap.progress().clone();
        FloatingLong energyDelta = FloatingLong.ZERO;

        for (int i = 0; i < newProgress.length; i++) {
            Object cached = snap.cached().get(i);
            if (cached == null) continue;

            try {
                @SuppressWarnings("unchecked")
                FloatingLongSupplier perTick = (FloatingLongSupplier) F_perTickEnergy.get(cached);
                IntSupplier req = (IntSupplier) F_requiredTicks.get(cached);

                if (perTick == null || req == null) continue;

                FloatingLong cost = perTick.get();
                if (snap.energy().greaterOrEqual(cost)) {
                    newProgress[i]++;
                    energyDelta = energyDelta.subtract(cost);
                    if (newProgress[i] >= req.getAsInt()) {
                        newProgress[i] = 0;
                    }
                }
            } catch (Throwable ignored) {
            }
        }
        return new FactoryResult(newProgress, energyDelta);
    }

    @Override
    public void commit(TileEntityFactory<?> be, FactoryResult result) {
        MachineEnergyContainer<?> energyContainer = be.getEnergyContainer();

        if (result.energyDelta().compareTo(FloatingLong.ZERO) < 0) {
            FloatingLong delta = result.energyDelta().copy().multiply(-1);
            energyContainer.extract(delta, Action.EXECUTE, AutomationType.INTERNAL);
        } else if (result.energyDelta().compareTo(FloatingLong.ZERO) > 0) {
            energyContainer.insert(result.energyDelta(), Action.EXECUTE, AutomationType.INTERNAL);
        }

        for (int i = 0; i < result.newProgress().length; i++) {
            be.progress[i] = result.newProgress()[i];
        }
    }

    public record FactorySnapshot(FloatingLong energy, int[] progress, List<Object> cached) {}
    public record FactoryResult(int[] newProgress, FloatingLong energyDelta) {}
}
