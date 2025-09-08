package dev.mcmt.core.tick;

import dev.mcmt.core.blacklist.BlacklistManager;
import dev.mcmt.core.blacklist.CrashWrapper;
import dev.mcmt.core.comms.MessageBus;
import dev.mcmt.core.effects.SideEffectApplier;
import dev.mcmt.core.effects.SideEffectBuffer;
import dev.mcmt.core.effects.SideEffectContext;
import dev.mcmt.core.scheduling.AdapterRegistry;
import dev.mcmt.core.scheduling.UnsafeParallelAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Orchestrator that executes per-tick worker tasks, applies blacklisting,
 * and applies side-effects on the main thread via a SideEffectContext.
 * 
 * Extended with adapter support: snapshot on main, compute in parallel,
 * and commit on main (before side-effects).
 */
public final class TickOrchestrator implements AutoCloseable {

    private final ExecutorService pool;
    private final BlacklistManager blacklist;
    private final CrashWrapper crashWrapper;
    private final Consumer<String> logger;

    // Per-tick state
    private final ThreadLocal<List<Runnable>> fallbackLocal = ThreadLocal.withInitial(ArrayList::new);
    private final ThreadLocal<List<Runnable>> commitsLocal  = ThreadLocal.withInitial(ArrayList::new);
    private final ThreadLocal<List<Future<?>>> futuresLocal  = ThreadLocal.withInitial(ArrayList::new);

    public TickOrchestrator(int threads, BlacklistManager blacklist, Consumer<String> logger) {
        this.pool = Executors.newFixedThreadPool(threads, r -> {
            Thread t = new Thread(r, "TickWorker");
            t.setDaemon(true);
            return t;
        });
        this.blacklist = Objects.requireNonNull(blacklist);
        this.crashWrapper = new CrashWrapper(blacklist);
        this.logger = logger != null ? logger : (s) -> {};
    }

    public TickFrame beginTick() {
        fallbackLocal.set(new ArrayList<>());
        commitsLocal.set(new ArrayList<>());
        futuresLocal.set(new ArrayList<>());
        SideEffectBuffer buffer = new SideEffectBuffer();
        MessageBus bus = new MessageBus();
        return new TickFrame(buffer, bus);
    }

    public void submit(TickFrame frame, TickLoop loop) {
        final String key = loop.id();
        final Runnable unit = () -> loop.tick(frame.buffer(), frame.bus());

        if (blacklist.isBlacklisted(key)) {
            fallbackLocal.get().add(wrapSafeMain(key, unit));
            return;
        }

        Future<?> f = pool.submit(() -> {
            boolean ok = crashWrapper.runOrBlacklist(key, unit);
            if (!ok) {
                fallbackLocal.get().add(wrapSafeMain(key, unit));
            }
        });
        futuresLocal.get().add(f);
    }

    /**
     * Submit a block entity using an UnsafeParallelAdapter if available.
     * Flow: snapshot (main) -> compute (parallel) -> commit (main, before side-effects).
     *
     * @param frame       current tick frame
     * @param key         unique id for blacklist/crash tracking (e.g. "<modid>:<type>@x,y,z")
     * @param be          the block entity instance
     * @param defaultUnit fallback runnable for the normal (single-thread) tick if no adapter is found or snapshot fails
     */
    @SuppressWarnings("unchecked")
    public <T> void submitWithAdapter(TickFrame frame, String key, T be, Runnable defaultUnit) {
        // Respect existing blacklist first
        if (blacklist.isBlacklisted(key)) {
            fallbackLocal.get().add(wrapSafeMain(key, defaultUnit));
            return;
        }

        UnsafeParallelAdapter<T, Object, Object> adapter =
                (UnsafeParallelAdapter<T, Object, Object>) AdapterRegistry.find(be);

        if (adapter == null) {
            // No adapter → use standard path
            submit(frame, new TickLoop() {
                @Override public String id() { return key; }
                @Override public void tick(SideEffectBuffer buf, MessageBus bus) { defaultUnit.run(); }
            });
            return;
        }

        // Snapshot on main thread (read-only)
        var snapOpt = adapter.snapshot(be);
        if (snapOpt.isEmpty()) {
            // Could not snapshot → fallback to default unit via standard path to get crash/blacklist semantics
            submit(frame, new TickLoop() {
                @Override public String id() { return key; }
                @Override public void tick(SideEffectBuffer buf, MessageBus bus) { defaultUnit.run(); }
            });
            return;
        }
        Object snapshot = snapOpt.get();

        // Parallel compute
        Future<?> f = pool.submit(() -> {
            String computeKey = key + "#compute";
            boolean okCompute = crashWrapper.runOrBlacklist(computeKey, () -> {
                Object result = adapter.compute(snapshot);

                // Defer commit to main thread, guarded as well
                commitsLocal.get().add(() -> {
                    String commitKey = key + "#commit";
                    boolean okCommit = crashWrapper.runOrBlacklist(commitKey, () -> adapter.commit(be, result));
                    if (!okCommit) {
                        logger.accept("[Orchestrator] Commit failed for " + key + " (blacklisted commit).");
                    }
                });
            });

            if (!okCompute) {
                // If compute failed/blacklisted, schedule fallback default tick on main
                fallbackLocal.get().add(wrapSafeMain(key, defaultUnit));
            }
        });
        futuresLocal.get().add(f);
    }

    public void endTick(TickFrame frame, SideEffectContext ctx) {
        var futures = futuresLocal.get();
        for (Future<?> f : futures) {
            try {
                f.get();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                logger.accept("[Orchestrator] Interrupted while waiting for workers.");
            } catch (ExecutionException ee) {
                logger.accept("[Orchestrator] Worker future reported: " + ee.getCause());
            }
        }
        futures.clear();

        // Run any tasks that had to fall back to main-thread (original behavior)
        var fallbacks = fallbackLocal.get();
        for (Runnable r : fallbacks) {
            try {
                r.run();
            } catch (Throwable t) {
                logger.accept("[Orchestrator] Main-thread fallback for task threw: " + t);
            }
        }
        fallbacks.clear();

        // Run adapter commit tasks on main thread BEFORE applying side-effects
        var commits = commitsLocal.get();
        for (Runnable r : commits) {
            try {
                r.run();
            } catch (Throwable t) {
                logger.accept("[Orchestrator] Commit task failed on main: " + t);
            }
        }
        commits.clear();

        // Apply buffered side-effects (sorted) on main thread via context
        try {
            SideEffectApplier.applyAll(frame.buffer().drainSorted(), ctx);
        } catch (Exception e) {
            logger.accept("[Orchestrator] Applying side-effects failed: " + e);
        }
    }

    private Runnable wrapSafeMain(String key, Runnable unit) {
        return () -> {
            try {
                unit.run();
            } catch (Throwable t) {
                logger.accept("[Orchestrator] Main-thread fallback for " + key + " threw: " + t);
            }
        };
    }

    @Override
    public void close() {
        pool.shutdownNow();
    }

    public record TickFrame(SideEffectBuffer buffer, MessageBus bus) {
        public SideEffectBuffer buffer() { return buffer; }
        public MessageBus bus() { return bus; }
    }

    // TickLoop is assumed to be defined elsewhere in your codebase in the same package.
    // public interface TickLoop {
    //     String id();
    //     void tick(SideEffectBuffer buffer, MessageBus bus);
    // }
}
