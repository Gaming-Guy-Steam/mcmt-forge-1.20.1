package mekanism.common.lib.transmitter.acceptor;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import mekanism.common.content.network.transmitter.Transmitter;
import mekanism.common.lib.transmitter.TransmitterNetworkRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraftforge.common.util.LazyOptional;

public class NetworkAcceptorCache<ACCEPTOR> {
   private final Map<BlockPos, Map<Direction, LazyOptional<ACCEPTOR>>> cachedAcceptors = new Object2ObjectOpenHashMap();
   private final Map<Transmitter<ACCEPTOR, ?, ?>, Set<Direction>> changedAcceptors = new Object2ObjectOpenHashMap();

   public void updateTransmitterOnSide(Transmitter<ACCEPTOR, ?, ?> transmitter, Direction side) {
      LazyOptional<ACCEPTOR> acceptor = transmitter.canConnectToAcceptor(side) ? transmitter.getAcceptor(side) : LazyOptional.empty();
      BlockPos acceptorPos = transmitter.getTilePos().m_121945_(side);
      if (acceptor.isPresent()) {
         this.cachedAcceptors.computeIfAbsent(acceptorPos, pos -> new EnumMap<>(Direction.class)).put(side.m_122424_(), acceptor);
      } else if (this.cachedAcceptors.containsKey(acceptorPos)) {
         Map<Direction, LazyOptional<ACCEPTOR>> cached = this.cachedAcceptors.get(acceptorPos);
         cached.remove(side.m_122424_());
         if (cached.isEmpty()) {
            this.cachedAcceptors.remove(acceptorPos);
         }
      } else {
         this.cachedAcceptors.remove(acceptorPos);
      }
   }

   public void adoptAcceptors(NetworkAcceptorCache<ACCEPTOR> other) {
      for (Entry<BlockPos, Map<Direction, LazyOptional<ACCEPTOR>>> entry : other.cachedAcceptors.entrySet()) {
         BlockPos pos = entry.getKey();
         if (this.cachedAcceptors.containsKey(pos)) {
            this.cachedAcceptors.get(pos).putAll(entry.getValue());
         } else {
            this.cachedAcceptors.put(pos, entry.getValue());
         }
      }

      for (Entry<Transmitter<ACCEPTOR, ?, ?>, Set<Direction>> entryx : other.changedAcceptors.entrySet()) {
         Transmitter<ACCEPTOR, ?, ?> transmitter = entryx.getKey();
         if (this.changedAcceptors.containsKey(transmitter)) {
            this.changedAcceptors.get(transmitter).addAll(entryx.getValue());
         } else {
            this.changedAcceptors.put(transmitter, entryx.getValue());
         }
      }
   }

   public void acceptorChanged(Transmitter<ACCEPTOR, ?, ?> transmitter, Direction side) {
      this.changedAcceptors.computeIfAbsent(transmitter, t -> EnumSet.noneOf(Direction.class)).add(side);
      TransmitterNetworkRegistry.registerChangedNetwork(transmitter.getTransmitterNetwork());
   }

   public void commit() {
      if (!this.changedAcceptors.isEmpty()) {
         for (Entry<Transmitter<ACCEPTOR, ?, ?>, Set<Direction>> entry : this.changedAcceptors.entrySet()) {
            Transmitter<ACCEPTOR, ?, ?> transmitter = entry.getKey();
            if (transmitter.isValid()) {
               for (Direction side : entry.getValue()) {
                  this.updateTransmitterOnSide(transmitter, side);
               }
            }
         }

         this.changedAcceptors.clear();
      }
   }

   public void deregister() {
      this.cachedAcceptors.clear();
      this.changedAcceptors.clear();
   }

   public Set<Entry<BlockPos, Map<Direction, LazyOptional<ACCEPTOR>>>> getAcceptorEntrySet() {
      return this.cachedAcceptors.entrySet();
   }

   public Collection<Map<Direction, LazyOptional<ACCEPTOR>>> getAcceptorValues() {
      return this.cachedAcceptors.values();
   }

   public int getAcceptorCount() {
      return this.cachedAcceptors.values().stream().mapToInt(Map::size).sum();
   }

   public boolean hasAcceptor(BlockPos acceptorPos) {
      return this.cachedAcceptors.containsKey(acceptorPos);
   }

   public Set<Direction> getAcceptorDirections(BlockPos pos) {
      return this.cachedAcceptors.get(pos).keySet();
   }
}
