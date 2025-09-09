package mekanism.common.lib.transmitter;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import mekanism.common.content.network.transmitter.BufferedTransmitter;
import mekanism.common.lib.math.Range3D;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class DynamicBufferedNetwork<ACCEPTOR, NETWORK extends DynamicBufferedNetwork<ACCEPTOR, NETWORK, BUFFER, TRANSMITTER>, BUFFER, TRANSMITTER extends BufferedTransmitter<ACCEPTOR, NETWORK, BUFFER, TRANSMITTER>>
   extends DynamicNetwork<ACCEPTOR, NETWORK, TRANSMITTER> {
   protected final LongSet chunks = new LongOpenHashSet();
   @Nullable
   protected Range3D packetRange;
   protected long capacity;
   protected boolean needsUpdate;
   private boolean forceScaleUpdate;
   private long lastSaveShareWriteTime;
   private long lastMarkDirtyTime;
   public float currentScale;

   protected DynamicBufferedNetwork(UUID networkID) {
      super(networkID);
   }

   protected abstract float computeContentScale();

   @Override
   public void onUpdate() {
      super.onUpdate();
      float scale = this.computeContentScale();
      if (scale != this.currentScale) {
         this.currentScale = scale;
         this.needsUpdate = true;
      }
   }

   @Override
   public void addNewTransmitters(Collection<TRANSMITTER> newTransmitters, CompatibleTransmitterValidator<ACCEPTOR, NETWORK, TRANSMITTER> transmitterValidator) {
      super.addNewTransmitters(newTransmitters, transmitterValidator);
      if (!this.forceScaleUpdate) {
         this.forceScaleUpdate = this.isEmpty();
      }
   }

   protected void addTransmitterFromCommit(TRANSMITTER transmitter) {
      super.addTransmitterFromCommit(transmitter);
      this.chunks.add(ChunkPos.m_151388_(transmitter.getTilePos()));
      this.updateCapacity(transmitter);
      this.absorbBuffer(transmitter);
   }

   @Override
   protected void validTransmittersAdded() {
      super.validTransmittersAdded();
      this.clampBuffer();
      if (this.forceScaleUpdate) {
         this.forceScaleUpdate = false;
         this.forceScaleUpdate();
      }

      this.needsUpdate = true;
      this.packetRange = null;
   }

   public List<TRANSMITTER> adoptTransmittersAndAcceptorsFrom(NETWORK net) {
      List<TRANSMITTER> transmittersToUpdate = super.adoptTransmittersAndAcceptorsFrom(net);
      this.chunks.addAll(net.chunks);
      this.updateCapacity();
      return transmittersToUpdate;
   }

   protected void removeInvalid(@Nullable TRANSMITTER triggerTransmitter) {
      super.removeInvalid(triggerTransmitter);
      this.clampBuffer();
      this.updateSaveShares(triggerTransmitter);
   }

   @Override
   public void deregister() {
      super.deregister();
      this.chunks.clear();
      this.packetRange = null;
   }

   protected abstract void forceScaleUpdate();

   @NotNull
   public abstract BUFFER getBuffer();

   public abstract void absorbBuffer(TRANSMITTER transmitter);

   public abstract void clampBuffer();

   public boolean isCompatibleWith(NETWORK other) {
      return true;
   }

   protected synchronized void updateCapacity(TRANSMITTER transmitter) {
      long transmitterCapacity = transmitter.getCapacity();
      if (transmitterCapacity > Long.MAX_VALUE - this.capacity) {
         this.capacity = Long.MAX_VALUE;
      } else {
         this.capacity += transmitterCapacity;
      }
   }

   public synchronized void updateCapacity() {
      long sum = 0L;

      for (TRANSMITTER transmitter : this.transmitters) {
         long transmitterCapacity = transmitter.getCapacity();
         if (transmitterCapacity > Long.MAX_VALUE - this.capacity) {
            sum = Long.MAX_VALUE;
            break;
         }

         sum += transmitterCapacity;
      }

      if (this.capacity != sum) {
         this.capacity = sum;
      }
   }

   public long getCapacity() {
      return this.capacity;
   }

   @Override
   public Object getNetworkReaderCapacity() {
      return this.getCapacity();
   }

   protected void updateSaveShares(@Nullable TRANSMITTER triggerTransmitter) {
   }

   public final void validateSaveShares(@NotNull TRANSMITTER triggerTransmitter) {
      if (this.world == null) {
         this.world = triggerTransmitter.getTileWorld();
      }

      if (this.world != null && this.world.m_46467_() != this.lastSaveShareWriteTime) {
         this.lastSaveShareWriteTime = this.world.m_46467_();
         this.updateSaveShares(triggerTransmitter);
      }
   }

   public void markDirty() {
      if (this.world != null && !this.world.f_46443_ && this.world.m_46467_() != this.lastMarkDirtyTime) {
         this.lastMarkDirtyTime = this.world.m_46467_();
         this.chunks.forEach(chunk -> WorldUtils.markChunkDirty(this.world, WorldUtils.getBlockPosFromChunkPos(chunk)));
      }
   }

   public Range3D getPacketRange() {
      if (this.packetRange == null) {
         this.packetRange = this.genPacketRange();
      }

      return this.packetRange;
   }

   private Range3D genPacketRange() {
      if (this.isEmpty()) {
         this.deregister();
         return null;
      } else {
         boolean initialized = false;
         int minX = 0;
         int minZ = 0;
         int maxX = 0;
         int maxZ = 0;

         for (TRANSMITTER transmitter : this.transmitters) {
            BlockPos pos = transmitter.getTilePos();
            if (initialized) {
               if (pos.m_123341_() < minX) {
                  minX = pos.m_123341_();
               } else if (pos.m_123341_() > maxX) {
                  maxX = pos.m_123341_();
               }

               if (pos.m_123343_() < minZ) {
                  minZ = pos.m_123343_();
               } else if (pos.m_123343_() > maxZ) {
                  maxZ = pos.m_123343_();
               }
            } else {
               minX = pos.m_123341_();
               minZ = pos.m_123343_();
               maxX = minX;
               maxZ = minZ;
               initialized = true;
            }
         }

         return new Range3D(minX, minZ, maxX, maxZ, this.world.m_46472_());
      }
   }

   public static class TransferEvent<NETWORK extends DynamicBufferedNetwork<?, NETWORK, ?, ?>> extends Event {
      public final NETWORK network;

      public TransferEvent(NETWORK network) {
         this.network = network;
      }
   }
}
