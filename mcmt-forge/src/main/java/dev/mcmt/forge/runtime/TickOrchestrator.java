// mcmt-forge/src/main/java/dev/mcmt/forge/runtime/TickOrchestrator.java
package dev.mcmt.forge.runtime;

import dev.mcmt.core.effects.SideEffectApplier;
import dev.mcmt.core.effects.SideEffectBuffer;
import dev.mcmt.core.world.LevelProxy;
import dev.mcmt.forge.integration.ForgeSideEffectContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Consumer;

public final class TickOrchestrator {

    public interface LevelIterator {
        List<LevelView> listLevels();
    }

    public interface LevelComputeProvider {
        void enqueueComputeTasks(LevelView level, LevelProxy proxy, Consumer<Runnable> submit);
    }

    public static final class LevelView {
        public final String dimensionId;
        public final Object level; // ServerLevel

        public LevelView(String dimensionId, Object level) {
            this.dimensionId = dimensionId;
            this.level = level;
        }
    }

    private final ExecutorService workerPool;
    private final LevelIterator levelIterator;
    private final LevelComputeProvider computeProvider;
    private final ForgeSideEffectContext ctx;

    private final List<Future<?>> inFlight = new ArrayList<>();
    private SideEffectBuffer buffer;

    public TickOrchestrator(ExecutorService workerPool,
                            LevelIterator levelIterator,
                            LevelComputeProvider computeProvider,
                            ForgeSideEffectContext ctx) {
        this.workerPool = Objects.requireNonNull(workerPool);
        this.levelIterator = Objects.requireNonNull(levelIterator);
        this.computeProvider = Objects.requireNonNull(computeProvider);
        this.ctx = Objects.requireNonNull(ctx);
    }

    public void beginTick() {
        buffer = new SideEffectBuffer();
        inFlight.clear();

        for (LevelView lv : levelIterator.listLevels()) {
            LevelProxy proxy = new LevelProxy(lv.dimensionId, buffer);
            computeProvider.enqueueComputeTasks(lv, proxy, runnable -> {
                Future<?> f = workerPool.submit(runnable);
                synchronized (inFlight) { inFlight.add(f); }
            });
        }
    }

    public void endTick() {
        List<Future<?>> wait;
        synchronized (inFlight) { wait = List.copyOf(inFlight); }
        for (Future<?> f : wait) {
            try {
                f.get();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted waiting for compute", ie);
            } catch (ExecutionException ee) {
                throw new RuntimeException("Compute task failed", ee.getCause());
            }
        }

        try {
            var ordered = buffer.drainSorted();
            SideEffectApplier.applyAll(ordered, ctx);
        } catch (Exception e) {
            throw new RuntimeException("Applying side-effects failed", e);
        } finally {
            buffer = null;
            inFlight.clear();
        }
    }
}
