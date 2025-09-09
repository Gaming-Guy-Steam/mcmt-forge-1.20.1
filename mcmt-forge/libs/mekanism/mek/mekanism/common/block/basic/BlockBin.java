package mekanism.common.block.basic;

import java.util.function.UnaryOperator;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.inventory.slot.BinInventorySlot;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.jetbrains.annotations.NotNull;

public class BlockBin extends BlockTile<TileEntityBin, BlockTypeTile<TileEntityBin>> {
   public BlockBin(BlockTypeTile<TileEntityBin> type, UnaryOperator<Properties> propertiesModifier) {
      super(type, propertiesModifier);
   }

   @Deprecated
   public void m_6256_(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Player player) {
      if (!world.f_46443_) {
         TileEntityBin bin = WorldUtils.getTileEntity(TileEntityBin.class, world, pos);
         if (bin != null) {
            BlockHitResult mop = MekanismUtils.rayTrace(player);
            if (mop.m_6662_() != Type.MISS && mop.m_82434_() == bin.getDirection()) {
               BinInventorySlot binSlot = bin.getBinSlot();
               if (!binSlot.isEmpty() && bin.removeTicks == 0) {
                  bin.removeTicks = 3;
                  ItemStack stack;
                  if (player.m_6144_()) {
                     stack = binSlot.getStack().m_255036_(1);
                     MekanismUtils.logMismatchedStackSize(binSlot.shrinkStack(1, Action.EXECUTE), 1L);
                  } else {
                     stack = binSlot.getBottomStack();
                     if (!stack.m_41619_()) {
                        MekanismUtils.logMismatchedStackSize(binSlot.shrinkStack(stack.m_41613_(), Action.EXECUTE), stack.m_41613_());
                     }
                  }

                  if (!player.m_150109_().m_36054_(stack)) {
                     BlockPos dropPos = pos.m_121945_(bin.getDirection());
                     Entity item = new ItemEntity(world, dropPos.m_123341_() + 0.5F, dropPos.m_123342_() + 0.3F, dropPos.m_123343_() + 0.5F, stack);
                     Vec3 motion = item.m_20184_();
                     item.m_5997_(-motion.m_7096_(), -motion.m_7098_(), -motion.m_7094_());
                     world.m_7967_(item);
                  } else {
                     world.m_6263_(
                        null,
                        pos.m_123341_() + 0.5F,
                        pos.m_123342_() + 0.5F,
                        pos.m_123343_() + 0.5F,
                        SoundEvents.f_12019_,
                        SoundSource.PLAYERS,
                        0.2F,
                        ((world.f_46441_.m_188501_() - world.f_46441_.m_188501_()) * 0.7F + 1.0F) * 2.0F
                     );
                  }
               }
            }
         }
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
      TileEntityBin bin = WorldUtils.getTileEntity(TileEntityBin.class, world, pos);
      if (bin == null) {
         return InteractionResult.PASS;
      } else if (bin.tryWrench(state, player, hand, hit) != WrenchResult.PASS) {
         return InteractionResult.SUCCESS;
      } else {
         ItemStack stack = player.m_21120_(hand);
         if (stack.m_41619_() && player.m_6144_() && hit.m_82434_() == bin.getDirection()) {
            return bin.toggleLock() ? InteractionResult.m_19078_(world.f_46443_) : InteractionResult.FAIL;
         } else {
            if (!world.f_46443_) {
               BinInventorySlot binSlot = bin.getBinSlot();
               int binMaxSize = binSlot.getLimit(binSlot.getStack());
               if (binSlot.getCount() < binMaxSize) {
                  if (bin.addTicks == 0) {
                     if (!stack.m_41619_()) {
                        ItemStack remain = binSlot.insertItem(stack, Action.EXECUTE, AutomationType.MANUAL);
                        player.m_21008_(hand, remain);
                        bin.addTicks = 5;
                     }
                  } else if (bin.addTicks > 0 && bin.getItemCount() > 0) {
                     NonNullList<ItemStack> inv = player.m_150109_().f_35974_;

                     for (int i = 0; i < inv.size() && binSlot.getCount() != binMaxSize; i++) {
                        ItemStack stackToAdd = (ItemStack)inv.get(i);
                        if (!stackToAdd.m_41619_()) {
                           ItemStack remain = binSlot.insertItem(stackToAdd, Action.EXECUTE, AutomationType.MANUAL);
                           inv.set(i, remain);
                           bin.addTicks = 5;
                        }

                        player.f_36096_.m_150429_();
                     }
                  }
               }
            }

            return InteractionResult.m_19078_(world.f_46443_);
         }
      }
   }
}
