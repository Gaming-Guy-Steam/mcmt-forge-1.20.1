package dev.mcmt.core.scheduling;

import dev.mcmt.core.TaskDomain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Collects WorkUnits for a single server tick and exposes:
 * - parallel execution plan
 * - main-thread fallback list (blacklisted or crashed)
 *
 * Instances are not reusable; create a new session per tick.
 */
public final class TickSession {

    private final long tickNumber;
    private final ConcurrentLinkedQueue<WorkUnit> work = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<WorkUnit> fallbackMain = new ConcurrentLinkedQueue<>();

    public TickSession(long tickNumber) {
        this.tickNumber = tickNumber;
    }

    public long tickNumber() {
        return tickNumber;
    }

    /**
     * Adds a task to the tick. Thread-safe.
     */
    public void add(WorkUnit unit) {
        Objects.requireNonNull(unit, "unit");
        work.add(unit);
    }

    /**
     * Returns a snapshot list of the work to do (unordered).
     */
    public List<WorkUnit> snapshotWork() {
        return new ArrayList<>(work);
    }

    /**
     * Adds a work unit to main-thread fallback queue (e.g., blacklisted or crashed).
     */
    public void enqueueFallback(WorkUnit unit) {
        if (unit != null) fallbackMain.add(unit);
    }

    /**
     * Returns fallback units ordered for stability:
     *  - by dimension, chunkX, chunkZ, then by domain to keep similar work together.
     */
   public List<WorkUnit> drainFallbackOrdered() {
    List<WorkUnit> list = new ArrayList<>(Math.max(16, fallbackMain.size()));
    for (WorkUnit u; (u = fallbackMain.poll()) != null; ) {
        list.add(u);
    }
    list.sort(Comparator
            .comparing((WorkUnit u) -> u.dimensionId)
            .thenComparingInt(u -> u.chunkX)
            .thenComparingInt(u -> u.chunkZ)
            .thenComparingInt(u -> domainOrder(u.domain))
            
    );
    return list;
}


    private static int domainOrder(TaskDomain d) {
        // Stable order to avoid weird interleavings on main
        return switch (d) {
            case BLOCK_ENTITY -> 0;
            case ENTITY -> 1;
            case FLUID_RANDOM -> 2;
            case GLOBAL -> 3;
        };
    }
}
