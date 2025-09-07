package dev.mcmt.core.effects;

import java.util.List;

/**
 * Applies a list of side-effects using the provided context.
 */
public final class SideEffectApplier {
    private SideEffectApplier() {}

    public static void applyAll(List<SideEffect> effects, SideEffectContext ctx) throws Exception {
        for (SideEffect e : effects) {
            e.apply(ctx);
        }
    }
}
