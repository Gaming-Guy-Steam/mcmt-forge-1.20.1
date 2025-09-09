package mekanism.common.lib.math;

import java.util.Random;
import mekanism.common.lib.math.voxel.VoxelCuboid;
import net.minecraft.world.phys.Vec3;

public record Plane(Vec3 minPos, Vec3 maxPos) {
   public static Plane getInnerCuboidPlane(VoxelCuboid cuboid, VoxelCuboid.CuboidSide side) {
      int minX = cuboid.getMinPos().m_123341_() + 1;
      int minY = cuboid.getMinPos().m_123342_() + 1;
      int minZ = cuboid.getMinPos().m_123343_() + 1;
      int maxX = cuboid.getMaxPos().m_123341_();
      int maxY = cuboid.getMaxPos().m_123342_();
      int maxZ = cuboid.getMaxPos().m_123343_();

      return switch (side) {
         case NORTH -> new Plane(new Vec3(minX, minY, minZ), new Vec3(maxX, maxY, minZ));
         case SOUTH -> new Plane(new Vec3(minX, minY, maxZ), new Vec3(maxX, maxY, maxZ));
         case WEST -> new Plane(new Vec3(minX, minY, minZ), new Vec3(minX, maxY, maxZ));
         case EAST -> new Plane(new Vec3(maxX, minY, minZ), new Vec3(maxX, maxY, maxZ));
         case BOTTOM -> new Plane(new Vec3(minX, minY, minZ), new Vec3(maxX, minY, maxZ));
         case TOP -> new Plane(new Vec3(minX, maxY, minZ), new Vec3(maxX, maxY, maxZ));
      };
   }

   public Vec3 getRandomPoint(Random rand) {
      return new Vec3(
         this.minPos.f_82479_ + rand.nextDouble() * (this.maxPos.f_82479_ - this.minPos.f_82479_),
         this.minPos.f_82480_ + rand.nextDouble() * (this.maxPos.f_82480_ - this.minPos.f_82480_),
         this.minPos.f_82481_ + rand.nextDouble() * (this.maxPos.f_82481_ - this.minPos.f_82481_)
      );
   }
}
