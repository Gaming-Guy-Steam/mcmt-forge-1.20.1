package dev.mcmt.core.effects;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Collects world mutations during parallel phase; applied on main thread in a stable order.
 */
public final class SideEffectBuffer {
    private final List<SideEffect> effects = new ArrayList<>(256);
    private final AtomicLong seq = new AtomicLong();

    public long nextSeq() { return seq.incrementAndGet(); }

    public synchronized void add(SideEffect e) {
        effects.add(e);
    }

    public synchronized int size() {
        return effects.size();
    }

    /**
     * Returns and clears all effects, ordered by their EffectKey.
     */
    public synchronized List<SideEffect> drainSorted() {
        List<SideEffect> snapshot = new ArrayList<>(effects);
        effects.clear();
        snapshot.sort(Comparator.comparing(SideEffect::key));
        return snapshot;
    }
}
