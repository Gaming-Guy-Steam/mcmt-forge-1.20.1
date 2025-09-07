package dev.mcmt.core.scheduling;

import dev.mcmt.core.TaskDomain;
import dev.mcmt.core.TickContext;
import dev.mcmt.core.blacklist.BlacklistManager;
import dev.mcmt.core.blacklist.CrashWrapper;
import dev.mcmt.core.concurrent.MCMTExecutor;
import dev.mcmt.core.crash.CrashListener;
import dev.mcmt.core.crash.CrashPolicy;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Orchestrates per-tick parallel execution with:
 * - unified worker pool (all domains share the same threads)
 * - blacklist-aware routing
 * - crash-to-blacklist behavior
 * - per-tick barrier (simulate phase)
 *
 * This class does not perform the "apply" phase; integration layer decides
 * when to apply world mutations on the real main thread.
 */
public final class ParallelTickScheduler {

    private final MCMTExecutor executor;
    private final BlacklistManager blacklist;
    private final CrashWrapper crashWrapper;

    // Time budget for the simulate phase barrier (defensive; integration may override per-tick).
    private volatile long simulateTimeoutMillis = 900L;

    public ParallelTickScheduler(MCMTExecutor executor,
                                 BlacklistManager blacklist,
                                 CrashPolicy policy,
                                 CrashListener listener) {
        this.executor = Objects.requireNonNull(executor, "executor");
        this.blacklist = Objects.requireNonNull(blacklist, "blacklist");
        this.crashWrapper = new CrashWrapper(blacklist, Objects.requireNonNull(policy, "policy"), listener);
    }

    public void setSimulateTimeoutMillis(long ms) {
        if (ms <= 0) throw new IllegalArgumentException("timeout must be > 0");
        this.simulateTimeoutMillis = ms;
    }

    public long getSimulateTimeoutMillis() {
        return simulateTimeoutMillis;
    }

    /**
     * Runs the simulate phase:
     * - Skips blacklisted classes and routes them to main fallback.
     * - Executes all other tasks in parallel on the worker pool.
     * - If a task crashes, it is blacklisted (per CrashPolicy) and routed to main fallback for retry in the same tick.
     *
     * Returns an optional TaskBatchException aggregating worker failures (useful for logging).
     * Even if tasks failed, all non-failing tasks will have completed when this returns.
     */
    public Optional<TaskBatchException> runSimulate(TickSession session) throws InterruptedException, TimeoutException {
        List<WorkUnit> units = session.snapshotWork();
        if (units.isEmpty()) return Optional.empty();

        // Partition: pre-route blacklisted work to main, keep the rest for workers.
        List<Runnable> parallel = new ArrayList<>(units.size());
        for (WorkUnit u : units) {
            if (isBlacklisted(u)) {
                session.enqueueFallback(u);
            } else {
                parallel.add(wrapUnit(session, u));
            }
        }

        if (parallel.isEmpty()) return Optional.empty();

        try {
            executor.invokeParallel(parallel, simulateTimeoutMillis, TimeUnit.MILLISECONDS);
            return Optional.empty();
        } catch (ExecutionException ee) {
            TaskBatchException agg = new TaskBatchException("One or more simulate tasks failed during tick " + session.tickNumber());
            agg.add("Parallel batch failed", ee.getCause());
            for (Throwable s : ee.getSuppressed()) {
                agg.add("Suppressed: " + s, s);
            }
            return Optional.of(agg);
        }
    }

    private boolean isBlacklisted(WorkUnit u) {
        return u.blamedClassName != null && blacklist.isBlacklisted(u.blamedClassName);
    }

    private Runnable wrapUnit(TickSession session, WorkUnit u) {
        // TickContext marks workers as "main-like" without touching the real main thread.
        return () -> {
            try (TickContext.Scope ignored = TickContext.enter(session.tickNumber(), domainFor(u.domain), /*mainLike*/ true)) {
                boolean ok = crashWrapper.runOrBlacklist(u.blamedClassName, u.parallelAction::run);
                if (!ok) {
                    // Schedule retry on main thread this tick
                    session.enqueueFallback(u);
                }
            }
        };
    }

    private static dev.mcmt.core.TaskDomain domainFor(TaskDomain d) {
        // Here TaskDomain is the same type; helper kept for clarity if we introduce sub-domains.
        return d;
    }
}
