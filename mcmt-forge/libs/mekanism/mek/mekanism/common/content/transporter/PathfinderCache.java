package mekanism.common.content.transporter;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import mekanism.common.content.network.InventoryNetwork;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class PathfinderCache {
   private static final Map<UUID, Map<PathfinderCache.PathData, PathfinderCache.CachedPath>> cachedPaths = new Object2ObjectOpenHashMap();

   private PathfinderCache() {
   }

   public static void onChanged(InventoryNetwork... networks) {
      for (InventoryNetwork network : networks) {
         cachedPaths.remove(network.getUUID());
      }
   }

   public static void addCachedPath(LogisticalTransporterBase start, PathfinderCache.PathData data, List<BlockPos> positions, double cost) {
      cachedPaths.computeIfAbsent(start.getTransmitterNetwork().getUUID(), uuid -> new Object2ObjectOpenHashMap())
         .put(data, new PathfinderCache.CachedPath(positions, cost));
   }

   public static PathfinderCache.CachedPath getCache(LogisticalTransporterBase start, BlockPos end, Set<Direction> sides) {
      PathfinderCache.CachedPath ret = null;
      UUID uuid = start.getTransmitterNetwork().getUUID();
      if (cachedPaths.containsKey(uuid)) {
         Map<PathfinderCache.PathData, PathfinderCache.CachedPath> pathMap = cachedPaths.get(uuid);

         for (Direction side : sides) {
            PathfinderCache.CachedPath test = pathMap.get(new PathfinderCache.PathData(start.getTilePos(), end, side));
            if (ret == null || test != null && test.cost() < ret.cost()) {
               ret = test;
            }
         }
      }

      return ret;
   }

   public static void reset() {
      cachedPaths.clear();
   }

   public record CachedPath(List<BlockPos> path, double cost) {
   }

   public static class PathData {
      private final BlockPos startTransporter;
      private final BlockPos end;
      private final Direction endSide;
      private final int hash;

      public PathData(BlockPos s, BlockPos e, Direction es) {
         this.startTransporter = s;
         this.end = e;
         this.endSide = es;
         int code = 1;
         code = 31 * code + this.startTransporter.hashCode();
         code = 31 * code + this.end.hashCode();
         code = 31 * code + this.endSide.hashCode();
         this.hash = code;
      }

      @Override
      public boolean equals(Object obj) {
         return obj instanceof PathfinderCache.PathData data
            && data.startTransporter.equals(this.startTransporter)
            && data.end.equals(this.end)
            && data.endSide == this.endSide;
      }

      @Override
      public int hashCode() {
         return this.hash;
      }
   }
}
