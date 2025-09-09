package mekanism.common.content.gear;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import mekanism.common.content.gear.mekatool.ModuleVeinMiningUnit;
import mekanism.common.util.MultipartUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public interface IBlastingItem {
   Map<BlockPos, BlockState> getBlastedBlocks(Level world, Player player, ItemStack stack, BlockPos pos, BlockState state);

   static Map<BlockPos, BlockState> findPositions(Level world, BlockPos targetPos, Player player, int radius) {
      if (radius > 0) {
         Direction targetSide = getTargetSide(world, targetPos, player);
         if (targetSide != null) {
            Vec3i lower;
            Vec3i upper;
            switch (targetSide) {
               case UP:
               case DOWN:
                  lower = new Vec3i(-radius, 0, -radius);
                  upper = new Vec3i(radius, 0, radius);
                  break;
               case EAST:
               case WEST:
                  lower = new Vec3i(0, -1, -radius);
                  upper = new Vec3i(0, 2 * radius - 1, radius);
                  break;
               case NORTH:
               case SOUTH:
                  lower = new Vec3i(-radius, -1, 0);
                  upper = new Vec3i(radius, 2 * radius - 1, 0);
                  break;
               default:
                  lower = new Vec3i(0, 0, 0);
                  upper = new Vec3i(0, 0, 0);
            }

            Map<BlockPos, BlockState> found = new HashMap<>();

            for (BlockPos nextPos : BlockPos.m_121940_(targetPos.m_121955_(lower), targetPos.m_121955_(upper))) {
               BlockState nextState = world.m_8055_(nextPos);
               if (canBlastBlock(world, nextPos, nextState)) {
                  found.put(nextPos.m_7949_(), nextState);
               }
            }

            return found;
         }
      }

      return Collections.emptyMap();
   }

   @Nullable
   private static Direction getTargetSide(Level world, BlockPos targetPos, Player player) {
      MultipartUtils.RayTraceVectors rayTraceVectors = MultipartUtils.getRayTraceVectors(player);
      Vec3 start = rayTraceVectors.start();
      Vec3 end = rayTraceVectors.end();
      Vec3 distance = end.m_82546_(start);
      if (distance.m_82556_() < 1.0E-7) {
         return null;
      } else {
         BlockState targetState = world.m_8055_(targetPos);
         VoxelShape shape = targetState.m_60651_(world, targetPos, CollisionContext.m_82750_(player));
         if (!shape.m_83281_()) {
            AABB bounds = shape.m_83215_();
            double[] ignoredMinDistance = new double[]{1.0};
            return AABB.m_82325_(
               bounds.m_82338_(targetPos),
               start,
               ignoredMinDistance,
               null,
               end.f_82479_ - start.f_82479_,
               end.f_82480_ - start.f_82480_,
               end.f_82481_ - start.f_82481_
            );
         } else {
            return null;
         }
      }
   }

   static boolean canBlastBlock(Level world, BlockPos pos, BlockState state) {
      return !state.m_60795_() && !state.m_278721_() && ModuleVeinMiningUnit.canVeinBlock(state) && state.m_60800_(world, pos) > 0.0F;
   }
}
