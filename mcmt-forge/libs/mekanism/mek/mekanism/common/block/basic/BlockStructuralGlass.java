package mekanism.common.block.basic;

import mekanism.common.block.prefab.BlockTileGlass;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.tile.prefab.TileEntityStructuralMultiblock;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class BlockStructuralGlass<TILE extends TileEntityStructuralMultiblock> extends BlockTileGlass<TILE, BlockTypeTile<TILE>> {
   public BlockStructuralGlass(BlockTypeTile<TILE> type) {
      super(type);
   }

   @Deprecated
   @NotNull
   @Override
   public InteractionResult m_6227_(
      @NotNull BlockState state,
      @NotNull Level world,
      @NotNull BlockPos pos,
      @NotNull Player player,
      @NotNull InteractionHand hand,
      @NotNull BlockHitResult hit
   ) {
      TileEntityStructuralMultiblock tile = WorldUtils.getTileEntity(TileEntityStructuralMultiblock.class, world, pos);
      if (tile == null) {
         return InteractionResult.PASS;
      } else if (!world.f_46443_) {
         return tile.tryWrench(state, player, hand, hit) != WrenchResult.PASS
            ? InteractionResult.SUCCESS
            : tile.onActivate(player, hand, player.m_21120_(hand));
      } else {
         return MekanismUtils.canUseAsWrench(player.m_21120_(hand)) || tile.structuralGuiAccessAllowed() && tile.hasFormedMultiblock()
            ? InteractionResult.SUCCESS
            : InteractionResult.PASS;
      }
   }
}
