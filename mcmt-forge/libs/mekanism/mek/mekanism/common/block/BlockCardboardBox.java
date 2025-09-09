package mekanism.common.block;

import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.states.IStateStorage;
import mekanism.common.item.block.ItemBlockCardboardBox;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tile.TileEntityCardboardBox;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockCardboardBox extends BlockMekanism implements IStateStorage, IHasTileEntity<TileEntityCardboardBox> {
   public BlockCardboardBox() {
      super(Properties.m_284310_().m_60913_(0.5F, 0.6F).m_284180_(MapColor.f_283825_));
   }

   @Deprecated
   @NotNull
   public InteractionResult m_6227_(
      @NotNull BlockState state,
      @NotNull Level world,
      @NotNull BlockPos pos,
      @NotNull Player player,
      @NotNull InteractionHand hand,
      @NotNull BlockHitResult hit
   ) {
      if (!player.m_6144_()) {
         return InteractionResult.PASS;
      } else if (!canReplace(world, player, pos, state)) {
         return InteractionResult.FAIL;
      } else {
         if (!world.f_46443_) {
            TileEntityCardboardBox box = WorldUtils.getTileEntity(TileEntityCardboardBox.class, world, pos);
            if (box != null && box.storedData != null) {
               BlockCardboardBox.BlockData data = box.storedData;
               BlockState adjustedState = Block.m_49931_(data.blockState, world, pos);
               world.m_46597_(pos, adjustedState);
               if (data.tileTag != null) {
                  data.updateLocation(pos);
                  BlockEntity tile = WorldUtils.getTileEntity(world, pos);
                  if (tile != null) {
                     tile.m_142466_(data.tileTag);
                  }
               }

               m_49840_(world, pos, MekanismBlocks.CARDBOARD_BOX.getItemStack());
               MekanismCriteriaTriggers.UNBOX_CARDBOARD_BOX.trigger((ServerPlayer)player);
            }
         }

         return InteractionResult.m_19078_(world.f_46443_);
      }
   }

   private static boolean canReplace(Level world, Player player, BlockPos pos, BlockState state) {
      return world.m_7966_(player, pos) && !MinecraftForge.EVENT_BUS.post(new BreakEvent(world, pos, state, player));
   }

   @NotNull
   @Override
   public ItemStack getCloneItemStack(@NotNull BlockState state, HitResult target, @NotNull BlockGetter world, @NotNull BlockPos pos, Player player) {
      ItemStack itemStack = new ItemStack(this);
      TileEntityCardboardBox tile = WorldUtils.getTileEntity(TileEntityCardboardBox.class, world, pos);
      if (tile == null) {
         return itemStack;
      } else {
         if (tile.storedData != null) {
            ((ItemBlockCardboardBox)itemStack.m_41720_()).setBlockData(itemStack, tile.storedData);
         }

         return itemStack;
      }
   }

   @Override
   public TileEntityTypeRegistryObject<TileEntityCardboardBox> getTileType() {
      return MekanismTileEntityTypes.CARDBOARD_BOX;
   }

   public static class BlockData {
      @NotNull
      public final BlockState blockState;
      @Nullable
      public CompoundTag tileTag;

      public BlockData(@NotNull BlockState blockState) {
         this.blockState = blockState;
      }

      public static BlockCardboardBox.BlockData read(@Nullable Level level, CompoundTag nbtTags) {
         HolderGetter<Block> holderGetter = (HolderGetter<Block>)(level == null
            ? BuiltInRegistries.f_256975_.m_255303_()
            : level.m_246945_(Registries.f_256747_));
         BlockCardboardBox.BlockData data = new BlockCardboardBox.BlockData(NbtUtils.m_247651_(holderGetter, nbtTags.m_128469_("blockState")));
         NBTUtils.setCompoundIfPresent(nbtTags, "tileTag", nbt -> data.tileTag = nbt);
         return data;
      }

      public void updateLocation(BlockPos pos) {
         if (this.tileTag != null) {
            this.tileTag.m_128405_("x", pos.m_123341_());
            this.tileTag.m_128405_("y", pos.m_123342_());
            this.tileTag.m_128405_("z", pos.m_123343_());
         }
      }

      public CompoundTag write(CompoundTag nbtTags) {
         nbtTags.m_128365_("blockState", NbtUtils.m_129202_(this.blockState));
         if (this.tileTag != null) {
            nbtTags.m_128365_("tileTag", this.tileTag);
         }

         return nbtTags;
      }
   }
}
