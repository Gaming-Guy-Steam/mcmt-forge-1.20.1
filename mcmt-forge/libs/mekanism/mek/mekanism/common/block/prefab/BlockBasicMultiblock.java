package mekanism.common.block.prefab;

import java.util.function.UnaryOperator;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class BlockBasicMultiblock<TILE extends TileEntityMekanism> extends BlockTile<TILE, BlockTypeTile<TILE>> {
   public BlockBasicMultiblock(BlockTypeTile<TILE> type, UnaryOperator<Properties> propertiesModifier) {
      this(type, propertiesModifier.apply(Properties.m_284310_().m_60913_(5.0F, 9.0F).m_60999_()));
   }

   public BlockBasicMultiblock(BlockTypeTile<TILE> type, Properties properties) {
      super(type, properties);
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
      TileEntityMultiblock<?> tile = WorldUtils.getTileEntity(TileEntityMultiblock.class, world, pos);
      if (tile == null) {
         return InteractionResult.PASS;
      } else if (!world.f_46443_) {
         return tile.tryWrench(state, player, hand, hit) != WrenchResult.PASS
            ? InteractionResult.SUCCESS
            : tile.onActivate(player, hand, player.m_21120_(hand));
      } else {
         return MekanismUtils.canUseAsWrench(player.m_21120_(hand)) || tile.hasGui() && tile.getMultiblock().isFormed()
            ? InteractionResult.SUCCESS
            : InteractionResult.PASS;
      }
   }
}
