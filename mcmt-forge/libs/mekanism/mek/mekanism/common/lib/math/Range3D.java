package mekanism.common.lib.math;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record Range3D(int xMin, int zMin, int xMax, int zMax, ResourceKey<Level> dimension) {
   public Range3D clone() {
      return new Range3D(this.xMin, this.zMin, this.xMax, this.zMax, this.dimension);
   }

   @Override
   public String toString() {
      return "[Range3D: " + this.xMin + ", " + this.zMin + ", " + this.xMax + ", " + this.zMax + ", dim=" + this.dimension.m_135782_() + "]";
   }
}
