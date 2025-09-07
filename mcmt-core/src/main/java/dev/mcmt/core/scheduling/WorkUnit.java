package dev.mcmt.core.scheduling;

import dev.mcmt.core.TaskDomain;

import java.util.Objects;

/**
 * A unit of tick work with metadata for scheduling, blacklisting, and fallback.
 *
 * Integration layer constructs these per tick from Minecraft sources.
 * - parallelAction is executed on a worker thread (unless blacklisted).
 * - mainFallbackAction runs on the real main thread when:
 *     a) the class is blacklisted, or
 *     b) the parallel action crashed (we retry on main in the same tick).
 */
public final class WorkUnit {

    public final TaskDomain domain;
    public final String dimensionId;
    public final int chunkX;
    public final int chunkZ;

    /**
     * Fully qualified class name of the entity/block entity/etc. blamed for crashes.
     * Used for blacklisting and routing decisions.
     */
    public final String blamedClassName;

    /**
     * Human-friendly description for logging/metrics (e.g., "BE Pump @ x,y,z").
     */
    public final String description;

    /**
     * Executes the tick's "simulate" logic in parallel.
     */
    public final Runnable parallelAction;

    /**
     * Executes the same logic on main thread to preserve compatibility.
     * Must be side-effect equivalent to parallelAction.
     */
    public final Runnable mainFallbackAction;

    public WorkUnit(
            TaskDomain domain,
            String dimensionId,
            int chunkX,
            int chunkZ,
            String blamedClassName,
            String description,
            Runnable parallelAction,
            Runnable mainFallbackAction
    ) {
        this.domain = Objects.requireNonNull(domain, "domain");
        this.dimensionId = Objects.requireNonNull(dimensionId, "dimensionId");
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.blamedClassName = blamedClassName; // may be null if unknown
        this.description = description != null ? description : "";
        this.parallelAction = Objects.requireNonNull(parallelAction, "parallelAction");
        this.mainFallbackAction = Objects.requireNonNull(mainFallbackAction, "mainFallbackAction");
    }
}
