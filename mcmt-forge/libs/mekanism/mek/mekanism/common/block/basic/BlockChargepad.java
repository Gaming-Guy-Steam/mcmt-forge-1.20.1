package mekanism.common.block.basic;

import java.util.function.UnaryOperator;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.registries.MekanismBlockTypes;
import mekanism.common.tile.TileEntityChargepad;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class BlockChargepad extends BlockTile.BlockTileModel<TileEntityChargepad, BlockTypeTile<TileEntityChargepad>> {
   private static final VoxelShape BASE = m_49796_(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);

   public BlockChargepad() {
      super(MekanismBlockTypes.CHARGEPAD, (UnaryOperator<Properties>)(properties -> properties.m_284180_(MapColor.f_283818_)));
   }

   @Deprecated
   @NotNull
   public VoxelShape m_5939_(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
      return context instanceof EntityCollisionContext entityCollisionContext && entityCollisionContext.m_193113_() instanceof Projectile
         ? super.m_5939_(state, level, pos, context)
         : BASE;
   }
}
