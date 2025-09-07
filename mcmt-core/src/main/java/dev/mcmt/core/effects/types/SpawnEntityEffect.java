package dev.mcmt.core.effects.types;

import dev.mcmt.core.effects.*;

public final class SpawnEntityEffect implements SideEffect {
    private final EffectKey key;
    private final String dim;
    private final Object entitySpec;

    public SpawnEntityEffect(EffectKey key, String dim, Object entitySpec) {
        this.key = key;
        this.dim = dim;
        this.entitySpec = entitySpec;
    }

    @Override
    public void apply(SideEffectContext ctx) throws Exception {
        ctx.spawnEntity(dim, entitySpec);
    }

    @Override
    public EffectKey key() { return key; }
}
