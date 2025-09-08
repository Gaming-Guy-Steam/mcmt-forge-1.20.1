package dev.mcmt.core.scheduling;

import java.util.ArrayList;
import java.util.List;

public class AdapterRegistry {
    private static final List<UnsafeParallelAdapter<?, ?, ?>> ADAPTERS = new ArrayList<>();

    public static void register(UnsafeParallelAdapter<?, ?, ?> adapter) {
        ADAPTERS.add(adapter);
    }

    @SuppressWarnings("unchecked")
    public static <T> UnsafeParallelAdapter<T, Object, Object> find(T be) {
        for (UnsafeParallelAdapter<?, ?, ?> a : ADAPTERS) {
            if (a.matches(be)) {
                return (UnsafeParallelAdapter<T, Object, Object>) a;
            }
        }
        return null;
    }
}
