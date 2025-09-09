package mekanism.common.tile.prefab;

import java.util.Objects;
import java.util.UUID;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.lib.multiblock.IInternalMultiblock;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityInternalMultiblock extends TileEntityMekanism implements IInternalMultiblock {
   @Nullable
   private MultiblockData multiblock;
   private UUID multiblockUUID;

   public TileEntityInternalMultiblock(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
      super(blockProvider, pos, state);
   }

   @Override
   public void setMultiblock(@Nullable MultiblockData multiblock) {
      this.multiblock = multiblock;
      this.setMultiblock(multiblock == null ? null : multiblock.inventoryID);
   }

   private void setMultiblock(UUID id) {
      UUID old = this.multiblockUUID;
      this.multiblockUUID = id;
      if (!Objects.equals(old, id)) {
         this.multiblockChanged(old);
      }
   }

   protected void multiblockChanged(@Nullable UUID old) {
      if (!this.isRemote()) {
         this.sendUpdatePacket();
      }
   }

   @Nullable
   @Override
   public UUID getMultiblockUUID() {
      return this.multiblockUUID;
   }

   @Nullable
   @Override
   public MultiblockData getMultiblock() {
      return this.multiblock;
   }

   @Override
   public void onNeighborChange(Block block, BlockPos neighborPos) {
      super.onNeighborChange(block, neighborPos);
      if (!this.isRemote() && this.multiblock != null && (this.f_58857_.m_46859_(neighborPos) || !this.multiblock.isKnownLocation(neighborPos))) {
         this.multiblock.recheckStructure = true;
      }
   }

   @Override
   public void blockRemoved() {
      super.blockRemoved();
      if (!this.isRemote() && this.hasFormedMultiblock() && this.multiblock != null) {
         this.multiblock.recheckStructure = true;
      }
   }

   @NotNull
   @Override
   public CompoundTag getReducedUpdateTag() {
      CompoundTag updateTag = super.getReducedUpdateTag();
      if (this.multiblockUUID != null) {
         updateTag.m_128362_("inventoryID", this.multiblockUUID);
      }

      return updateTag;
   }

   @Override
   public void handleUpdateTag(@NotNull CompoundTag tag) {
      super.handleUpdateTag(tag);
      NBTUtils.setUUIDIfPresentElse(tag, "inventoryID", this::setMultiblock, () -> this.multiblockUUID = null);
   }
}
