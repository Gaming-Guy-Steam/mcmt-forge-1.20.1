package mekanism.common.lib.multiblock;

import java.util.NavigableMap;
import java.util.Set;
import java.util.Map.Entry;
import mekanism.common.lib.math.voxel.VoxelCuboid;
import mekanism.common.lib.math.voxel.VoxelPlane;

public class StructureHelper {
   private StructureHelper() {
   }

   public static VoxelCuboid fetchCuboid(Structure structure, VoxelCuboid minBounds, VoxelCuboid maxBounds) {
      VoxelCuboid prev = null;
      Structure.Axis[] var4 = Structure.Axis.AXES;
      int var5 = var4.length;
      int var6 = 0;

      while (var6 < var5) {
         Structure.Axis axis = var4[var6];
         NavigableMap<Integer, VoxelPlane> majorAxisMap = structure.getMajorAxisMap(axis);
         Entry<Integer, VoxelPlane> firstMajor = majorAxisMap.firstEntry();
         Entry<Integer, VoxelPlane> lastMajor = majorAxisMap.lastEntry();
         if (firstMajor != null && firstMajor.getValue().equals(lastMajor.getValue()) && firstMajor.getValue().isFull()) {
            VoxelCuboid cuboid = VoxelCuboid.from(firstMajor.getValue(), lastMajor.getValue(), firstMajor.getKey(), lastMajor.getKey());
            if (prev != null || cuboid.greaterOrEqual(minBounds) && maxBounds.greaterOrEqual(cuboid)) {
               if (prev != null && !prev.equals(cuboid)) {
                  return null;
               }

               NavigableMap<Integer, VoxelPlane> minorAxisMap = structure.getMinorAxisMap(axis);
               if (minorAxisMap.isEmpty()
                  || !hasOutOfBoundsNegativeMinor(minorAxisMap, firstMajor.getKey()) && !hasOutOfBoundsPositiveMinor(minorAxisMap, lastMajor.getKey())) {
                  prev = cuboid;
                  var6++;
                  continue;
               }

               return null;
            }

            return null;
         }

         return null;
      }

      return prev;
   }

   public static VoxelCuboid fetchCuboid(Structure structure, VoxelCuboid minBounds, VoxelCuboid maxBounds, Set<VoxelCuboid.CuboidSide> sides, int tolerance) {
      if (sides.size() < 2) {
         return null;
      } else {
         int missing = 0;
         VoxelCuboid.CuboidBuilder builder = new VoxelCuboid.CuboidBuilder();

         for (VoxelCuboid.CuboidSide side : sides) {
            Structure.Axis axis = side.getAxis();
            Structure.Axis horizontal = axis.horizontal();
            Structure.Axis vertical = axis.vertical();
            NavigableMap<Integer, VoxelPlane> majorAxisMap = structure.getMajorAxisMap(axis);
            Entry<Integer, VoxelPlane> majorEntry = side.getFace().isPositive() ? majorAxisMap.lastEntry() : majorAxisMap.firstEntry();
            if (majorEntry == null) {
               return null;
            }

            VoxelPlane plane = majorEntry.getValue();
            missing += plane.getMissing();
            if (missing > tolerance) {
               return null;
            }

            int majorKey = majorEntry.getKey();
            builder.set(side, majorKey);
            if (!builder.trySet(VoxelCuboid.CuboidSide.get(VoxelCuboid.CuboidSide.Face.NEGATIVE, horizontal), plane.getMinCol())
               || !builder.trySet(VoxelCuboid.CuboidSide.get(VoxelCuboid.CuboidSide.Face.POSITIVE, horizontal), plane.getMaxCol())
               || !builder.trySet(VoxelCuboid.CuboidSide.get(VoxelCuboid.CuboidSide.Face.NEGATIVE, vertical), plane.getMinRow())
               || !builder.trySet(VoxelCuboid.CuboidSide.get(VoxelCuboid.CuboidSide.Face.POSITIVE, vertical), plane.getMaxRow())) {
               return null;
            }

            NavigableMap<Integer, VoxelPlane> minorAxisMap = structure.getMinorAxisMap(axis);
            if (!minorAxisMap.isEmpty()) {
               if (side.getFace().isPositive()) {
                  if (hasOutOfBoundsPositiveMinor(minorAxisMap, majorKey)) {
                     return null;
                  }
               } else if (hasOutOfBoundsNegativeMinor(minorAxisMap, majorKey)) {
                  return null;
               }
            }
         }

         VoxelCuboid ret = builder.build();
         return ret.greaterOrEqual(minBounds) && maxBounds.greaterOrEqual(ret) ? ret : null;
      }
   }

   private static boolean hasOutOfBoundsPositiveMinor(NavigableMap<Integer, VoxelPlane> minorAxisMap, int majorKey) {
      Entry<Integer, VoxelPlane> minorEntry = minorAxisMap.lastEntry();

      while (minorEntry != null) {
         int minorKey = minorEntry.getKey();
         if (minorKey <= majorKey) {
            break;
         }

         if (minorEntry.getValue().hasFrame()) {
            return true;
         }

         minorEntry = minorAxisMap.lowerEntry(minorKey);
      }

      return false;
   }

   private static boolean hasOutOfBoundsNegativeMinor(NavigableMap<Integer, VoxelPlane> minorAxisMap, int majorKey) {
      Entry<Integer, VoxelPlane> minorEntry = minorAxisMap.firstEntry();

      while (minorEntry != null) {
         int minorKey = minorEntry.getKey();
         if (minorKey >= majorKey) {
            break;
         }

         if (minorEntry.getValue().hasFrame()) {
            return true;
         }

         minorEntry = minorAxisMap.higherEntry(minorKey);
      }

      return false;
   }
}
