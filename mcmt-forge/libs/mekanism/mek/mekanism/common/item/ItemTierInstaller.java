package mekanism.common.item;

import mekanism.api.text.TextComponentUtil;
import mekanism.api.tier.BaseTier;
import mekanism.common.Mekanism;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeUpgradeable;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITierUpgradable;
import mekanism.common.tile.interfaces.ITileDirectional;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemTierInstaller extends Item {
   @Nullable
   private final BaseTier fromTier;
   @NotNull
   private final BaseTier toTier;

   public ItemTierInstaller(@Nullable BaseTier fromTier, @NotNull BaseTier toTier, Properties properties) {
      super(properties);
      this.fromTier = fromTier;
      this.toTier = toTier;
   }

   @Nullable
   public BaseTier getFromTier() {
      return this.fromTier;
   }

   @NotNull
   public BaseTier getToTier() {
      return this.toTier;
   }

   @NotNull
   public Component m_7626_(@NotNull ItemStack stack) {
      return TextComponentUtil.build(this.toTier.getColor(), super.m_7626_(stack));
   }

   @NotNull
   public InteractionResult m_6225_(UseOnContext context) {
      Player player = context.m_43723_();
      Level world = context.m_43725_();
      if (!world.f_46443_ && player != null) {
         BlockPos pos = context.m_8083_();
         BlockState state = world.m_8055_(pos);
         Block block = state.m_60734_();
         AttributeUpgradeable upgradeableBlock = Attribute.get(block, AttributeUpgradeable.class);
         if (upgradeableBlock != null) {
            BaseTier baseTier = Attribute.getBaseTier(block);
            if (baseTier == this.fromTier && baseTier != this.toTier) {
               BlockState upgradeState = upgradeableBlock.upgradeResult(state, this.toTier);
               if (state == upgradeState) {
                  return InteractionResult.PASS;
               }

               BlockEntity tile = WorldUtils.getTileEntity(world, pos);
               if (tile instanceof ITierUpgradable tierUpgradable) {
                  if (tile instanceof TileEntityMekanism tileMek && !tileMek.playersUsing.isEmpty()) {
                     return InteractionResult.FAIL;
                  }

                  IUpgradeData upgradeData = tierUpgradable.getUpgradeData();
                  if (upgradeData != null) {
                     world.m_46597_(pos, upgradeState);
                     TileEntityMekanism upgradedTile = WorldUtils.getTileEntity(TileEntityMekanism.class, world, pos);
                     if (upgradedTile == null) {
                        Mekanism.logger.warn("Error upgrading block at position: {} in {}.", pos, world);
                        return InteractionResult.FAIL;
                     }

                     if (tile instanceof ITileDirectional directional && directional.isDirectional()) {
                        upgradedTile.setFacing(directional.getDirection());
                     }

                     upgradedTile.parseUpgradeData(upgradeData);
                     upgradedTile.sendUpdatePacket();
                     upgradedTile.m_6596_();
                     if (!player.m_7500_()) {
                        context.m_43722_().m_41774_(1);
                     }

                     return InteractionResult.m_19078_(world.f_46443_);
                  }

                  if (tierUpgradable.canBeUpgraded()) {
                     Mekanism.logger
                        .warn(
                           "Got no upgrade data for block {} at position: {} in {} but it said it would be able to provide some.",
                           new Object[]{block, pos, world}
                        );
                     return InteractionResult.FAIL;
                  }
               }
            }
         }

         return InteractionResult.PASS;
      } else {
         return InteractionResult.PASS;
      }
   }
}
