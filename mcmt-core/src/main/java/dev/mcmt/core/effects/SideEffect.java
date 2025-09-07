package dev.mcmt.core.effects;

/**
 * Represents a world mutation captured during the parallel "simulate" phase.
 * Integration layer will implement SideEffectContext to actually apply these.
 */
public interface SideEffect {
    /**
     * Apply this effect using the provided context.
     */
    void apply(SideEffectContext ctx) throws Exception;

    /**
     * Stable ordering key for deterministic apply.
     */
    EffectKey key();
}
