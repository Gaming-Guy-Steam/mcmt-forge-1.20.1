package dev.mcmt.core.scheduling;

import java.util.Optional;

public interface UnsafeParallelAdapter<T, Snapshot, Result> {
    boolean matches(Object be);
    Optional<Snapshot> snapshot(T be);
    Result compute(Snapshot snap);
    void commit(T be, Result result);
}
