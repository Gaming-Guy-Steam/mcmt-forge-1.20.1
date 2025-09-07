package dev.mcmt.core.effects.types;

import dev.mcmt.core.effects.*;

public final class ScheduleBlockTickEffect implements SideEffect {
    private final EffectKey key;
    private final String dim;
    private final int x, y, z;
    private final Object blockType;
    private final int delayTicks;

    public ScheduleBlockTickEffect(EffectKey key, String dim, int x, int y, int z, Object blockType, int delayTicks) {
        this.key = key;
        this.dim = dim;
        this.x = x; this.y = y; this.z = z;
        this.blockType = blockType;
        this.delayTicks = delayTicks;
    }

    @Override
    public void apply(SideEffectContext ctx) throws Exception {
        ctx.scheduleBlockTick(dim, x, y, z, blockType, delayTicks);
    }

    @Override
    public EffectKey key() { return key; }
}
