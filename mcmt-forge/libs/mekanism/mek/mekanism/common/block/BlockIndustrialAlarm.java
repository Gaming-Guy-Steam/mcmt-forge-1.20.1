package mekanism.common.block;

import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.registries.MekanismBlockTypes;
import mekanism.common.tile.TileEntityIndustrialAlarm;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.VoxelShapeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class BlockIndustrialAlarm extends BlockTile.BlockTileModel<TileEntityIndustrialAlarm, BlockTypeTile<TileEntityIndustrialAlarm>> {
   private static final VoxelShape[] MIN_SHAPES = new VoxelShape[EnumUtils.DIRECTIONS.length];

   public BlockIndustrialAlarm() {
      super(MekanismBlockTypes.INDUSTRIAL_ALARM, Properties.m_284310_().m_60913_(2.0F, 2.4F).m_284180_(MapColor.f_283913_));
   }

   @Deprecated
   @NotNull
   @Override
   public BlockState m_7417_(
      BlockState state,
      @NotNull Direction facing,
      @NotNull BlockState facingState,
      @NotNull LevelAccessor world,
      @NotNull BlockPos currentPos,
      @NotNull BlockPos facingPos
   ) {
      return facing.m_122424_() == Attribute.getFacing(state) && !state.m_60710_(world, currentPos)
         ? Blocks.f_50016_.m_49966_()
         : super.m_7417_(state, facing, facingState, world, currentPos, facingPos);
   }

   @Deprecated
   public boolean m_7898_(@NotNull BlockState state, @NotNull LevelReader world, @NotNull BlockPos pos) {
      Direction side = Attribute.getFacing(state);
      Direction sideOn = side.m_122424_();
      BlockPos offsetPos = pos.m_121945_(sideOn);
      VoxelShape projected = world.m_8055_(offsetPos).m_60816_(world, offsetPos).m_83263_(side);
      return !state.m_204336_(BlockTags.f_13035_) && !Shapes.m_83157_(projected, MIN_SHAPES[sideOn.ordinal()], BooleanOp.f_82683_);
   }

   static {
      VoxelShapeUtils.setShape(m_49796_(5.0, 0.0, 5.0, 11.0, 16.0, 11.0), MIN_SHAPES, true);
   }
}
