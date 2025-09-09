package mekanism.common.item.block;

import java.util.List;
import mekanism.api.security.ISecurityUtils;
import mekanism.api.text.EnumColor;
import mekanism.common.CommonWorldTickHandler;
import mekanism.common.MekanismLang;
import mekanism.common.block.BlockCardboardBox;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tile.TileEntityCardboardBox;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.RegistryUtils;
import mekanism.common.util.WorldUtils;
import mekanism.common.util.text.BooleanStateDisplay;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemBlockCardboardBox extends ItemBlockMekanism<BlockCardboardBox> {
   public ItemBlockCardboardBox(BlockCardboardBox block) {
      super(block, new Properties().m_41487_(16));
   }

   public void m_7373_(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      tooltip.add(
         MekanismLang.BLOCK_DATA.translateColored(EnumColor.INDIGO, new Object[]{BooleanStateDisplay.YesNo.of(this.getBlockData(world, stack) != null)})
      );
      BlockCardboardBox.BlockData data = this.getBlockData(world, stack);
      if (data != null) {
         try {
            tooltip.add(MekanismLang.BLOCK.translate(new Object[]{data.blockState.m_60734_()}));
            if (data.tileTag != null) {
               tooltip.add(MekanismLang.BLOCK_ENTITY.translate(new Object[]{data.tileTag.m_128461_("id")}));
            }
         } catch (Exception var7) {
         }
      }
   }

   private static boolean canReplace(Level world, Player player, BlockPos pos, Direction sideClicked, BlockState state, ItemStack stack) {
      return world.m_7966_(player, pos)
         && player.m_36204_(pos.m_121945_(sideClicked), sideClicked, stack)
         && !MinecraftForge.EVENT_BUS.post(new BreakEvent(world, pos, state, player));
   }

   @NotNull
   public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
      Player player = context.m_43723_();
      if (!stack.m_41619_() && player != null) {
         Level world = context.m_43725_();
         BlockPos pos = context.m_8083_();
         if (this.getBlockData(world, stack) == null && !player.m_6144_()) {
            BlockState state = world.m_8055_(pos);
            if (!state.m_60795_() && state.m_60800_(world, pos) != -1.0F) {
               if (!state.m_204336_(MekanismTags.Blocks.CARDBOARD_BLACKLIST)
                  && !MekanismConfig.general.cardboardModBlacklist.get().contains(RegistryUtils.getNamespace(state.m_60734_()))
                  && canReplace(world, player, pos, context.m_43719_(), state, stack)) {
                  BlockEntity tile = WorldUtils.getTileEntity(world, pos);
                  if (tile == null
                     || !MekanismTags.TileEntityTypes.CARDBOARD_BLACKLIST_LOOKUP.contains(tile.m_58903_())
                        && ISecurityUtils.INSTANCE.canAccessOrDisplayError(player, tile)) {
                     if (!world.f_46443_) {
                        BlockCardboardBox.BlockData data = new BlockCardboardBox.BlockData(state);
                        if (tile != null) {
                           data.tileTag = tile.m_187480_();
                        }

                        if (!player.m_7500_()) {
                           stack.m_41774_(1);
                        }

                        CommonWorldTickHandler.monitoringCardboardBox = true;
                        world.m_46597_(pos, (BlockState)this.m_40614_().m_49966_().m_61124_(BlockStateHelper.storageProperty, true));
                        CommonWorldTickHandler.monitoringCardboardBox = false;
                        TileEntityCardboardBox box = WorldUtils.getTileEntity(TileEntityCardboardBox.class, world, pos);
                        if (box != null) {
                           box.storedData = data;
                        }
                     }

                     return InteractionResult.SUCCESS;
                  }

                  return InteractionResult.FAIL;
               }

               return InteractionResult.FAIL;
            }
         }

         return InteractionResult.PASS;
      } else {
         return InteractionResult.PASS;
      }
   }

   public boolean m_7429_(@NotNull BlockPlaceContext context, @NotNull BlockState state) {
      Level world = context.m_43725_();
      if (world.f_46443_) {
         return true;
      } else if (super.m_7429_(context, state)) {
         TileEntityCardboardBox tile = WorldUtils.getTileEntity(TileEntityCardboardBox.class, world, context.m_8083_());
         if (tile != null) {
            tile.storedData = this.getBlockData(world, context.m_43722_());
         }

         return true;
      } else {
         return false;
      }
   }

   public void setBlockData(ItemStack stack, BlockCardboardBox.BlockData data) {
      ItemDataUtils.setCompound(stack, "data", data.write(new CompoundTag()));
   }

   public BlockCardboardBox.BlockData getBlockData(@Nullable Level level, ItemStack stack) {
      return ItemDataUtils.hasData(stack, "data", 10) ? BlockCardboardBox.BlockData.read(level, ItemDataUtils.getCompound(stack, "data")) : null;
   }

   public int getMaxStackSize(ItemStack stack) {
      BlockCardboardBox.BlockData blockData = this.getBlockData(null, stack);
      return blockData == null ? super.getMaxStackSize(stack) : 1;
   }
}
