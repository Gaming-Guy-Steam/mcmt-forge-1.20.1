package dev.mcmt.core.effects;

/**
 * Bridge to the actual game engine (Forge integration will implement this).
 */
public interface SideEffectContext {
    void setBlock(String dimensionId, int x, int y, int z, Object blockStateHandle, int flags) throws Exception;

    void scheduleBlockTick(String dimensionId, int x, int y, int z, Object blockTypeHandle, int delayTicks) throws Exception;

    void spawnEntity(String dimensionId, Object entitySpecHandle) throws Exception;

    void setBlockEntityData(String dimensionId, int x, int y, int z, Object nbtData) throws Exception;
}
