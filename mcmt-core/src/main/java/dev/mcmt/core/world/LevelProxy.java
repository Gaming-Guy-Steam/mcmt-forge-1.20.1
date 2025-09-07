package dev.mcmt.core.world;

import dev.mcmt.core.effects.*;
import dev.mcmt.core.effects.types.ScheduleBlockTickEffect;
import dev.mcmt.core.effects.types.SetBlockEffect;
import dev.mcmt.core.effects.types.SpawnEntityEffect;
import dev.mcmt.core.effects.types.SetBlockEntityDataEffect;

/**
 * Producer-side API to enqueue world mutations as side-effects.
 */
public final class LevelProxy {
    private final String dim;
    private final SideEffectBuffer buffer;

    public LevelProxy(String dim, SideEffectBuffer buffer) {
        this.dim = dim;
        this.buffer = buffer;
    }

    public void setBlock(int x, int y, int z, Object stateHandle, int flags) {
        long seq = buffer.nextSeq();
        EffectKey key = new EffectKey(dim, x >> 4, z >> 4, 0, seq);
        buffer.add(new SetBlockEffect(key, dim, x, y, z, stateHandle, flags));
    }

    public void scheduleBlockTick(int x, int y, int z, Object blockTypeHandle, int delay) {
        long seq = buffer.nextSeq();
        EffectKey key = new EffectKey(dim, x >> 4, z >> 4, 1, seq);
        buffer.add(new ScheduleBlockTickEffect(key, dim, x, y, z, blockTypeHandle, delay));
    }

    public void spawnEntity(Object entitySpecHandle) {
        long seq = buffer.nextSeq();
        EffectKey key = new EffectKey(dim, 0, 0, 2, seq);
        buffer.add(new SpawnEntityEffect(key, dim, entitySpecHandle));
    }

    public void setBlockEntityData(int x, int y, int z, Object nbtDataHandle) {
        long seq = buffer.nextSeq();
        EffectKey key = new EffectKey(dim, x >> 4, z >> 4, 3, seq);
        buffer.add(new SetBlockEntityDataEffect(key, dim, x, y, z, nbtDataHandle));
    }
}
