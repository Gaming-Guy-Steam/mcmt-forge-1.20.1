package mekanism.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;
import mekanism.common.Mekanism;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class VoxelShapeUtils {
   private static final Vec3 fromOrigin = new Vec3(-0.5, -0.5, -0.5);

   public static void print(double x1, double y1, double z1, double x2, double y2, double z2) {
      Mekanism.logger
         .info(
            "box({}, {}, {}, {}, {}, {}),",
            new Object[]{Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2), Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2)}
         );
   }

   public static void printSimplified(String name, VoxelShape shape) {
      Mekanism.logger.info("Simplified: {}", name);
      shape.m_83296_()
         .m_83299_()
         .forEach(box -> print(box.f_82288_ * 16.0, box.f_82289_ * 16.0, box.f_82290_ * 16.0, box.f_82291_ * 16.0, box.f_82292_ * 16.0, box.f_82293_ * 16.0));
   }

   public static AABB rotate(AABB box, Direction side) {
      return switch (side) {
         case DOWN -> box;
         case UP -> new AABB(box.f_82288_, -box.f_82289_, -box.f_82290_, box.f_82291_, -box.f_82292_, -box.f_82293_);
         case NORTH -> new AABB(box.f_82288_, -box.f_82290_, box.f_82289_, box.f_82291_, -box.f_82293_, box.f_82292_);
         case SOUTH -> new AABB(-box.f_82288_, -box.f_82290_, -box.f_82289_, -box.f_82291_, -box.f_82293_, -box.f_82292_);
         case WEST -> new AABB(box.f_82289_, -box.f_82290_, -box.f_82288_, box.f_82292_, -box.f_82293_, -box.f_82291_);
         case EAST -> new AABB(-box.f_82289_, -box.f_82290_, box.f_82288_, -box.f_82292_, -box.f_82293_, box.f_82291_);
         default -> throw new IncompatibleClassChangeError();
      };
   }

   public static AABB rotate(AABB box, Rotation rotation) {
      return switch (rotation) {
         case NONE -> box;
         case CLOCKWISE_90 -> new AABB(-box.f_82290_, box.f_82289_, box.f_82288_, -box.f_82293_, box.f_82292_, box.f_82291_);
         case CLOCKWISE_180 -> new AABB(-box.f_82288_, box.f_82289_, -box.f_82290_, -box.f_82291_, box.f_82292_, -box.f_82293_);
         case COUNTERCLOCKWISE_90 -> new AABB(box.f_82290_, box.f_82289_, -box.f_82288_, box.f_82293_, box.f_82292_, -box.f_82291_);
         default -> throw new IncompatibleClassChangeError();
      };
   }

   public static AABB rotateHorizontal(AABB box, Direction side) {
      return switch (side) {
         case NORTH -> rotate(box, Rotation.NONE);
         case SOUTH -> rotate(box, Rotation.CLOCKWISE_180);
         case WEST -> rotate(box, Rotation.COUNTERCLOCKWISE_90);
         case EAST -> rotate(box, Rotation.CLOCKWISE_90);
         default -> box;
      };
   }

   public static VoxelShape rotate(VoxelShape shape, Direction side) {
      return rotate(shape, (UnaryOperator<AABB>)(box -> rotate(box, side)));
   }

   public static VoxelShape rotate(VoxelShape shape, Rotation rotation) {
      return rotate(shape, (UnaryOperator<AABB>)(box -> rotate(box, rotation)));
   }

   public static VoxelShape rotateHorizontal(VoxelShape shape, Direction side) {
      return rotate(shape, (UnaryOperator<AABB>)(box -> rotateHorizontal(box, side)));
   }

   public static VoxelShape rotate(VoxelShape shape, UnaryOperator<AABB> rotateFunction) {
      List<VoxelShape> rotatedPieces = new ArrayList<>();

      for (AABB sourceBoundingBox : shape.m_83299_()) {
         rotatedPieces.add(
            Shapes.m_83064_(
               rotateFunction.apply(sourceBoundingBox.m_82386_(fromOrigin.f_82479_, fromOrigin.f_82480_, fromOrigin.f_82481_))
                  .m_82386_(-fromOrigin.f_82479_, -fromOrigin.f_82481_, -fromOrigin.f_82481_)
            )
         );
      }

      return combine(rotatedPieces);
   }

   public static VoxelShape combine(VoxelShape... shapes) {
      return batchCombine(Shapes.m_83040_(), BooleanOp.f_82695_, true, shapes);
   }

   public static VoxelShape combine(Collection<VoxelShape> shapes) {
      return batchCombine(Shapes.m_83040_(), BooleanOp.f_82695_, true, shapes);
   }

   public static VoxelShape exclude(VoxelShape... shapes) {
      return batchCombine(Shapes.m_83144_(), BooleanOp.f_82685_, true, shapes);
   }

   public static VoxelShape batchCombine(VoxelShape initial, BooleanOp function, boolean simplify, Collection<VoxelShape> shapes) {
      VoxelShape combinedShape = initial;

      for (VoxelShape shape : shapes) {
         combinedShape = Shapes.m_83148_(combinedShape, shape, function);
      }

      return simplify ? combinedShape.m_83296_() : combinedShape;
   }

   public static VoxelShape batchCombine(VoxelShape initial, BooleanOp function, boolean simplify, VoxelShape... shapes) {
      VoxelShape combinedShape = initial;

      for (VoxelShape shape : shapes) {
         combinedShape = Shapes.m_83148_(combinedShape, shape, function);
      }

      return simplify ? combinedShape.m_83296_() : combinedShape;
   }

   public static void setShape(VoxelShape shape, VoxelShape[] dest, boolean verticalAxis) {
      setShape(shape, dest, verticalAxis, false);
   }

   public static void setShape(VoxelShape shape, VoxelShape[] dest, boolean verticalAxis, boolean invert) {
      Direction[] dirs = verticalAxis ? EnumUtils.DIRECTIONS : EnumUtils.HORIZONTAL_DIRECTIONS;

      for (Direction side : dirs) {
         dest[verticalAxis ? side.ordinal() : side.ordinal() - 2] = verticalAxis
            ? rotate(shape, invert ? side.m_122424_() : side)
            : rotateHorizontal(shape, side);
      }
   }

   public static void setShape(VoxelShape shape, VoxelShape[] dest) {
      setShape(shape, dest, false, false);
   }
}
