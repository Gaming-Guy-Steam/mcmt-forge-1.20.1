package mekanism.common.block.basic;

import java.util.function.UnaryOperator;
import mekanism.api.security.ISecurityUtils;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.registries.MekanismBlockTypes;
import mekanism.common.resource.BlockResourceInfo;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockLogisticalSorter extends BlockTile.BlockTileModel<TileEntityLogisticalSorter, Machine<TileEntityLogisticalSorter>> {
   public BlockLogisticalSorter() {
      super(MekanismBlockTypes.LOGISTICAL_SORTER, (UnaryOperator<Properties>)(properties -> properties.m_284180_(BlockResourceInfo.STEEL.getMapColor())));
   }

   @Nullable
   @Override
   public BlockState m_5573_(@NotNull BlockPlaceContext context) {
      BlockState state = super.m_5573_(context);
      Direction facing = Attribute.getFacing(state);
      if (facing == null) {
         return state;
      } else {
         Direction oppositeDirection = facing.m_122424_();
         Level level = context.m_43725_();
         BlockPos pos = context.m_8083_();
         BlockEntity back = WorldUtils.getTileEntity(level, pos.m_121945_(oppositeDirection));
         if (!InventoryUtils.isItemHandler(back, oppositeDirection)) {
            for (Direction dir : EnumUtils.DIRECTIONS) {
               if (dir != oppositeDirection) {
                  BlockEntity neighbor = WorldUtils.getTileEntity(level, pos.m_121945_(dir));
                  if (InventoryUtils.isItemHandler(neighbor, dir)) {
                     state = Attribute.setFacing(state, dir.m_122424_());
                     break;
                  }
               }
            }
         }

         return state;
      }
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
      TileEntityLogisticalSorter tile = WorldUtils.getTileEntity(TileEntityLogisticalSorter.class, world, pos);
      if (tile == null) {
         return InteractionResult.PASS;
      } else if (world.f_46443_) {
         return this.genericClientActivated(player, hand);
      } else {
         ItemStack stack = player.m_21120_(hand);
         if (!MekanismUtils.canUseAsWrench(stack)) {
            return tile.openGui(player);
         } else if (!ISecurityUtils.INSTANCE.canAccessOrDisplayError(player, tile)) {
            return InteractionResult.FAIL;
         } else if (player.m_6144_()) {
            WorldUtils.dismantleBlock(state, world, pos);
            return InteractionResult.SUCCESS;
         } else {
            Direction change = tile.getDirection().m_122427_();
            if (!tile.hasConnectedInventory()) {
               for (Direction dir : EnumUtils.DIRECTIONS) {
                  BlockEntity tileEntity = WorldUtils.getTileEntity(world, pos.m_121945_(dir));
                  if (InventoryUtils.isItemHandler(tileEntity, dir)) {
                     change = dir.m_122424_();
                     break;
                  }
               }
            }

            tile.setFacing(change);
            world.m_46672_(pos, this);
            return InteractionResult.SUCCESS;
         }
      }
   }

   @Deprecated
   @NotNull
   @Override
   public BlockState m_7417_(
      BlockState state,
      @NotNull Direction dir,
      @NotNull BlockState facingState,
      @NotNull LevelAccessor world,
      @NotNull BlockPos pos,
      @NotNull BlockPos neighborPos
   ) {
      if (!world.m_5776_()) {
         TileEntityLogisticalSorter sorter = WorldUtils.getTileEntity(TileEntityLogisticalSorter.class, world, pos);
         if (sorter != null && !sorter.hasConnectedInventory()) {
            BlockEntity tileEntity = WorldUtils.getTileEntity(world, neighborPos);
            if (InventoryUtils.isItemHandler(tileEntity, dir)) {
               sorter.setFacing(dir.m_122424_());
               state = sorter.m_58900_();
            }
         }
      }

      return super.m_7417_(state, dir, facingState, world, pos, neighborPos);
   }
}
