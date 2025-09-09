package mekanism.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import java.util.Arrays;
import mekanism.common.util.EnumUtils;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.util.Mth;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class RenderResizableCuboid {
   private static final int[] combinedARGB = new int[EnumUtils.DIRECTIONS.length];
   private static final Vector3f NORMAL = (Vector3f)Util.m_137469_(new Vector3f(1.0F, 1.0F, 1.0F), Vector3f::normalize);
   private static final int X_AXIS_MASK = 1 << Axis.X.ordinal();
   private static final int Y_AXIS_MASK = 1 << Axis.Y.ordinal();
   private static final int Z_AXIS_MASK = 1 << Axis.Z.ordinal();

   private RenderResizableCuboid() {
   }

   public static void renderCube(
      MekanismRenderer.Model3D cube,
      PoseStack matrix,
      VertexConsumer buffer,
      int argb,
      int light,
      int overlay,
      RenderResizableCuboid.FaceDisplay faceDisplay,
      Camera camera,
      @Nullable Vec3 renderPos
   ) {
      Arrays.fill(combinedARGB, argb);
      renderCube(cube, matrix, buffer, combinedARGB, light, overlay, faceDisplay, camera, renderPos);
   }

   public static void renderCube(
      MekanismRenderer.Model3D cube,
      PoseStack matrix,
      VertexConsumer buffer,
      int[] colors,
      int light,
      int overlay,
      RenderResizableCuboid.FaceDisplay faceDisplay,
      Camera camera,
      @Nullable Vec3 renderPos
   ) {
      MekanismRenderer.Model3D.SpriteInfo[] sprites = new MekanismRenderer.Model3D.SpriteInfo[6];
      int axisToRender = 0;
      if (renderPos != null && faceDisplay != RenderResizableCuboid.FaceDisplay.BOTH) {
         Vec3 camPos = camera.m_90583_();
         Vec3 minPos = renderPos.m_82520_(cube.minX, cube.minY, cube.minZ);
         Vec3 maxPos = renderPos.m_82520_(cube.maxX, cube.maxY, cube.maxZ);

         for (Direction direction : EnumUtils.DIRECTIONS) {
            MekanismRenderer.Model3D.SpriteInfo sprite = cube.getSpriteToRender(direction);
            if (sprite != null) {
               Axis axis = direction.m_122434_();
               AxisDirection axisDirection = direction.m_122421_();

               double planeLocation = switch (axisDirection) {
                  case POSITIVE -> axis.m_6150_(maxPos.f_82479_, maxPos.f_82480_, maxPos.f_82481_);
                  case NEGATIVE -> axis.m_6150_(minPos.f_82479_, minPos.f_82480_, minPos.f_82481_);
                  default -> throw new IncompatibleClassChangeError();
               };
               double cameraPosition = axis.m_6150_(camPos.f_82479_, camPos.f_82480_, camPos.f_82481_);
               if (faceDisplay.front == (axisDirection == AxisDirection.POSITIVE)) {
                  if (cameraPosition >= planeLocation) {
                     sprites[direction.ordinal()] = sprite;
                     axisToRender |= 1 << axis.ordinal();
                  }
               } else if (cameraPosition <= planeLocation) {
                  sprites[direction.ordinal()] = sprite;
                  axisToRender |= 1 << axis.ordinal();
               }
            }
         }
      } else {
         for (Direction directionx : EnumUtils.DIRECTIONS) {
            MekanismRenderer.Model3D.SpriteInfo sprite = cube.getSpriteToRender(directionx);
            if (sprite != null) {
               sprites[directionx.ordinal()] = sprite;
               axisToRender |= 1 << directionx.m_122434_().ordinal();
            }
         }
      }

      if (axisToRender != 0) {
         int xShift = Mth.m_14143_(cube.minX);
         int yShift = Mth.m_14143_(cube.minY);
         int zShift = Mth.m_14143_(cube.minZ);
         float minX = cube.minX - xShift;
         float minY = cube.minY - yShift;
         float minZ = cube.minZ - zShift;
         float maxX = cube.maxX - xShift;
         float maxY = cube.maxY - yShift;
         float maxZ = cube.maxZ - zShift;
         int xDelta = calculateDelta(minX, maxX);
         int yDelta = calculateDelta(minY, maxY);
         int zDelta = calculateDelta(minZ, maxZ);
         float[] xBounds = getBlockBounds(xDelta, minX, maxX);
         float[] yBounds = getBlockBounds(yDelta, minY, maxY);
         float[] zBounds = getBlockBounds(zDelta, minZ, maxZ);
         matrix.m_85836_();
         matrix.m_252880_(xShift, yShift, zShift);
         Pose lastMatrix = matrix.m_85850_();
         Matrix4f matrix4f = lastMatrix.m_252922_();
         RenderResizableCuboid.NormalData normal = new RenderResizableCuboid.NormalData(lastMatrix.m_252943_(), NORMAL, faceDisplay);
         Vector3f from = new Vector3f();
         Vector3f to = new Vector3f();
         int xIncrement = 1;
         int yIncrement = 1;
         int zIncrement = 1;
         if (axisToRender == X_AXIS_MASK) {
            xIncrement = Math.max(xDelta, 1);
         } else if (axisToRender == Y_AXIS_MASK) {
            yIncrement = Math.max(yDelta, 1);
         } else if (axisToRender == Z_AXIS_MASK) {
            zIncrement = Math.max(zDelta, 1);
         }

         for (int y = 0; y <= yDelta; y += yIncrement) {
            MekanismRenderer.Model3D.SpriteInfo upSprite = y == yDelta ? sprites[Direction.UP.ordinal()] : null;
            MekanismRenderer.Model3D.SpriteInfo downSprite = y == 0 ? sprites[Direction.DOWN.ordinal()] : null;
            from.y = yBounds[y];
            to.y = yBounds[y + 1];

            for (int z = 0; z <= zDelta; z += zIncrement) {
               MekanismRenderer.Model3D.SpriteInfo northSprite = z == 0 ? sprites[Direction.NORTH.ordinal()] : null;
               MekanismRenderer.Model3D.SpriteInfo southSprite = z == zDelta ? sprites[Direction.SOUTH.ordinal()] : null;
               from.z = zBounds[z];
               to.z = zBounds[z + 1];

               for (int x = 0; x <= xDelta; x += xIncrement) {
                  MekanismRenderer.Model3D.SpriteInfo westSprite = x == 0 ? sprites[Direction.WEST.ordinal()] : null;
                  MekanismRenderer.Model3D.SpriteInfo eastSprite = x == xDelta ? sprites[Direction.EAST.ordinal()] : null;
                  from.x = xBounds[x];
                  to.x = xBounds[x + 1];
                  putTexturedQuad(buffer, matrix4f, westSprite, from, to, Direction.WEST, colors, light, overlay, faceDisplay, normal);
                  putTexturedQuad(buffer, matrix4f, eastSprite, from, to, Direction.EAST, colors, light, overlay, faceDisplay, normal);
                  putTexturedQuad(buffer, matrix4f, northSprite, from, to, Direction.NORTH, colors, light, overlay, faceDisplay, normal);
                  putTexturedQuad(buffer, matrix4f, southSprite, from, to, Direction.SOUTH, colors, light, overlay, faceDisplay, normal);
                  putTexturedQuad(buffer, matrix4f, upSprite, from, to, Direction.UP, colors, light, overlay, faceDisplay, normal);
                  putTexturedQuad(buffer, matrix4f, downSprite, from, to, Direction.DOWN, colors, light, overlay, faceDisplay, normal);
               }
            }
         }

         matrix.m_85849_();
      }
   }

   private static float[] getBlockBounds(int delta, float start, float end) {
      float[] bounds = new float[2 + delta];
      bounds[0] = start;
      int offset = (int)start;

      for (int i = 1; i <= delta; i++) {
         bounds[i] = i + offset;
      }

      bounds[delta + 1] = end;
      return bounds;
   }

   private static int calculateDelta(float min, float max) {
      int delta = (int)(max - (int)min);
      if (max % 1.0 == 0.0) {
         delta--;
      }

      return delta;
   }

   private static void putTexturedQuad(
      VertexConsumer buffer,
      Matrix4f matrix,
      @Nullable MekanismRenderer.Model3D.SpriteInfo spriteInfo,
      Vector3f from,
      Vector3f to,
      Direction face,
      int[] colors,
      int light,
      int overlay,
      RenderResizableCuboid.FaceDisplay faceDisplay,
      RenderResizableCuboid.NormalData normal
   ) {
      if (spriteInfo != null) {
         float x1 = from.x();
         float y1 = from.y();
         float z1 = from.z();
         float x2 = to.x();
         float y2 = to.y();
         float z2 = to.z();
         RenderResizableCuboid.Bounds uBounds;
         RenderResizableCuboid.Bounds vBounds;
         switch (face.m_122434_()) {
            case Z:
               uBounds = RenderResizableCuboid.Bounds.calculate(x2, x1);
               vBounds = RenderResizableCuboid.Bounds.calculate(y1, y2);
               break;
            case X:
               uBounds = RenderResizableCuboid.Bounds.calculate(z2, z1);
               vBounds = RenderResizableCuboid.Bounds.calculate(y1, y2);
               break;
            default:
               uBounds = RenderResizableCuboid.Bounds.calculate(x1, x2);
               vBounds = RenderResizableCuboid.Bounds.calculate(z2, z1);
         }

         float minU = spriteInfo.getU(uBounds.min());
         float maxU = spriteInfo.getU(uBounds.max());
         float minV = spriteInfo.getV(1.0F - vBounds.max());
         float maxV = spriteInfo.getV(1.0F - vBounds.min());
         int argb = colors[face.ordinal()];
         switch (face) {
            case DOWN:
               drawFace(buffer, matrix, argb, minU, maxU, minV, maxV, light, overlay, faceDisplay, normal, x1, y1, z2, x1, y1, z1, x2, y1, z1, x2, y1, z2);
               break;
            case UP:
               drawFace(buffer, matrix, argb, minU, maxU, minV, maxV, light, overlay, faceDisplay, normal, x1, y2, z1, x1, y2, z2, x2, y2, z2, x2, y2, z1);
               break;
            case NORTH:
               drawFace(buffer, matrix, argb, minU, maxU, minV, maxV, light, overlay, faceDisplay, normal, x1, y1, z1, x1, y2, z1, x2, y2, z1, x2, y1, z1);
               break;
            case SOUTH:
               drawFace(buffer, matrix, argb, minU, maxU, minV, maxV, light, overlay, faceDisplay, normal, x2, y1, z2, x2, y2, z2, x1, y2, z2, x1, y1, z2);
               break;
            case WEST:
               drawFace(buffer, matrix, argb, minU, maxU, minV, maxV, light, overlay, faceDisplay, normal, x1, y1, z2, x1, y2, z2, x1, y2, z1, x1, y1, z1);
               break;
            case EAST:
               drawFace(buffer, matrix, argb, minU, maxU, minV, maxV, light, overlay, faceDisplay, normal, x2, y1, z1, x2, y2, z1, x2, y2, z2, x2, y1, z2);
         }
      }
   }

   private static void drawFace(
      VertexConsumer buffer,
      Matrix4f matrix,
      int argb,
      float minU,
      float maxU,
      float minV,
      float maxV,
      int light,
      int overlay,
      RenderResizableCuboid.FaceDisplay faceDisplay,
      RenderResizableCuboid.NormalData normal,
      float x1,
      float y1,
      float z1,
      float x2,
      float y2,
      float z2,
      float x3,
      float y3,
      float z3,
      float x4,
      float y4,
      float z4
   ) {
      int red = ARGB32.m_13665_(argb);
      int green = ARGB32.m_13667_(argb);
      int blue = ARGB32.m_13669_(argb);
      int alpha = ARGB32.m_13655_(argb);
      if (faceDisplay.front) {
         buffer.m_252986_(matrix, x1, y1, z1)
            .m_6122_(red, green, blue, alpha)
            .m_7421_(minU, maxV)
            .m_86008_(overlay)
            .m_85969_(light)
            .m_5601_(normal.front.x(), normal.front.y(), normal.front.z())
            .m_5752_();
         buffer.m_252986_(matrix, x2, y2, z2)
            .m_6122_(red, green, blue, alpha)
            .m_7421_(minU, minV)
            .m_86008_(overlay)
            .m_85969_(light)
            .m_5601_(normal.front.x(), normal.front.y(), normal.front.z())
            .m_5752_();
         buffer.m_252986_(matrix, x3, y3, z3)
            .m_6122_(red, green, blue, alpha)
            .m_7421_(maxU, minV)
            .m_86008_(overlay)
            .m_85969_(light)
            .m_5601_(normal.front.x(), normal.front.y(), normal.front.z())
            .m_5752_();
         buffer.m_252986_(matrix, x4, y4, z4)
            .m_6122_(red, green, blue, alpha)
            .m_7421_(maxU, maxV)
            .m_86008_(overlay)
            .m_85969_(light)
            .m_5601_(normal.front.x(), normal.front.y(), normal.front.z())
            .m_5752_();
      }

      if (faceDisplay.back) {
         buffer.m_252986_(matrix, x4, y4, z4)
            .m_6122_(red, green, blue, alpha)
            .m_7421_(maxU, maxV)
            .m_86008_(overlay)
            .m_85969_(light)
            .m_5601_(normal.back.x(), normal.back.y(), normal.back.z())
            .m_5752_();
         buffer.m_252986_(matrix, x3, y3, z3)
            .m_6122_(red, green, blue, alpha)
            .m_7421_(maxU, minV)
            .m_86008_(overlay)
            .m_85969_(light)
            .m_5601_(normal.back.x(), normal.back.y(), normal.back.z())
            .m_5752_();
         buffer.m_252986_(matrix, x2, y2, z2)
            .m_6122_(red, green, blue, alpha)
            .m_7421_(minU, minV)
            .m_86008_(overlay)
            .m_85969_(light)
            .m_5601_(normal.back.x(), normal.back.y(), normal.back.z())
            .m_5752_();
         buffer.m_252986_(matrix, x1, y1, z1)
            .m_6122_(red, green, blue, alpha)
            .m_7421_(minU, maxV)
            .m_86008_(overlay)
            .m_85969_(light)
            .m_5601_(normal.back.x(), normal.back.y(), normal.back.z())
            .m_5752_();
      }
   }

   private record Bounds(float min, float max) {
      public static RenderResizableCuboid.Bounds calculate(float min, float max) {
         boolean bigger = min > max;
         min %= 1.0F;
         max %= 1.0F;
         return bigger ? new RenderResizableCuboid.Bounds(min == 0.0F ? 1.0F : min, max) : new RenderResizableCuboid.Bounds(min, max == 0.0F ? 1.0F : max);
      }
   }

   public static enum FaceDisplay {
      FRONT(true, false),
      BACK(false, true),
      BOTH(true, true);

      private final boolean front;
      private final boolean back;

      private FaceDisplay(boolean front, boolean back) {
         this.front = front;
         this.back = back;
      }
   }

   private record NormalData(Vector3f front, Vector3f back) {
      private NormalData(Matrix3f normalMatrix, Vector3f normal, RenderResizableCuboid.FaceDisplay faceDisplay) {
         this(
            faceDisplay.front ? calculate(normalMatrix, normal.x(), normal.y(), normal.z()) : new Vector3f(),
            faceDisplay.back ? calculate(normalMatrix, -normal.x(), -normal.y(), -normal.z()) : new Vector3f()
         );
      }

      private static Vector3f calculate(Matrix3f normalMatrix, float x, float y, float z) {
         Vector3f matrixAdjustedNormal = new Vector3f(x, y, z);
         return matrixAdjustedNormal.mul(normalMatrix);
      }
   }
}
