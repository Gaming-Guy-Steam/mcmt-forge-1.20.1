package dev.mcmt.core.effects.types;

import dev.mcmt.core.effects.EffectKey;
import dev.mcmt.core.effects.SideEffect;
import dev.mcmt.core.effects.SideEffectContext;

public final class SetBlockEntityDataEffect implements SideEffect {

    private final EffectKey key;
    private final String dim;
    private final int x, y, z;
    private final Object nbtData;

    public SetBlockEntityDataEffect(EffectKey key, String dim, int x, int y, int z, Object nbtData) {
        this.key = key;
        this.dim = dim;
        this.x = x;
        this.y = y;
        this.z = z;
        this.nbtData = nbtData;
    }

    @Override
    public void apply(SideEffectContext ctx) throws Exception {
        ctx.setBlockEntityData(dim, x, y, z, nbtData);
    }

    @Override
    public EffectKey key() {
        return key;
    }
}
