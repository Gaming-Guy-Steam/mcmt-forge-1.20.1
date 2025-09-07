package dev.mcmt.core.effects.types;

import dev.mcmt.core.effects.*;

public final class SetBlockEffect implements SideEffect {
    private final EffectKey key;
    private final String dim;
    private final int x, y, z;
    private final Object state;
    private final int flags;

    public SetBlockEffect(EffectKey key, String dim, int x, int y, int z, Object state, int flags) {
        this.key = key; this.dim = dim;
        this.x = x; this.y = y; this.z = z;
        this.state = state; this.flags = flags;
    }

    @Override public void apply(SideEffectContext ctx) throws Exception {
        ctx.setBlock(dim, x, y, z, state, flags);
    }
    @Override public EffectKey key() { return key; }
}
