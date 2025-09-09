package mekanism.client.render.lib;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import mekanism.common.lib.Color;
import mekanism.common.lib.math.Quaternion;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public interface QuadTransformation {
   Direction[][] ROTATION_MATRIX = new Direction[][]{
      {Direction.SOUTH, Direction.NORTH, Direction.DOWN, Direction.DOWN, Direction.DOWN, Direction.DOWN},
      {Direction.NORTH, Direction.SOUTH, Direction.UP, Direction.UP, Direction.UP, Direction.UP},
      {Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST},
      {Direction.UP, Direction.DOWN, Direction.SOUTH, Direction.NORTH, Direction.EAST, Direction.WEST},
      {Direction.WEST, Direction.WEST, Direction.WEST, Direction.EAST, Direction.SOUTH, Direction.NORTH},
      {Direction.EAST, Direction.EAST, Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH}
   };
   QuadTransformation identity = q -> false;
   QuadTransformation fullbright = packedLight(15728880);
   QuadTransformation filtered_fullbright = QuadTransformation.TextureFilteredTransformation.of(fullbright, rl -> rl.m_135815_().contains("led"));

   static QuadTransformation color(Color color) {
      return new QuadTransformation.ColorTransformation(color);
   }

   static QuadTransformation light(int light) {
      return new QuadTransformation.LightTransformation(light, light);
   }

   static QuadTransformation packedLight(int light) {
      return new QuadTransformation.LightTransformation(LightTexture.m_109883_(light), LightTexture.m_109894_(light));
   }

   static QuadTransformation ambientShade(boolean ambientOcclusion, boolean shade) {
      return new QuadTransformation.AmbientShadeTransformation(ambientOcclusion, shade);
   }

   static QuadTransformation translate(double xTranslation, double yTranslation, double zTranslation) {
      return translate(new Vec3(xTranslation, yTranslation, zTranslation));
   }

   static QuadTransformation translate(Vec3 translation) {
      return new QuadTransformation.TranslationTransformation(translation);
   }

   static QuadTransformation rotate(Direction side) {
      if (side == null) {
         return identity;
      } else {
         return switch (side) {
            case UP -> rotate(90.0, 0.0, 0.0);
            case DOWN -> rotate(-90.0, 0.0, 0.0);
            case WEST -> rotate(0.0, 90.0, 0.0);
            case EAST -> rotate(0.0, -90.0, 0.0);
            case SOUTH -> rotate(0.0, 180.0, 0.0);
            default -> identity;
         };
      }
   }

   static QuadTransformation rotateY(double degrees) {
      return rotate(0.0, degrees, 0.0);
   }

   static QuadTransformation rotate(double rotationX, double rotationY, double rotationZ) {
      return rotate(new Quaternion(rotationX, rotationY, rotationZ, true));
   }

   static QuadTransformation rotate(Quaternion quat) {
      return new QuadTransformation.RotationTransformation(quat);
   }

   static QuadTransformation sideRotate(Direction side) {
      return new QuadTransformation.SideTransformation(side);
   }

   static QuadTransformation texture(TextureAtlasSprite texture) {
      return new QuadTransformation.TextureTransformation(texture);
   }

   static QuadTransformation uvShift(float uShift, float vShift) {
      return new QuadTransformation.UVTransformation(uShift, vShift);
   }

   static QuadTransformation list(QuadTransformation... transforms) {
      return QuadTransformation.TransformationList.of(transforms);
   }

   boolean transform(Quad quad);

   default QuadTransformation and(QuadTransformation other) {
      return list(this, other);
   }

   public static class AmbientShadeTransformation implements QuadTransformation {
      private final boolean ambientOcclusion;
      private final boolean shade;

      public AmbientShadeTransformation(boolean ambientOcclusion, boolean shade) {
         this.ambientOcclusion = ambientOcclusion;
         this.shade = shade;
      }

      @Override
      public boolean transform(Quad quad) {
         quad.setHasAmbientOcclusion(this.ambientOcclusion);
         quad.setShade(this.shade);
         return true;
      }

      @Override
      public boolean equals(Object o) {
         return o == this
            ? true
            : o instanceof QuadTransformation.AmbientShadeTransformation other && this.ambientOcclusion == other.ambientOcclusion && this.shade == other.shade;
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.ambientOcclusion, this.shade);
      }
   }

   public static class ColorTransformation implements QuadTransformation {
      private final Color color;

      protected ColorTransformation(Color color) {
         this.color = color;
      }

      @Override
      public boolean transform(Quad quad) {
         quad.vertexTransform(v -> v.color(this.color));
         return true;
      }

      @Override
      public boolean equals(Object o) {
         return o == this ? true : o instanceof QuadTransformation.ColorTransformation other && this.color.equals(other.color);
      }

      @Override
      public int hashCode() {
         return this.color.hashCode();
      }
   }

   public static class LightTransformation implements QuadTransformation {
      private final int lightU;
      private final int lightV;

      public LightTransformation(int lightU, int lightV) {
         this.lightU = lightU;
         this.lightV = lightV;
      }

      @Override
      public boolean transform(Quad quad) {
         quad.vertexTransform(v -> v.light(this.lightU, this.lightV));
         return true;
      }

      @Override
      public boolean equals(Object o) {
         return o == this ? true : o instanceof QuadTransformation.LightTransformation other && this.lightU == other.lightU && this.lightV == other.lightV;
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.lightU, this.lightV);
      }
   }

   public static class RotationTransformation implements QuadTransformation {
      private static final double EPSILON = 10000.0;
      private final Quaternion quaternion;

      protected RotationTransformation(Quaternion quaternion) {
         this.quaternion = quaternion;
      }

      @Override
      public boolean transform(Quad quad) {
         quad.vertexTransform(v -> {
            v.pos(round(this.quaternion.rotate(v.getPos().m_82492_(0.5, 0.5, 0.5)).m_82520_(0.5, 0.5, 0.5)));
            v.normal(round(this.quaternion.rotate(v.getNormalD()).normalize()));
         });
         return true;
      }

      private static Vec3 round(Vec3 vec) {
         return new Vec3(
            Math.round(vec.f_82479_ * 10000.0) / 10000.0, Math.round(vec.f_82480_ * 10000.0) / 10000.0, Math.round(vec.f_82481_ * 10000.0) / 10000.0
         );
      }

      @Override
      public boolean equals(Object o) {
         return o == this ? true : o instanceof QuadTransformation.RotationTransformation other && this.quaternion.equals(other.quaternion);
      }

      @Override
      public int hashCode() {
         return this.quaternion.hashCode();
      }
   }

   public static class SideTransformation implements QuadTransformation {
      private final Direction side;

      protected SideTransformation(Direction side) {
         this.side = side;
      }

      @Override
      public boolean transform(Quad quad) {
         if (this.side != null) {
            Direction newSide = ROTATION_MATRIX[quad.getSide().ordinal()][this.side.ordinal()];
            if (newSide != quad.getSide()) {
               quad.setSide(newSide);
               return true;
            }
         }

         return false;
      }

      @Override
      public boolean equals(Object o) {
         return o == this ? true : o instanceof QuadTransformation.SideTransformation other && this.side == other.side;
      }

      @Override
      public int hashCode() {
         return this.side == null ? -1 : this.side.hashCode();
      }
   }

   public static class TextureFilteredTransformation implements QuadTransformation {
      private final QuadTransformation original;
      private final Predicate<ResourceLocation> verifier;

      protected TextureFilteredTransformation(QuadTransformation original, Predicate<ResourceLocation> verifier) {
         this.original = original;
         this.verifier = verifier;
      }

      public static QuadTransformation.TextureFilteredTransformation of(QuadTransformation original, Predicate<ResourceLocation> verifier) {
         return new QuadTransformation.TextureFilteredTransformation(original, verifier);
      }

      @Override
      public boolean transform(Quad quad) {
         return this.verifier.test(quad.getTexture().m_245424_().m_246162_()) && this.original.transform(quad);
      }

      @Override
      public boolean equals(Object o) {
         return o == this ? true : o instanceof QuadTransformation.TextureFilteredTransformation other && this.verifier.equals(other.verifier);
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.original, this.verifier);
      }
   }

   public static class TextureTransformation implements QuadTransformation {
      private final TextureAtlasSprite texture;

      protected TextureTransformation(TextureAtlasSprite texture) {
         this.texture = texture;
      }

      @Override
      public boolean transform(Quad quad) {
         if (this.texture != null && quad.getTexture() != this.texture) {
            QuadUtils.remapUVs(quad, this.texture);
            quad.setTexture(this.texture);
            return true;
         } else {
            return false;
         }
      }

      @Override
      public boolean equals(Object o) {
         return o == this ? true : o instanceof QuadTransformation.TextureTransformation other && this.texture == other.texture;
      }

      @Override
      public int hashCode() {
         return this.texture == null ? -1 : this.texture.hashCode();
      }
   }

   public static class TransformationList implements QuadTransformation {
      private final List<QuadTransformation> list;
      private final int hashCode;

      protected TransformationList(List<QuadTransformation> list) {
         this.list = list;
         this.hashCode = list.hashCode();
      }

      public static QuadTransformation.TransformationList of(QuadTransformation... trans) {
         return new QuadTransformation.TransformationList(List.of(trans));
      }

      @Override
      public boolean transform(Quad quad) {
         boolean transformed = false;

         for (QuadTransformation transformation : this.list) {
            transformed |= transformation.transform(quad);
         }

         return transformed;
      }

      @Override
      public QuadTransformation and(QuadTransformation other) {
         List<QuadTransformation> newList = new ArrayList<>(this.list);
         newList.add(other);
         return new QuadTransformation.TransformationList(newList);
      }

      @Override
      public boolean equals(Object o) {
         return o == this ? true : o instanceof QuadTransformation.TransformationList other && this.list.equals(other.list);
      }

      @Override
      public int hashCode() {
         return this.hashCode;
      }
   }

   public static class TranslationTransformation implements QuadTransformation {
      private final Vec3 translation;

      protected TranslationTransformation(Vec3 translation) {
         this.translation = translation;
      }

      @Override
      public boolean transform(Quad quad) {
         quad.vertexTransform(v -> v.pos(v.getPos().m_82549_(this.translation)));
         return true;
      }

      @Override
      public boolean equals(Object o) {
         return o == this ? true : o instanceof QuadTransformation.TranslationTransformation other && this.translation.equals(other.translation);
      }

      @Override
      public int hashCode() {
         return this.translation.hashCode();
      }
   }

   public static class UVTransformation implements QuadTransformation {
      private final float uShift;
      private final float vShift;

      protected UVTransformation(float uShift, float vShift) {
         this.uShift = uShift;
         this.vShift = vShift;
      }

      @Override
      public boolean transform(Quad quad) {
         TextureAtlasSprite texture = quad.getTexture();
         float uMin = texture.m_118409_();
         float uMax = texture.m_118410_();
         float vMin = texture.m_118411_();
         float vMax = texture.m_118412_();
         float uShift = this.uShift * (uMax - uMin) / 16.0F;
         float vShift = this.vShift * (vMax - vMin) / 16.0F;
         quad.vertexTransform(v -> v.texRaw(v.getTexU() + uShift, v.getTexV() + vShift));
         return true;
      }

      @Override
      public boolean equals(Object o) {
         return o == this
            ? true
            : o instanceof QuadTransformation.UVTransformation other
               && Float.compare(other.uShift, this.uShift) == 0
               && Float.compare(other.vShift, this.vShift) == 0;
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.uShift, this.vShift);
      }
   }
}
