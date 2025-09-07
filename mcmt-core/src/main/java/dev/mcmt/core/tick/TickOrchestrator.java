package dev.mcmt.core.tick;

import dev.mcmt.core.blacklist.BlacklistManager;
import dev.mcmt.core.blacklist.CrashWrapper;
import dev.mcmt.core.comms.MessageBus;
import dev.mcmt.core.effects.SideEffectApplier;
import dev.mcmt.core.effects.SideEffectBuffer;
import dev.mcmt.core.effects.SideEffectContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Orchestrator that executes per-tick worker tasks, applies blacklisting,
 * and applies side-effects on the main thread via a SideEffectContext.
 */
public final class TickOrchestrator implements AutoCloseable {

    private final ExecutorService pool;
    private final BlacklistManager blacklist;
    private final CrashWrapper crashWrapper;
    private final Consumer<String> logger;

    // Per-tick state
    private final ThreadLocal<List<Runnable>> fallbackLocal = ThreadLocal.withInitial(ArrayList::new);
    private final ThreadLocal<List<Future<?>>> futuresLocal = ThreadLocal.withInitial(ArrayList::new);

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

        var fallbacks = fallbackLocal.get();
        for (Runnable r : fallbacks) {
            try {
                r.run();
            } catch (Throwable t) {
                logger.accept("[Orchestrator] Fallback task failed on main: " + t);
            }
        }
        fallbacks.clear();

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
}
