package dev.mcmt.core.effects;

import java.util.Objects;

/**
 * Coarse-grained ordering key: dimension, chunk coords, z-order, insertion seq.
 */
public final class EffectKey implements Comparable<EffectKey> {
    public final String dimensionId;
    public final int chunkX, chunkZ;
    public final int zOrder;
    public final long seq;

    public EffectKey(String dimensionId, int chunkX, int chunkZ, int zOrder, long seq) {
        this.dimensionId = Objects.requireNonNull(dimensionId);
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.zOrder = zOrder;
        this.seq = seq;
    }

    @Override
    public int compareTo(EffectKey o) {
        int c = dimensionId.compareTo(o.dimensionId);
        if (c != 0) return c;
        c = Integer.compare(chunkX, o.chunkX);
        if (c != 0) return c;
        c = Integer.compare(chunkZ, o.chunkZ);
        if (c != 0) return c;
        c = Integer.compare(zOrder, o.zOrder);
        if (c != 0) return c;
        return Long.compare(seq, o.seq);
    }
}
