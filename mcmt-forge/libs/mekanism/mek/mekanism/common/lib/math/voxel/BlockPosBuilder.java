package mekanism.common.lib.math.voxel;

import mekanism.common.lib.multiblock.Structure;
import net.minecraft.core.BlockPos;

public class BlockPosBuilder {
   private final int[] pos = new int[3];
   private final boolean[] set = new boolean[3];

   public BlockPos build() {
      return new BlockPos(this.pos[0], this.pos[1], this.pos[2]);
   }

   public boolean isSet(Structure.Axis axis) {
      return this.set[axis.ordinal()];
   }

   public void set(Structure.Axis axis, int value) {
      this.pos[axis.ordinal()] = value;
      this.set[axis.ordinal()] = true;
   }

   public int get(Structure.Axis axis) {
      return this.pos[axis.ordinal()];
   }
}
