package mekanism.common.lib.math.voxel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import mekanism.common.lib.multiblock.Structure;
import net.minecraft.core.BlockPos;

public class VoxelPlane {
   private final Structure.Axis axis;
   private int minCol;
   private int maxCol;
   private int minRow;
   private int maxRow;
   private int size;
   private boolean hasFrame;
   private final Set<BlockPos> outsideSet = new HashSet<>();

   public VoxelPlane(Structure.Axis axis, BlockPos pos, boolean frame) {
      this.axis = axis;
      if (frame) {
         this.size = 1;
         this.minCol = this.maxCol = axis.horizontal().getCoord(pos);
         this.minRow = this.maxRow = axis.vertical().getCoord(pos);
         this.hasFrame = true;
      } else {
         this.outsideSet.add(pos);
      }
   }

   public boolean isFull() {
      return this.size > 0 && this.getMissing() == 0;
   }

   public int getMissing() {
      return this.length() * this.height() - this.size;
   }

   public int length() {
      return this.hasFrame ? this.maxCol - this.minCol + 1 : 0;
   }

   public int height() {
      return this.hasFrame ? this.maxRow - this.minRow + 1 : 0;
   }

   public boolean hasFrame() {
      return this.hasFrame;
   }

   public void merge(VoxelPlane other) {
      this.outsideSet.addAll(other.outsideSet);
      if (other.hasFrame) {
         this.size = this.size + other.size;
         if (this.hasFrame) {
            this.minCol = Math.min(this.minCol, other.minCol);
            this.maxCol = Math.max(this.maxCol, other.maxCol);
            this.minRow = Math.min(this.minRow, other.minRow);
            this.maxRow = Math.max(this.maxRow, other.maxRow);
         } else {
            this.minCol = other.minCol;
            this.maxCol = other.maxCol;
            this.minRow = other.minRow;
            this.maxRow = other.maxRow;
            this.hasFrame = true;
         }
      }

      if (this.hasFrame) {
         this.outsideSet.removeIf(pos -> {
            if (this.isOutside(pos)) {
               return false;
            } else {
               this.size++;
               return true;
            }
         });
      }
   }

   public Structure.Axis getAxis() {
      return this.axis;
   }

   public int size() {
      return this.size;
   }

   public int getMinRow() {
      return this.minRow;
   }

   public int getMaxRow() {
      return this.maxRow;
   }

   public int getMinCol() {
      return this.minCol;
   }

   public int getMaxCol() {
      return this.maxCol;
   }

   public boolean isOutside(BlockPos pos) {
      int col = this.axis.horizontal().getCoord(pos);
      int row = this.axis.vertical().getCoord(pos);
      return col < this.minCol || col > this.maxCol || row < this.minRow || row > this.maxRow;
   }

   @Override
   public String toString() {
      return "Plane(full="
         + this.isFull()
         + ", size="
         + this.size()
         + ", frame="
         + this.hasFrame
         + ", bounds="
         + List.<Integer>of(this.minCol, this.minRow, this.maxCol, this.maxRow)
         + ")";
   }

   @Override
   public boolean equals(Object obj) {
      return obj instanceof VoxelPlane other
         && this.size == other.size
         && this.minCol == other.minCol
         && this.maxCol == other.maxCol
         && this.minRow == other.minRow
         && this.maxRow == other.maxRow;
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + this.size;
      result = 31 * result + this.minCol;
      result = 31 * result + this.maxCol;
      result = 31 * result + this.minRow;
      return 31 * result + this.maxRow;
   }
}
