package mekanism.common.tile.base;

import java.util.Objects;
import mekanism.api.Chunk3D;
import mekanism.api.Coord4D;
import mekanism.common.Mekanism;
import mekanism.common.network.to_client.PacketUpdateTile;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.interfaces.ITileWrapper;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TileEntityUpdateable extends BlockEntity implements ITileWrapper {
   @Nullable
   private Coord4D cachedCoord;
   private boolean cacheCoord;
   private long lastSave;

   public TileEntityUpdateable(TileEntityTypeRegistryObject<?> type, BlockPos pos, BlockState state) {
      super(type.get(), pos, state);
   }

   protected void cacheCoord() {
      this.cacheCoord = true;
      this.updateCoord();
   }

   @NotNull
   protected Level getWorldNN() {
      return Objects.requireNonNull(this.m_58904_(), "getWorldNN called before world set");
   }

   public boolean isRemote() {
      return this.getWorldNN().m_5776_();
   }

   public void blockRemoved() {
   }

   public void markDirtyComparator() {
   }

   public final void m_6596_() {
      this.setChanged(true);
   }

   public final void markForSave() {
      this.setChanged(false);
   }

   protected void setChanged(boolean updateComparator) {
      if (this.f_58857_ != null) {
         long time = this.f_58857_.m_46467_();
         if (this.lastSave != time) {
            WorldUtils.markChunkDirty(this.f_58857_, this.f_58858_);
            this.lastSave = time;
         }

         if (updateComparator && !this.isRemote()) {
            this.markDirtyComparator();
         }
      }
   }

   @Nullable
   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.m_195640_(this);
   }

   public void handleUpdateTag(@NotNull CompoundTag tag) {
      super.m_142466_(tag);
   }

   @NotNull
   public CompoundTag m_5995_() {
      return this.getReducedUpdateTag();
   }

   @NotNull
   public CompoundTag getReducedUpdateTag() {
      return super.m_5995_();
   }

   public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
      if (this.isRemote() && net.getDirection() == PacketFlow.CLIENTBOUND) {
         CompoundTag tag = pkt.m_131708_();
         if (tag != null) {
            this.handleUpdatePacket(tag);
         }
      }
   }

   public void handleUpdatePacket(@NotNull CompoundTag tag) {
      this.handleUpdateTag(tag);
   }

   public void sendUpdatePacket() {
      this.sendUpdatePacket(this);
   }

   public void sendUpdatePacket(BlockEntity tracking) {
      if (this.isRemote()) {
         Mekanism.logger.warn("Update packet call requested from client side", new IllegalStateException());
      } else if (this.m_58901_()) {
         Mekanism.logger.warn("Update packet call requested for removed tile", new IllegalStateException());
      } else {
         Mekanism.packetHandler().sendToAllTracking(new PacketUpdateTile(this), tracking);
      }
   }

   protected void updateModelData() {
      this.requestModelDataUpdate();
      WorldUtils.updateBlock(this.m_58904_(), this.m_58899_(), this.m_58900_());
   }

   @Override
   public Level getTileWorld() {
      return this.f_58857_;
   }

   @Override
   public BlockPos getTilePos() {
      return this.f_58858_;
   }

   public void m_142466_(@NotNull CompoundTag nbt) {
      super.m_142466_(nbt);
      this.updateCoord();
   }

   public void m_142339_(@NotNull Level world) {
      super.m_142339_(world);
      this.updateCoord();
   }

   private void updateCoord() {
      if (this.cacheCoord && this.f_58857_ != null) {
         this.cachedCoord = new Coord4D(this.f_58858_, this.f_58857_);
      }
   }

   @Override
   public Coord4D getTileCoord() {
      return this.cacheCoord && this.cachedCoord != null ? this.cachedCoord : ITileWrapper.super.getTileCoord();
   }

   @Override
   public Chunk3D getTileChunk() {
      if (this.cacheCoord && this.cachedCoord != null) {
         return new Chunk3D(this.cachedCoord);
      } else {
         BlockPos pos = this.getTilePos();
         return new Chunk3D(this.getTileWorld().m_46472_(), SectionPos.m_123171_(pos.m_123341_()), SectionPos.m_123171_(pos.m_123343_()));
      }
   }
}
