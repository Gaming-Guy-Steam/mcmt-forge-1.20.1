package mekanism.common.lib.math.voxel;

import mekanism.common.lib.multiblock.Structure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class VoxelCuboid implements IShape {
   private BlockPos minPos;
   private BlockPos maxPos;

   public VoxelCuboid(BlockPos minPos, BlockPos maxPos) {
      this.minPos = minPos;
      this.maxPos = maxPos;
   }

   public VoxelCuboid(int length, int height, int width) {
      this(BlockPos.f_121853_, new BlockPos(length - 1, height - 1, width - 1));
   }

   public int length() {
      return this.maxPos.m_123341_() - this.minPos.m_123341_() + 1;
   }

   public int width() {
      return this.maxPos.m_123343_() - this.minPos.m_123343_() + 1;
   }

   public int height() {
      return this.maxPos.m_123342_() - this.minPos.m_123342_() + 1;
   }

   public BlockPos getMinPos() {
      return this.minPos;
   }

   public BlockPos getMaxPos() {
      return this.maxPos;
   }

   public void setMinPos(BlockPos minPos) {
      this.minPos = minPos;
   }

   public void setMaxPos(BlockPos maxPos) {
      this.maxPos = maxPos;
   }

   public BlockPos getCenter() {
      return new BlockPos(
         (this.minPos.m_123341_() + this.maxPos.m_123341_()) / 2,
         (this.minPos.m_123342_() + this.maxPos.m_123342_()) / 2,
         (this.minPos.m_123343_() + this.maxPos.m_123343_()) / 2
      );
   }

   public Direction getSide(BlockPos pos) {
      if (pos.m_123341_() == this.minPos.m_123341_()) {
         return Direction.WEST;
      } else if (pos.m_123341_() == this.maxPos.m_123341_()) {
         return Direction.EAST;
      } else if (pos.m_123342_() == this.minPos.m_123342_()) {
         return Direction.DOWN;
      } else if (pos.m_123342_() == this.maxPos.m_123342_()) {
         return Direction.UP;
      } else if (pos.m_123343_() == this.minPos.m_123343_()) {
         return Direction.NORTH;
      } else {
         return pos.m_123343_() == this.maxPos.m_123343_() ? Direction.SOUTH : null;
      }
   }

   public boolean isOnSide(BlockPos pos) {
      return this.getWallRelative(pos).isWall();
   }

   public boolean isOnEdge(BlockPos pos) {
      return this.getWallRelative(pos).isOnEdge();
   }

   public boolean isOnCorner(BlockPos pos) {
      return this.getWallRelative(pos).isOnCorner();
   }

   public VoxelCuboid.WallRelative getWallRelative(BlockPos pos) {
      int matches = this.getMatches(pos);
      if (matches >= 3) {
         return VoxelCuboid.WallRelative.CORNER;
      } else if (matches == 2) {
         return VoxelCuboid.WallRelative.EDGE;
      } else {
         return matches == 1 ? VoxelCuboid.WallRelative.SIDE : VoxelCuboid.WallRelative.INVALID;
      }
   }

   public int getMatches(BlockPos pos) {
      int matches = 0;
      if (pos.m_123341_() == this.minPos.m_123341_()) {
         matches++;
      }

      if (pos.m_123341_() == this.maxPos.m_123341_()) {
         matches++;
      }

      if (pos.m_123342_() == this.minPos.m_123342_()) {
         matches++;
      }

      if (pos.m_123342_() == this.maxPos.m_123342_()) {
         matches++;
      }

      if (pos.m_123343_() == this.minPos.m_123343_()) {
         matches++;
      }

      if (pos.m_123343_() == this.maxPos.m_123343_()) {
         matches++;
      }

      return matches;
   }

   public VoxelCuboid.CuboidRelative getRelativeLocation(BlockPos pos) {
      if (pos.m_123341_() > this.minPos.m_123341_()
         && pos.m_123341_() < this.maxPos.m_123341_()
         && pos.m_123342_() > this.minPos.m_123342_()
         && pos.m_123342_() < this.maxPos.m_123342_()
         && pos.m_123343_() > this.minPos.m_123343_()
         && pos.m_123343_() < this.maxPos.m_123343_()) {
         return VoxelCuboid.CuboidRelative.INSIDE;
      } else {
         return pos.m_123341_() >= this.minPos.m_123341_()
               && pos.m_123341_() <= this.maxPos.m_123341_()
               && pos.m_123342_() >= this.minPos.m_123342_()
               && pos.m_123342_() <= this.maxPos.m_123342_()
               && pos.m_123343_() >= this.minPos.m_123343_()
               && pos.m_123343_() <= this.maxPos.m_123343_()
            ? VoxelCuboid.CuboidRelative.WALLS
            : VoxelCuboid.CuboidRelative.OUTSIDE;
      }
   }

   public boolean greaterOrEqual(VoxelCuboid other) {
      return this.length() >= other.length() && this.width() >= other.width() && this.height() >= other.height();
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + this.maxPos.hashCode();
      return 31 * result + this.minPos.hashCode();
   }

   @Override
   public boolean equals(Object obj) {
      return obj instanceof VoxelCuboid other && this.minPos.equals(other.minPos) && this.maxPos.equals(other.maxPos);
   }

   public static VoxelCuboid from(VoxelPlane p1, VoxelPlane p2, int p1Pos, int p2Pos) {
      BlockPosBuilder min = new BlockPosBuilder();
      BlockPosBuilder max = new BlockPosBuilder();
      p1.getAxis().set(min, p1Pos);
      p2.getAxis().set(max, p2Pos);
      p1.getAxis().horizontal().set(min, p1.getMinCol());
      p1.getAxis().horizontal().set(max, p1.getMaxCol());
      p1.getAxis().vertical().set(min, p1.getMinRow());
      p1.getAxis().vertical().set(max, p1.getMaxRow());
      return new VoxelCuboid(min.build(), max.build());
   }

   @Override
   public String toString() {
      return "Cuboid(start=" + this.minPos + ", bounds=(" + this.length() + "," + this.height() + "," + this.width() + "))";
   }

   public static class CuboidBuilder {
      private final BlockPosBuilder[] bounds = new BlockPosBuilder[]{new BlockPosBuilder(), new BlockPosBuilder()};

      public boolean isSet(VoxelCuboid.CuboidSide side) {
         return this.bounds[side.getFace().ordinal()].isSet(side.getAxis());
      }

      public void set(VoxelCuboid.CuboidSide side, int val) {
         this.bounds[side.getFace().ordinal()].set(side.getAxis(), val);
      }

      public boolean trySet(VoxelCuboid.CuboidSide side, int val) {
         if (this.isSet(side) && this.get(side) != val) {
            return false;
         } else {
            this.set(side, val);
            return true;
         }
      }

      public int get(VoxelCuboid.CuboidSide side) {
         return this.bounds[side.getFace().ordinal()].get(side.getAxis());
      }

      public VoxelCuboid build() {
         return new VoxelCuboid(this.bounds[0].build(), this.bounds[1].build());
      }
   }

   public static enum CuboidRelative {
      INSIDE,
      OUTSIDE,
      WALLS;

      public boolean isWall() {
         return this == WALLS;
      }
   }

   public static enum CuboidSide {
      BOTTOM(Structure.Axis.Y, VoxelCuboid.CuboidSide.Face.NEGATIVE),
      TOP(Structure.Axis.Y, VoxelCuboid.CuboidSide.Face.POSITIVE),
      NORTH(Structure.Axis.Z, VoxelCuboid.CuboidSide.Face.NEGATIVE),
      SOUTH(Structure.Axis.Z, VoxelCuboid.CuboidSide.Face.POSITIVE),
      WEST(Structure.Axis.X, VoxelCuboid.CuboidSide.Face.NEGATIVE),
      EAST(Structure.Axis.X, VoxelCuboid.CuboidSide.Face.POSITIVE);

      public static final VoxelCuboid.CuboidSide[] SIDES = values();
      private static final VoxelCuboid.CuboidSide[][] ORDERED = new VoxelCuboid.CuboidSide[][]{{WEST, BOTTOM, NORTH}, {EAST, TOP, SOUTH}};
      private static final VoxelCuboid.CuboidSide[] OPPOSITES = new VoxelCuboid.CuboidSide[]{TOP, BOTTOM, SOUTH, NORTH, EAST, WEST};
      private final Structure.Axis axis;
      private final VoxelCuboid.CuboidSide.Face face;

      private CuboidSide(Structure.Axis axis, VoxelCuboid.CuboidSide.Face face) {
         this.axis = axis;
         this.face = face;
      }

      public Structure.Axis getAxis() {
         return this.axis;
      }

      public VoxelCuboid.CuboidSide.Face getFace() {
         return this.face;
      }

      public VoxelCuboid.CuboidSide flip() {
         return OPPOSITES[this.ordinal()];
      }

      public static VoxelCuboid.CuboidSide get(VoxelCuboid.CuboidSide.Face face, Structure.Axis axis) {
         return ORDERED[face.ordinal()][axis.ordinal()];
      }

      public static enum Face {
         NEGATIVE,
         POSITIVE;

         public VoxelCuboid.CuboidSide.Face getOpposite() {
            return this == POSITIVE ? NEGATIVE : POSITIVE;
         }

         public boolean isPositive() {
            return this == POSITIVE;
         }
      }
   }

   public static enum WallRelative {
      SIDE,
      EDGE,
      CORNER,
      INVALID;

      public boolean isWall() {
         return this != INVALID;
      }

      public boolean isOnEdge() {
         return this == EDGE || this == CORNER;
      }

      public boolean isOnCorner() {
         return this == CORNER;
      }
   }
}
