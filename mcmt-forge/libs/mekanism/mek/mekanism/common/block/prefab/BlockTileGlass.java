package mekanism.common.block.prefab;

import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class BlockTileGlass<TILE extends TileEntityMekanism, TYPE extends BlockTypeTile<TILE>> extends BlockTile<TILE, TYPE> {
   public BlockTileGlass(TYPE type) {
      super(
         type,
         Properties.m_284310_()
            .m_60918_(SoundType.f_56744_)
            .m_60913_(3.5F, 9.6F)
            .m_60955_()
            .m_60999_()
            .m_60960_(BlockStateHelper.NEVER_PREDICATE)
            .m_60971_(BlockStateHelper.NEVER_PREDICATE)
            .m_280658_(NoteBlockInstrument.HAT)
      );
   }

   public boolean shouldDisplayFluidOverlay(BlockState state, BlockAndTintGetter world, BlockPos pos, FluidState fluidState) {
      return true;
   }

   @Deprecated
   public boolean m_6104_(@NotNull BlockState state, @NotNull BlockState adjacentBlockState, @NotNull Direction side) {
      return adjacentBlockState.m_60734_() instanceof BlockTileGlass;
   }

   @Deprecated
   public float m_7749_(@NotNull BlockState state, @NotNull BlockGetter worldIn, @NotNull BlockPos pos) {
      return 1.0F;
   }

   public boolean m_7420_(@NotNull BlockState state, @NotNull BlockGetter reader, @NotNull BlockPos pos) {
      return true;
   }

   @Deprecated
   @NotNull
   public VoxelShape m_5909_(@NotNull BlockState state, @NotNull BlockGetter reader, @NotNull BlockPos pos, @NotNull CollisionContext ctx) {
      return Shapes.m_83040_();
   }
}
