package mekanism.common.lib.multiblock;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.function.ToIntFunction;
import mekanism.common.lib.math.voxel.BlockPosBuilder;
import mekanism.common.lib.math.voxel.VoxelPlane;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;

public class Structure {
   public static final Structure INVALID = new Structure();
   private final Map<BlockPos, IMultiblockBase> nodes = new Object2ObjectOpenHashMap();
   private final Map<Structure.Axis, NavigableMap<Integer, VoxelPlane>> minorPlaneMap = new EnumMap<>(Structure.Axis.class);
   private final Map<Structure.Axis, NavigableMap<Integer, VoxelPlane>> planeMap = new EnumMap<>(Structure.Axis.class);
   private boolean valid;
   private long updateTimestamp;
   private boolean didUpdate;
   private MultiblockData multiblockData;
   private IMultiblock<?> controller;

   private Structure() {
   }

   public Structure(IMultiblockBase node) {
      this.init(node);
      this.valid = true;
   }

   private void init(IMultiblockBase node) {
      BlockPos pos = node.getTilePos();
      this.nodes.put(pos, node);

      for (Structure.Axis axis : Structure.Axis.AXES) {
         this.getMinorAxisMap(axis).put(axis.getCoord(pos), new VoxelPlane(axis, pos, node instanceof IMultiblock));
      }

      if (node instanceof IMultiblock<?> multiblock && (this.getController() == null || multiblock.canBeMaster())) {
         this.controller = multiblock;
      }
   }

   public MultiblockData getMultiblockData() {
      return this.multiblockData;
   }

   public void setMultiblockData(MultiblockData multiblockData) {
      boolean changed = this.multiblockData != multiblockData;
      this.multiblockData = multiblockData;
      if (changed) {
         for (IMultiblockBase node : this.nodes.values()) {
            node.resetForFormed();
         }
      }
   }

   public IMultiblock<?> getController() {
      return this.controller;
   }

   public MultiblockManager<?> getManager() {
      return this.getController() != null && this.valid ? this.getController().getManager() : null;
   }

   public IMultiblockBase getTile(BlockPos pos) {
      return this.nodes.get(pos);
   }

   public NavigableMap<Integer, VoxelPlane> getMinorAxisMap(Structure.Axis axis) {
      return this.minorPlaneMap.computeIfAbsent(axis, k -> new TreeMap<>(Integer::compare));
   }

   public NavigableMap<Integer, VoxelPlane> getMajorAxisMap(Structure.Axis axis) {
      return this.planeMap.computeIfAbsent(axis, k -> new TreeMap<>(Integer::compare));
   }

   public void markForUpdate(Level world, boolean invalidate) {
      this.updateTimestamp = world.m_46467_();
      this.didUpdate = false;
      if (invalidate) {
         this.invalidate(world);
      } else {
         this.removeMultiblock(world);
      }
   }

   public <TILE extends BlockEntity & IMultiblockBase> void doImmediateUpdate(TILE tile, boolean tryValidate) {
      this.updateTimestamp = tile.m_58904_().m_46467_() - 1L;
      this.didUpdate = false;
      this.invalidate(tile.m_58904_());
      this.tick(tile, tryValidate);
   }

   public <TILE extends BlockEntity & IMultiblockBase> void tick(TILE tile, boolean tryValidate) {
      if (!this.didUpdate && this.updateTimestamp == tile.m_58904_().m_46467_() - 1L) {
         this.didUpdate = true;
         this.runUpdate(tile);
      }

      if (tryValidate && !this.isValid()) {
         validate(tile, new Long2ObjectOpenHashMap());
      }
   }

   public <TILE extends BlockEntity & IMultiblockBase> FormationProtocol.FormationResult runUpdate(TILE tile) {
      if (this.getController() != null && this.multiblockData == null) {
         return this.getController().createFormationProtocol().doUpdate();
      } else {
         this.removeMultiblock(tile.m_58904_());
         return FormationProtocol.FormationResult.FAIL;
      }
   }

   public void add(Structure s) {
      if (s != this) {
         if (s.getController() != null && s.getController().canBeMaster() && (this.getController() == null || !this.getController().canBeMaster())) {
            this.controller = s.getController();
         }

         MultiblockManager<?> manager = this.getManager();
         s.nodes.forEach((key, value) -> {
            this.nodes.put(key, value);
            value.setStructure(manager, this);
         });

         for (Entry<Structure.Axis, NavigableMap<Integer, VoxelPlane>> entry : s.minorPlaneMap.entrySet()) {
            Structure.Axis axis = entry.getKey();
            Map<Integer, VoxelPlane> minorMap = this.getMinorAxisMap(axis);
            Map<Integer, VoxelPlane> majorMap = this.getMajorAxisMap(axis);
            entry.getValue().forEach((key, value) -> {
               VoxelPlane majorPlane = majorMap.get(key);
               if (majorPlane != null) {
                  majorPlane.merge(value);
               } else {
                  VoxelPlane minorPlane = minorMap.get(key);
                  if (minorPlane == null) {
                     minorMap.put(key, value);
                  } else {
                     minorPlane.merge(value);
                     if (minorPlane.hasFrame() && minorPlane.length() >= 2 && minorPlane.height() >= 2) {
                        majorMap.put(key, minorPlane);
                        minorMap.remove(key);
                     }
                  }
               }
            });
         }

         for (Entry<Structure.Axis, NavigableMap<Integer, VoxelPlane>> entry : s.planeMap.entrySet()) {
            Structure.Axis axis = entry.getKey();
            Map<Integer, VoxelPlane> minorMap = this.getMinorAxisMap(axis);
            Map<Integer, VoxelPlane> majorMap = this.getMajorAxisMap(axis);
            entry.getValue().forEach((key, value) -> {
               VoxelPlane majorPlane = majorMap.get(key);
               if (majorPlane == null) {
                  majorMap.put(key, value);
                  VoxelPlane minorPlane = minorMap.get(key);
                  if (minorPlane != null) {
                     value.merge(minorPlane);
                     minorMap.remove(key);
                  }
               } else {
                  majorPlane.merge(value);
               }
            });
         }
      }
   }

