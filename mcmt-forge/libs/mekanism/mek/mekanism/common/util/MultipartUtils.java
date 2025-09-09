package mekanism.common.util;

import java.util.Collection;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class MultipartUtils {
   private MultipartUtils() {
   }

   public static MultipartUtils.RayTraceVectors getRayTraceVectors(Entity entity) {
      float pitch = entity.m_146909_();
      float yaw = entity.m_146908_();
      Vec3 start = entity.m_146892_();
      float f1 = Mth.m_14089_(-yaw * (float) (Math.PI / 180.0) - (float) Math.PI);
      float f2 = Mth.m_14031_(-yaw * (float) (Math.PI / 180.0) - (float) Math.PI);
      float f3 = -Mth.m_14089_(-pitch * (float) (Math.PI / 180.0));
      float lookY = Mth.m_14031_(-pitch * (float) (Math.PI / 180.0));
      float lookX = f2 * f3;
      float lookZ = f1 * f3;
      double reach = 5.0;
      if (entity instanceof Player player) {
         reach = player.getBlockReach();
      }

      Vec3 end = start.m_82520_(lookX * reach, lookY * reach, lookZ * reach);
      return new MultipartUtils.RayTraceVectors(start, end);
   }

   public static MultipartUtils.AdvancedRayTraceResult collisionRayTrace(Entity entity, BlockPos pos, Collection<VoxelShape> boxes) {
      MultipartUtils.RayTraceVectors vecs = getRayTraceVectors(entity);
      return collisionRayTrace(pos, vecs.start(), vecs.end(), boxes);
   }

   public static MultipartUtils.AdvancedRayTraceResult collisionRayTrace(BlockPos pos, Vec3 start, Vec3 end, Collection<VoxelShape> boxes) {
      double minDistance = Double.POSITIVE_INFINITY;
      MultipartUtils.AdvancedRayTraceResult hit = null;
      int i = -1;

      for (VoxelShape shape : boxes) {
         if (shape != null) {
            BlockHitResult result = shape.m_83220_(start, end, pos);
            if (result != null) {
               MultipartUtils.AdvancedRayTraceResult advancedResult = new MultipartUtils.AdvancedRayTraceResult(result, shape, i);
               double d = advancedResult.squareDistanceTo(start);
               if (d < minDistance) {
                  minDistance = d;
                  hit = advancedResult;
               }
            }
         }

         i++;
      }

      return hit;
   }

   public static class AdvancedRayTraceResult {
      public final VoxelShape bounds;
      public final HitResult hit;
      public final int subHit;

      public AdvancedRayTraceResult(HitResult mop, VoxelShape shape, int subHit) {
         this.hit = mop;
         this.bounds = shape;
         this.subHit = subHit;
      }

      public boolean valid() {
         return this.hit != null && this.bounds != null;
      }

      public double squareDistanceTo(Vec3 vec) {
         return this.hit.m_82450_().m_82557_(vec);
      }
   }

   public record RayTraceVectors(Vec3 start, Vec3 end) {
   }
}
