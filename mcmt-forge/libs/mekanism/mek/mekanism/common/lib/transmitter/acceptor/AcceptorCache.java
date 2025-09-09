package mekanism.common.lib.transmitter.acceptor;

import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.content.network.transmitter.Transmitter;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.CapabilityUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class AcceptorCache<ACCEPTOR> extends AbstractAcceptorCache<ACCEPTOR, AcceptorCache.AcceptorInfo<ACCEPTOR>> {
   public AcceptorCache(Transmitter<ACCEPTOR, ?, ?> transmitter, TileEntityTransmitter transmitterTile) {
      super(transmitter, transmitterTile);
   }

   protected void updateCachedAcceptorAndListen(Direction side, BlockEntity acceptorTile, LazyOptional<ACCEPTOR> acceptor) {
      this.updateCachedAcceptorAndListen(side, acceptorTile, acceptor, acceptor, true);
   }

   protected void updateCachedAcceptorAndListen(
      Direction side, BlockEntity acceptorTile, LazyOptional<ACCEPTOR> acceptor, LazyOptional<?> sourceAcceptor, boolean sourceIsSame
   ) {
      boolean dirtyAcceptor = false;
      if (this.cachedAcceptors.containsKey(side)) {
         AcceptorCache.AcceptorInfo<ACCEPTOR> acceptorInfo = this.cachedAcceptors.get(side);
         if (acceptorTile != acceptorInfo.getTile()) {
            this.cachedAcceptors.put(side, new AcceptorCache.AcceptorInfo<>(acceptorTile, sourceAcceptor, acceptor));
            dirtyAcceptor = true;
         } else if (sourceAcceptor != acceptorInfo.sourceAcceptor) {
            acceptorInfo.updateAcceptor(sourceAcceptor, acceptor);
            dirtyAcceptor = true;
         }
      } else {
         this.cachedAcceptors.put(side, new AcceptorCache.AcceptorInfo<>(acceptorTile, sourceAcceptor, acceptor));
         dirtyAcceptor = true;
      }

      if (dirtyAcceptor) {
         this.transmitter.markDirtyAcceptor(side);
         NonNullConsumer<LazyOptional<ACCEPTOR>> refreshListener = this.getRefreshListener(side);
         if (sourceIsSame) {
            acceptor.addListener(refreshListener);
         } else {
            CapabilityUtils.addListener(sourceAcceptor, refreshListener);
         }
      }
   }

   @Override
   public LazyOptional<ACCEPTOR> getConnectedAcceptor(Direction side) {
      if (this.cachedAcceptors.containsKey(side)) {
         AcceptorCache.AcceptorInfo<ACCEPTOR> acceptorInfo = this.cachedAcceptors.get(side);
         if (!acceptorInfo.getTile().m_58901_()) {
            return acceptorInfo.acceptor;
         }
      }

      return LazyOptional.empty();
   }

   @Nullable
   public BlockEntity getConnectedAcceptorTile(Direction side) {
      if (this.cachedAcceptors.containsKey(side)) {
         BlockEntity tile = this.cachedAcceptors.get(side).getTile();
         if (!tile.m_58901_()) {
            return tile;
         }
      }

      return null;
   }

   public boolean isAcceptorAndListen(@Nullable BlockEntity tile, Direction side, Capability<ACCEPTOR> capability) {
      LazyOptional<ACCEPTOR> acceptor = CapabilityUtils.getCapability(tile, capability, side.m_122424_());
      if (acceptor.isPresent()) {
         this.updateCachedAcceptorAndListen(side, tile, acceptor);
         return true;
      } else {
         return false;
      }
   }

   public static class AcceptorInfo<ACCEPTOR> extends AbstractAcceptorInfo {
      private LazyOptional<?> sourceAcceptor;
      private LazyOptional<ACCEPTOR> acceptor;

      private AcceptorInfo(BlockEntity tile, LazyOptional<?> sourceAcceptor, LazyOptional<ACCEPTOR> acceptor) {
         super(tile);
         this.acceptor = acceptor;
         this.sourceAcceptor = sourceAcceptor;
      }

      private void updateAcceptor(LazyOptional<?> sourceAcceptor, LazyOptional<ACCEPTOR> acceptor) {
         this.sourceAcceptor = sourceAcceptor;
         this.acceptor = acceptor;
      }

      @Override
      public boolean equals(Object o) {
         return o == this
            ? true
            : o instanceof AcceptorCache.AcceptorInfo<?> other
               && this.getTile().equals(other.getTile())
               && this.sourceAcceptor.equals(other.sourceAcceptor)
               && this.acceptor.equals(other.acceptor);
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.getTile(), this.sourceAcceptor, this.acceptor);
      }
   }
}
