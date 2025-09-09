package mekanism.common.block.basic;

import java.util.function.UnaryOperator;
import mekanism.api.security.ISecurityUtils;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.resource.BlockResourceInfo;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.FluidUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class BlockFluidTank extends BlockTile.BlockTileModel<TileEntityFluidTank, Machine<TileEntityFluidTank>> {
   public BlockFluidTank(Machine<TileEntityFluidTank> type) {
      super(type, (UnaryOperator<Properties>)(properties -> properties.m_284180_(BlockResourceInfo.STEEL.getMapColor())));
   }

   public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
      int ambientLight = super.getLightEmission(state, world, pos);
      if (ambientLight == 15) {
         return ambientLight;
      } else {
         TileEntityFluidTank tile = WorldUtils.getTileEntity(TileEntityFluidTank.class, world, pos);
         if (tile != null) {
            FluidStack fluid = tile.fluidTank.getFluid();
            if (!fluid.isEmpty()) {
               ambientLight = Math.max(ambientLight, fluid.getFluid().getFluidType().getLightLevel(fluid));
            }
         }

         return ambientLight;
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
      TileEntityFluidTank tile = WorldUtils.getTileEntity(TileEntityFluidTank.class, world, pos, true);
      if (tile == null) {
         return InteractionResult.PASS;
      } else if (world.f_46443_) {
         return this.genericClientActivated(player, hand);
      } else if (tile.tryWrench(state, player, hand, hit) != WrenchResult.PASS) {
         return InteractionResult.SUCCESS;
      } else {
         if (!player.m_6144_()) {
            if (!ISecurityUtils.INSTANCE.canAccessOrDisplayError(player, tile)) {
               return InteractionResult.FAIL;
            }

            ItemStack stack = player.m_21120_(hand);
            if (!stack.m_41619_() && FluidUtils.handleTankInteraction(player, hand, stack, tile.fluidTank)) {
               player.m_150109_().m_6596_();
               return InteractionResult.SUCCESS;
            }
         }

         return tile.openGui(player);
      }
   }
}
