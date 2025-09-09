package mekanism.common.block.interfaces;

import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IHasTileEntity<TILE extends BlockEntity> extends EntityBlock {
   TileEntityTypeRegistryObject<? extends TILE> getTileType();

   default TILE createDummyBlockEntity() {
      return this.createDummyBlockEntity(((Block)this).m_49966_());
   }

   default TILE createDummyBlockEntity(@NotNull BlockState state) {
      return this.m_142194_(BlockPos.f_121853_, state);
   }

   default TILE m_142194_(@NotNull BlockPos pos, @NotNull BlockState state) {
      return (TILE)this.getTileType().get().m_155264_(pos, state);
   }

   @Nullable
   default <T extends BlockEntity> BlockEntityTicker<T> m_142354_(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> blockEntityType) {
      TileEntityTypeRegistryObject<? extends TILE> type = this.getTileType();
      return (BlockEntityTicker<T>)(blockEntityType == type.get() ? type.getTicker(level.f_46443_) : null);
   }

   default boolean triggerBlockEntityEvent(@NotNull BlockState state, Level level, BlockPos pos, int id, int param) {
      BlockEntity blockEntity = WorldUtils.getTileEntity(level, pos);
      return blockEntity != null && blockEntity.m_7531_(id, param);
   }
}
