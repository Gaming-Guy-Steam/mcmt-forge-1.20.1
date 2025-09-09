package mekanism.common.lib.transmitter.acceptor;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.content.network.transmitter.Transmitter;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.Direction;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public abstract class AbstractAcceptorCache<ACCEPTOR, INFO extends AbstractAcceptorInfo> {
   private final Map<Direction, NonNullConsumer<LazyOptional<ACCEPTOR>>> cachedListeners = new EnumMap<>(Direction.class);
   protected final Map<Direction, INFO> cachedAcceptors = new EnumMap<>(Direction.class);
   protected final Transmitter<ACCEPTOR, ?, ?> transmitter;
   private final TileEntityTransmitter transmitterTile;
   public byte currentAcceptorConnections = 0;

   protected AbstractAcceptorCache(Transmitter<ACCEPTOR, ?, ?> transmitter, TileEntityTransmitter transmitterTile) {
      this.transmitter = transmitter;
      this.transmitterTile = transmitterTile;
   }

   public void clear() {
      this.cachedListeners.clear();
      this.cachedAcceptors.clear();
   }

   public void invalidateCachedAcceptor(Direction side) {
      if (this.cachedAcceptors.containsKey(side)) {
         this.cachedAcceptors.remove(side);
         this.transmitter.markDirtyAcceptor(side);
      }
   }

   public LazyOptional<ACCEPTOR> getCachedAcceptor(Direction side) {
      return Transmitter.connectionMapContainsSide(this.currentAcceptorConnections, side) ? this.getConnectedAcceptor(side) : LazyOptional.empty();
   }

   public List<ACCEPTOR> getConnectedAcceptors(Set<Direction> sides) {
      List<ACCEPTOR> acceptors = new ArrayList<>(sides.size());

      for (Direction side : sides) {
         this.getConnectedAcceptor(side).ifPresent(acceptors::add);
      }

      return acceptors;
   }

   protected abstract LazyOptional<ACCEPTOR> getConnectedAcceptor(Direction side);

   protected NonNullConsumer<LazyOptional<ACCEPTOR>> getRefreshListener(@NotNull Direction side) {
      return this.cachedListeners.computeIfAbsent(side, s -> new AbstractAcceptorCache.RefreshListener<>(this.transmitterTile, s));
   }

   private static class RefreshListener<ACCEPTOR> implements NonNullConsumer<LazyOptional<ACCEPTOR>> {
      private final WeakReference<TileEntityTransmitter> tile;
      private final Direction side;

      private RefreshListener(TileEntityTransmitter tile, Direction side) {
         this.tile = new WeakReference<>(tile);
         this.side = side;
      }

      public void accept(@NotNull LazyOptional<ACCEPTOR> ignored) {
         TileEntityTransmitter transmitterTile = this.tile.get();
         if (transmitterTile != null
            && !transmitterTile.m_58901_()
            && transmitterTile.m_58898_()
            && transmitterTile.isLoaded()
            && WorldUtils.isBlockLoaded(transmitterTile.m_58904_(), transmitterTile.m_58899_().m_121945_(this.side))) {
            transmitterTile.getTransmitter().refreshConnections(this.side);
         }
      }
   }
}
