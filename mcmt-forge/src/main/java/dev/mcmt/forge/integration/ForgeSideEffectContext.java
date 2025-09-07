package dev.mcmt.forge.integration;

import dev.mcmt.core.effects.SideEffectContext;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;
import java.util.function.Function;

/**
 * Applies side-effects to the actual Minecraft world via Forge.
 */
public final class ForgeSideEffectContext implements SideEffectContext {

    public interface LevelLookup {
        ServerLevel resolve(String dimensionId);
    }

    private final LevelLookup levels;

    public ForgeSideEffectContext(LevelLookup levels) {
        this.levels = Objects.requireNonNull(levels);
    }

    @Override
    public void setBlock(String dimensionId, int x, int y, int z, Object blockStateHandle, int flags) {
        ServerLevel level = levels.resolve(dimensionId);
        if (level == null) throw new IllegalStateException("No level for " + dimensionId);

        BlockState state = ((FBlockState) blockStateHandle).state();
        BlockPos pos = new BlockPos(x, y, z);
        level.setBlock(pos, state, flags);
    }

    @Override
    public void scheduleBlockTick(String dimensionId, int x, int y, int z, Object blockTypeHandle, int delayTicks) {
        ServerLevel level = levels.resolve(dimensionId);
        if (level == null) throw new IllegalStateException("No level for " + dimensionId);

        Block block = ((FBlockType) blockTypeHandle).block();
        BlockPos pos = new BlockPos(x, y, z);
        level.scheduleTick(pos, block, delayTicks);
    }

    @Override
    public void spawnEntity(String dimensionId, Object entitySpecHandle) {
        ServerLevel level = levels.resolve(dimensionId);
        if (level == null) throw new IllegalStateException("No level for " + dimensionId);

        Entity e = ((FEntitySpec) entitySpecHandle).factory().apply(level);
        level.addFreshEntity(e);
    }

    @Override
    public void setBlockEntityData(String dimensionId, int x, int y, int z, Object nbtData) {
        ServerLevel level = levels.resolve(dimensionId);
        if (level == null) throw new IllegalStateException("No level for " + dimensionId);

        BlockPos pos = new BlockPos(x, y, z);
        BlockEntity be = level.getBlockEntity(pos);
        if (be != null && nbtData instanceof CompoundTag tag) {
            be.load(tag.copy());
            be.setChanged();
        }
    }

    // ---- Handle wrappers ----
    public static record FBlockState(BlockState state) { }
    public static record FBlockType(Block block) { }
    public static record FEntitySpec(Function<ServerLevel, Entity> factory) { }
}
