package mekanism.common.content.transporter;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mekanism.api.Coord4D;
import mekanism.api.text.EnumColor;
import mekanism.common.content.network.InventoryNetwork;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.lib.SidedBlockPos;
import mekanism.common.lib.inventory.TransitRequest;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TransporterPathfinder {
   private TransporterPathfinder() {
   }

   private static List<TransporterPathfinder.Destination> getPaths(
      LogisticalTransporterBase start, TransporterStack stack, TransitRequest request, int min, Map<Coord4D, Set<TransporterStack>> additionalFlowingStacks
   ) {
      InventoryNetwork network = start.getTransmitterNetwork();
      if (network == null) {
         return Collections.emptyList();
      } else {
         Long2ObjectMap<ChunkAccess> chunkMap = new Long2ObjectOpenHashMap();
         List<InventoryNetwork.AcceptorData> acceptors = network.calculateAcceptors(request, stack, chunkMap, additionalFlowingStacks);
         List<TransporterPathfinder.Destination> paths = new ArrayList<>();

         for (InventoryNetwork.AcceptorData data : acceptors) {
            TransporterPathfinder.Destination path = getPath(network, data, start, stack, min, chunkMap);
            if (path != null) {
               paths.add(path);
            }
         }

         Collections.sort(paths);
         return paths;
      }
   }

   private static boolean checkPath(InventoryNetwork network, List<BlockPos> path, TransporterStack stack) {
      for (int i = path.size() - 1; i > 0; i--) {
         LogisticalTransporterBase transmitter = network.getTransmitter(path.get(i));
         if (transmitter == null) {
            return false;
         }

         EnumColor color = transmitter.getColor();
         if (color != null && color != stack.color) {
            return false;
         }
      }

      return true;
   }

   @Nullable
   private static TransporterPathfinder.Destination getPath(
      InventoryNetwork network,
      InventoryNetwork.AcceptorData data,
      LogisticalTransporterBase start,
      TransporterStack stack,
      int min,
      Long2ObjectMap<ChunkAccess> chunkMap
   ) {
      final TransitRequest.TransitResponse response = data.getResponse();
      if (response.getSendingAmount() >= min) {
         BlockPos dest = data.getLocation();
         PathfinderCache.CachedPath test = PathfinderCache.getCache(start, dest, data.getSides());
         if (test != null && checkPath(network, test.path(), stack)) {
            return new TransporterPathfinder.Destination(test.path(), false, response, test.cost());
         }

         TransporterPathfinder.Pathfinder p = new TransporterPathfinder.Pathfinder(new TransporterPathfinder.Pathfinder.DestChecker() {
            @Override
            public boolean isValid(TransporterStack stack, Direction side, BlockEntity tile) {
               return TransporterUtils.canInsert(tile, stack.color, response.getStack(), side, false);
            }
         }, network, start.getTileWorld(), dest, start.getTilePos(), stack);
         p.find(chunkMap);
         List<BlockPos> path = p.getPath();
         if (path.size() >= 2) {
            PathfinderCache.addCachedPath(start, new PathfinderCache.PathData(start.getTilePos(), dest, p.getSide()), path, p.finalScore);
            return new TransporterPathfinder.Destination(path, false, response, p.finalScore);
         }
      }

      return null;
   }

   @Nullable
   public static TransporterPathfinder.Destination getNewBasePath(LogisticalTransporterBase start, TransporterStack stack, TransitRequest request, int min) {
      return getNewBasePath(start, stack, request, min, Collections.emptyMap());
   }

   @Nullable
   public static TransporterPathfinder.Destination getNewBasePath(
      LogisticalTransporterBase start, TransporterStack stack, TransitRequest request, int min, Map<Coord4D, Set<TransporterStack>> additionalFlowingStacks
   ) {
      List<TransporterPathfinder.Destination> paths = getPaths(start, stack, request, min, additionalFlowingStacks);
      return paths.isEmpty() ? null : paths.get(0);
   }

   @Nullable
   public static TransporterPathfinder.Destination getNewRRPath(
      LogisticalTransporterBase start, TransporterStack stack, TransitRequest request, TileEntityLogisticalSorter outputter, int min
   ) {
      List<TransporterPathfinder.Destination> destinations = getPaths(start, stack, request, min, Collections.emptyMap());
      int destinationCount = destinations.size();
      if (destinationCount == 0) {
         return null;
      } else {
         if (destinationCount > 1 && outputter.rrTarget != null) {
            for (int i = 0; i < destinationCount; i++) {
               TransporterPathfinder.Destination destination = destinations.get(i);
               List<BlockPos> path = destination.getPath();
               BlockPos pos = path.get(0);
               if (outputter.rrTarget.pos().equals(pos)) {
                  Direction sideOfDest = WorldUtils.sideDifference(path.get(1), pos);
                  if (outputter.rrTarget.side() == sideOfDest) {
                     if (i == destinationCount - 1) {
                        outputter.rrTarget = SidedBlockPos.get(destinations.get(0));
                     } else {
                        outputter.rrTarget = SidedBlockPos.get(destinations.get(i + 1));
                     }

                     return destination;
                  }
               }
            }
         }

         TransporterPathfinder.Destination destination = destinations.get(0);
         if (destinationCount > 1) {
            outputter.rrTarget = SidedBlockPos.get(destinations.get(1));
         } else {
            outputter.rrTarget = SidedBlockPos.get(destination);
         }

         return destination;
      }
   }

   @Nullable
   public static TransporterPathfinder.IdlePathData getIdlePath(LogisticalTransporterBase start, TransporterStack stack) {
      InventoryNetwork network = start.getTransmitterNetwork();
      if (network == null) {
         return null;
      } else {
         if (stack.homeLocation != null) {
            Long2ObjectMap<ChunkAccess> chunkMap = new Long2ObjectOpenHashMap();
            TransporterPathfinder.Pathfinder p = new TransporterPathfinder.Pathfinder(new TransporterPathfinder.Pathfinder.DestChecker() {
               @Override
               public boolean isValid(TransporterStack stack, Direction side, BlockEntity tile) {
                  return TransporterUtils.canInsert(tile, stack.color, stack.itemStack, side, true);
               }
            }, network, start.getTileWorld(), stack.homeLocation, start.getTilePos(), stack);
            p.find(chunkMap);
            List<BlockPos> path = p.getPath();
            if (path.size() >= 2) {
               return new TransporterPathfinder.IdlePathData(path, TransporterStack.Path.HOME);
            }

            stack.homeLocation = null;
         }

         TransporterPathfinder.IdlePath d = new TransporterPathfinder.IdlePath(network, start.getTilePos(), stack);
         TransporterPathfinder.Destination dest = d.find();
         return dest == null ? null : new TransporterPathfinder.IdlePathData(dest.getPath(), dest.getPathType());
      }
   }

   public static class Destination implements Comparable<TransporterPathfinder.Destination> {
      private final TransitRequest.TransitResponse response;
      private final List<BlockPos> path;
      private final double score;
      private TransporterStack.Path pathType;

      public Destination(List<BlockPos> list, boolean inv, TransitRequest.TransitResponse ret, double gScore) {
         this.path = new ArrayList<>(list);
         if (inv) {
            Collections.reverse(this.path);
         }

         this.response = ret;
         this.score = gScore;
      }

      public TransporterPathfinder.Destination setPathType(TransporterStack.Path type) {
         this.pathType = type;
         return this;
      }

      @Override
      public int hashCode() {
         int code = 1;
         return 31 * code + this.path.hashCode();
      }

      @Override
      public boolean equals(Object o) {
         return o instanceof TransporterPathfinder.Destination other && other.path.equals(this.path);
      }

      public int compareTo(@NotNull TransporterPathfinder.Destination dest) {
         if (this.score < dest.score) {
            return -1;
         } else {
            return this.score > dest.score ? 1 : this.path.size() - dest.path.size();
         }
      }

      public TransitRequest.TransitResponse getResponse() {
         return this.response;
      }

      public TransporterStack.Path getPathType() {
         return this.pathType;
      }

      public List<BlockPos> getPath() {
         return this.path;
      }
   }

   public static class IdlePath {
      private final InventoryNetwork network;
      private final BlockPos start;
      private final TransporterStack transportStack;

      public IdlePath(InventoryNetwork network, BlockPos start, TransporterStack stack) {
         this.network = network;
         this.start = start;
         this.transportStack = stack;
      }

      public TransporterPathfinder.Destination find() {
         ArrayList<BlockPos> ret = new ArrayList<>();
         ret.add(this.start);
         LogisticalTransporterBase startTransmitter = this.network.getTransmitter(this.start);
         if (this.transportStack.idleDir == null) {
            return this.getDestination(ret, startTransmitter);
         } else {
            LogisticalTransporterBase transmitter = this.network.getTransmitter(this.start.m_121945_(this.transportStack.idleDir));
            if (this.transportStack.canInsertToTransporter(transmitter, this.transportStack.idleDir, startTransmitter)) {
               this.loopSide(ret, this.transportStack.idleDir, startTransmitter);
               return new TransporterPathfinder.Destination(ret, true, null, 0.0).setPathType(TransporterStack.Path.NONE);
            } else {
               TransitRequest request = TransitRequest.simple(this.transportStack.itemStack);
               if (startTransmitter != null) {
                  TransporterPathfinder.Destination newPath = TransporterPathfinder.getNewBasePath(startTransmitter, this.transportStack, request, 0);
                  if (newPath != null && newPath.getResponse() != null) {
                     this.transportStack.idleDir = null;
                     newPath.setPathType(TransporterStack.Path.DEST);
                     return newPath;
                  }
               }

               return this.getDestination(ret, startTransmitter);
            }
         }
      }

      @Nullable
      private TransporterPathfinder.Destination getDestination(List<BlockPos> ret, @Nullable LogisticalTransporterBase startTransmitter) {
         Direction newSide = this.findSide(startTransmitter);
         if (newSide != null) {
            this.transportStack.idleDir = newSide;
            this.loopSide(ret, newSide, startTransmitter);
            return new TransporterPathfinder.Destination(ret, true, null, 0.0).setPathType(TransporterStack.Path.NONE);
         } else {
            if (startTransmitter != null) {
               Direction sideClosest = this.transportStack.idleDir == null ? this.transportStack.getSide(startTransmitter) : this.transportStack.idleDir;

               for (Direction side : EnumSet.complementOf(EnumSet.of(sideClosest))) {
                  if (startTransmitter.getConnectionType(side) != ConnectionType.NONE) {
                     this.transportStack.idleDir = side;
                     ret.add(this.start.m_121945_(side));
                     return new TransporterPathfinder.Destination(ret, true, null, 0.0).setPathType(TransporterStack.Path.NONE);
                  }
               }
            }

            return null;
         }
      }

      private void loopSide(List<BlockPos> list, Direction side, @Nullable LogisticalTransporterBase startTransmitter) {
         LogisticalTransporterBase lastTransmitter = startTransmitter;
         BlockPos pos = this.start.m_121945_(side);

         for (LogisticalTransporterBase transmitter = this.network.getTransmitter(pos);
            this.transportStack.canInsertToTransporter(transmitter, side, lastTransmitter);
            transmitter = this.network.getTransmitter(pos)
         ) {
            lastTransmitter = transmitter;
            list.add(pos);
            pos = pos.m_121945_(side);
         }
      }

      private Direction findSide(@Nullable LogisticalTransporterBase startTransmitter) {
         if (this.transportStack.idleDir == null) {
            for (Direction side : EnumUtils.DIRECTIONS) {
               if (this.canInsertToTransporter(side, startTransmitter)) {
                  return side;
               }
            }
         } else {
            Direction opposite = this.transportStack.idleDir.m_122424_();

            for (Direction sidex : EnumSet.complementOf(EnumSet.of(opposite))) {
               if (this.canInsertToTransporter(sidex, startTransmitter)) {
                  return sidex;
               }
            }

            if (this.canInsertToTransporter(opposite, startTransmitter)) {
               return opposite;
            }
         }

         return null;
      }

      private boolean canInsertToTransporter(Direction from, @Nullable LogisticalTransporterBase startTransmitter) {
         return this.transportStack.canInsertToTransporter(this.network.getTransmitter(this.start.m_121945_(from)), from, startTransmitter);
      }
   }

   public record IdlePathData(List<BlockPos> path, TransporterStack.Path type) {
   }

   public static class Pathfinder {
      private final Set<BlockPos> openSet = new ObjectOpenHashSet();
      private final Set<BlockPos> closedSet = new ObjectOpenHashSet();
      private final Map<BlockPos, BlockPos> navMap = new Object2ObjectOpenHashMap();
      private final Object2DoubleOpenHashMap<BlockPos> gScore = new Object2DoubleOpenHashMap();
      private final Object2DoubleOpenHashMap<BlockPos> fScore = new Object2DoubleOpenHashMap();
      private final InventoryNetwork network;
      private final BlockPos start;
      private final BlockPos finalNode;
      private final TransporterStack transportStack;
      private final TransporterPathfinder.Pathfinder.DestChecker destChecker;
      private final Level world;
      private double finalScore;
      private Direction side;
      private List<BlockPos> results = new ArrayList<>();

      public Pathfinder(
         TransporterPathfinder.Pathfinder.DestChecker checker,
         InventoryNetwork network,
         Level world,
         BlockPos finalNode,
         BlockPos start,
         TransporterStack stack
      ) {
         this.destChecker = checker;
         this.network = network;
         this.world = world;
         this.finalNode = finalNode;
         this.start = start;
         this.transportStack = stack;
      }

      public boolean find(Long2ObjectMap<ChunkAccess> chunkMap) {
         this.openSet.add(this.start);
         this.gScore.put(this.start, 0.0);
         double totalDistance = WorldUtils.distanceBetween(this.start, this.finalNode);
         this.fScore.put(this.start, totalDistance);
         boolean hasValidDirection = false;
         LogisticalTransporterBase startTransmitter = this.network.getTransmitter(this.start);

         for (Direction direction : EnumUtils.DIRECTIONS) {
            BlockPos neighbor = this.start.m_121945_(direction);
            LogisticalTransporterBase neighborTransmitter = this.network.getTransmitter(neighbor);
            if (this.transportStack.canInsertToTransporter(neighborTransmitter, direction, startTransmitter)) {
               hasValidDirection = true;
               break;
            }

            if (this.isValidDestination(this.start, startTransmitter, direction, neighbor, chunkMap)) {
               return true;
            }
         }

         if (!hasValidDirection) {
            return false;
         } else {
            double maxSearchDistance = Math.max(2.0 * totalDistance, 4.0);

            while (!this.openSet.isEmpty()) {
               BlockPos currentNode = null;
               double lowestFScore = 0.0;

               for (BlockPos node : this.openSet) {
                  if (currentNode == null || this.fScore.getDouble(node) < lowestFScore) {
                     currentNode = node;
                     lowestFScore = this.fScore.getDouble(node);
                  }
               }

               if (currentNode == null) {
                  break;
               }

               this.openSet.remove(currentNode);
               this.closedSet.add(currentNode);
               if (!(WorldUtils.distanceBetween(this.start, currentNode) > maxSearchDistance)) {
                  LogisticalTransporterBase currentNodeTransmitter = this.network.getTransmitter(currentNode);
                  double currentScore = this.gScore.getDouble(currentNode);

                  for (Direction direction : EnumUtils.DIRECTIONS) {
                     BlockPos neighborx = currentNode.m_121945_(direction);
                     LogisticalTransporterBase neighborTransmitterx = this.network.getTransmitter(neighborx);
                     if (this.transportStack.canInsertToTransporter(neighborTransmitterx, direction, currentNodeTransmitter)) {
                        double tentativeG = currentScore + neighborTransmitterx.getCost();
                        if ((!this.closedSet.contains(neighborx) || !(tentativeG >= this.gScore.getDouble(neighborx)))
                           && (!this.openSet.contains(neighborx) || tentativeG < this.gScore.getDouble(neighborx))) {
                           this.navMap.put(neighborx, currentNode);
                           this.gScore.put(neighborx, tentativeG);
                           this.fScore.put(neighborx, tentativeG + WorldUtils.distanceBetween(neighborx, this.finalNode));
                           this.openSet.add(neighborx);
                        }
                     } else if (this.isValidDestination(currentNode, currentNodeTransmitter, direction, neighborx, chunkMap)) {
                        return true;
                     }
                  }
               }
            }

            return false;
         }
      }

      private boolean isValidDestination(
         BlockPos start, @Nullable LogisticalTransporterBase startTransporter, Direction direction, BlockPos neighbor, Long2ObjectMap<ChunkAccess> chunkMap
      ) {
         if (startTransporter != null && neighbor.equals(this.finalNode)) {
            BlockEntity neighborTile = WorldUtils.getTileEntity(this.world, chunkMap, neighbor);
            if (neighborTile != null
               && this.destChecker.isValid(this.transportStack, direction, neighborTile)
               && (startTransporter.canEmitTo(direction) || this.finalNode.equals(this.transportStack.homeLocation) && startTransporter.canConnect(direction))) {
               this.side = direction;
               this.results = this.reconstructPath(this.navMap, start);
               this.finalScore = this.gScore.getDouble(start) + WorldUtils.distanceBetween(start, this.finalNode);
               return true;
            }
         }

         return false;
      }

      private List<BlockPos> reconstructPath(Map<BlockPos, BlockPos> navMap, BlockPos nextNode) {
         List<BlockPos> path = new ArrayList<>();

         while (nextNode != null) {
            path.add(nextNode);
            nextNode = navMap.get(nextNode);
         }

         return path;
      }

      public List<BlockPos> getPath() {
         List<BlockPos> path = new ArrayList<>();
         path.add(this.finalNode);
         path.addAll(this.results);
         return path;
      }

      public Direction getSide() {
         return this.side;
      }

      public static class DestChecker {
         public boolean isValid(TransporterStack stack, Direction side, BlockEntity tile) {
            return false;
         }
      }
   }
}