   public boolean isValid() {
      return this.valid;
   }

   public void invalidate(Level world) {
      this.removeMultiblock(world);
      this.valid = false;
   }

   public void removeMultiblock(Level world) {
      if (this.multiblockData != null) {
         this.multiblockData.remove(world);
         this.multiblockData = null;
      }
   }

   public boolean contains(BlockPos pos) {
      return this.nodes.containsKey(pos);
   }

   public int size() {
      return this.nodes.size();
   }

   private static void validate(IMultiblockBase node, Long2ObjectMap<ChunkAccess> chunkMap) {
      if (node instanceof IMultiblock<?> multiblock) {
         if (!multiblock.getStructure().isValid()) {
            multiblock.resetStructure(multiblock.getManager());
         }
      } else if (node instanceof IStructuralMultiblock) {
         node.resetStructure(null);
      }

      FormationProtocol.explore(node.getTilePos(), pos -> {
         if (pos.equals(node.getTilePos())) {
            return true;
         } else if (WorldUtils.getTileEntity(node.getTileWorld(), chunkMap, pos) instanceof IMultiblockBase adj && isCompatible(node, adj)) {
            boolean didMerge = false;
            if (node instanceof IStructuralMultiblock && adj instanceof IStructuralMultiblock) {
               Set<MultiblockManager<?>> managers = new HashSet<>();
               managers.addAll(((IStructuralMultiblock)node).getStructureMap().keySet());
               managers.addAll(((IStructuralMultiblock)adj).getStructureMap().keySet());

               for (MultiblockManager<?> manager : managers) {
                  didMerge = mergeIfNecessary(node, adj, manager);
               }
            } else {
               if (node instanceof IStructuralMultiblock) {
                  if (!hasStructure(node, (IMultiblock<?>)adj)) {
                     validate(adj, chunkMap);
                  }

                  return false;
               }

               if (adj instanceof IStructuralMultiblock) {
                  didMerge = mergeIfNecessary(node, adj, getManager(node));
               } else {
                  didMerge = mergeIfNecessary(node, adj, getManager(node));
               }
            }

            return didMerge;
         } else {
            return false;
         }
      });
   }

   private static boolean hasStructure(IMultiblockBase structural, IMultiblock<?> multiblock) {
      return structural.getStructure(multiblock.getManager()) == multiblock.getStructure();
   }

   private static boolean mergeIfNecessary(IMultiblockBase node, IMultiblockBase adj, MultiblockManager<?> manager) {
      Structure nodeStructure = node.getStructure(manager);
      if (!nodeStructure.isValid()) {
         nodeStructure = node.resetStructure(manager);
      }

      Structure adjStructure = adj.getStructure(manager);
      if (!adjStructure.isValid()) {
         adjStructure = adj.resetStructure(manager);
      }

      if (node.hasStructure(adjStructure)) {
         return false;
      } else {
         Structure changed;
         if (nodeStructure.size() < adjStructure.size() && (nodeStructure.getManager() == null || adjStructure.getManager() != null)) {
            changed = adjStructure;
            adjStructure.add(nodeStructure);
         } else {
            changed = nodeStructure;
            nodeStructure.add(adjStructure);
         }

         changed.markForUpdate(node.getTileWorld(), false);
         return true;
      }
   }

   private static boolean isCompatible(IMultiblockBase node, IMultiblockBase other) {
      MultiblockManager<?> manager = getManager(node);
      MultiblockManager<?> otherManager = getManager(other);
      if (manager != null && otherManager != null) {
         return manager == otherManager;
      } else if (manager == null && otherManager == null) {
         return true;
      } else if (manager == null && node instanceof IStructuralMultiblock multiblock) {
         return multiblock.canInterface(otherManager);
      } else {
         return otherManager == null && other instanceof IStructuralMultiblock multiblock ? multiblock.canInterface(manager) : false;
      }
   }

   private static MultiblockManager<?> getManager(IMultiblockBase node) {
      return node instanceof IMultiblock<?> multiblock ? multiblock.getManager() : null;
   }

   public static enum Axis {
      X(Vec3i::m_123341_),
      Y(Vec3i::m_123342_),
      Z(Vec3i::m_123343_);

      private final ToIntFunction<BlockPos> posMapper;
      static final Structure.Axis[] AXES = values();

      private Axis(ToIntFunction<BlockPos> posMapper) {
         this.posMapper = posMapper;
      }

      public int getCoord(BlockPos pos) {
         return this.posMapper.applyAsInt(pos);
      }

      public void set(BlockPosBuilder pos, int val) {
         pos.set(this, val);
      }

      public Structure.Axis horizontal() {
         return this == X ? Z : X;
      }

      public Structure.Axis vertical() {
         return this == Y ? Z : Y;
      }

      public static Structure.Axis get(Direction side) {
         return AXES[side.m_122434_().ordinal()];
      }
   }
}
