package mekanism.client.render.lib;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import java.util.Arrays;
import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.lib.Color;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.pipeline.QuadBakingVertexConsumer.Buffered;
import org.joml.Vector3f;

public class Quad {
   private final Vertex[] vertices;
   private Direction side;
   private TextureAtlasSprite sprite;
   private int tintIndex;
   private boolean shade;
   private boolean hasAmbientOcclusion;

   public Quad(TextureAtlasSprite sprite, Direction side, Vertex[] vertices) {
      this(sprite, side, vertices, -1, false, true);
   }

   public Quad(TextureAtlasSprite sprite, Direction side, Vertex[] vertices, int tintIndex, boolean shade, boolean hasAmbientOcclusion) {
      this.sprite = sprite;
      this.side = side;
      this.vertices = vertices;
      this.tintIndex = tintIndex;
      this.shade = shade;
      this.hasAmbientOcclusion = hasAmbientOcclusion;
   }

   public Quad(BakedQuad quad) {
      this.vertices = new Vertex[4];
      this.side = quad.m_111306_();
      this.sprite = quad.m_173410_();
      this.tintIndex = quad.m_111305_();
      this.shade = quad.m_111307_();
      this.hasAmbientOcclusion = quad.hasAmbientOcclusion();
      new Quad.BakedQuadUnpacker().putBulkData(new PoseStack().m_85850_(), quad, 1.0F, 1.0F, 1.0F, 1.0F, 0, OverlayTexture.f_118083_, true);
   }

   public TextureAtlasSprite getTexture() {
      return this.sprite;
   }

   public void setTexture(TextureAtlasSprite sprite) {
      this.sprite = sprite;
   }

   public int getTint() {
      return this.tintIndex;
   }

   public void setTint(int tintIndex) {
      this.tintIndex = tintIndex;
   }

   public void vertexTransform(Consumer<Vertex> transformation) {
      for (Vertex v : this.vertices) {
         transformation.accept(v);
      }
   }

   public boolean transform(QuadTransformation... transformations) {
      boolean transformed = false;

      for (QuadTransformation transform : transformations) {
         transformed |= transform.transform(this);
      }

      return transformed;
   }

   public Vertex[] getVertices() {
      return this.vertices;
   }

   public void setSide(Direction side) {
      this.side = side;
   }

   public Direction getSide() {
      return this.side;
   }

   public boolean isShade() {
      return this.shade;
   }

   public void setShade(boolean shade) {
      this.shade = shade;
   }

   public boolean hasAmbientOcclusion() {
      return this.hasAmbientOcclusion;
   }

   public void setHasAmbientOcclusion(boolean hasAmbientOcclusion) {
      this.hasAmbientOcclusion = hasAmbientOcclusion;
   }

   public BakedQuad bake() {
      Buffered quadBaker = new Buffered();
      quadBaker.setSprite(this.sprite);
      quadBaker.setDirection(this.side);
      quadBaker.setTintIndex(this.tintIndex);
      quadBaker.setShade(this.shade);
      quadBaker.setHasAmbientOcclusion(this.hasAmbientOcclusion);

      for (Vertex vertex : this.vertices) {
         vertex.write(quadBaker);
      }

      return quadBaker.getQuad();
   }

   public Quad copy() {
      Vertex[] newVertices = new Vertex[this.vertices.length];

      for (int i = 0; i < newVertices.length; i++) {
         newVertices[i] = this.vertices[i].copy(true);
      }

      return new Quad(this.sprite, this.side, newVertices, this.tintIndex, this.shade, this.hasAmbientOcclusion);
   }

   public Quad flip() {
      Vertex[] flipped = new Vertex[this.vertices.length];

      for (int i = 0; i < flipped.length; i++) {
         flipped[i] = this.vertices[i].flip();
      }

      return new Quad(this.sprite, this.side.m_122424_(), flipped, this.tintIndex, this.shade, this.hasAmbientOcclusion);
   }

   @NothingNullByDefault
   private class BakedQuadUnpacker implements VertexConsumer {
      private Vertex vertex = new Vertex();
      private int vertexIndex = 0;

      public VertexConsumer m_5483_(double x, double y, double z) {
         this.vertex.pos(new Vec3(x, y, z));
         return this;
      }

      public VertexConsumer m_6122_(int red, int green, int blue, int alpha) {
         this.vertex.color(red, green, blue, alpha);
         return this;
      }

      public VertexConsumer m_7421_(float u, float v) {
         this.vertex.texRaw(u, v);
         return this;
      }

      public VertexConsumer m_7122_(int u, int v) {
         this.vertex.overlay(u, v);
         return this;
      }

      public VertexConsumer m_7120_(int u, int v) {
         this.vertex.lightRaw(u, v);
         return this;
      }

      public VertexConsumer m_5601_(float x, float y, float z) {
         this.vertex.normal(x, y, z);
         return this;
      }

      public void m_5752_() {
         if (this.vertexIndex != Quad.this.vertices.length) {
            Quad.this.vertices[this.vertexIndex++] = this.vertex;
            this.vertex = new Vertex();
         }
      }

      public void m_7404_(int red, int green, int blue, int alpha) {
      }

      public void m_141991_() {
      }

      public VertexConsumer misc(VertexFormatElement element, int... rawData) {
         this.vertex.misc(element, Arrays.copyOf(rawData, rawData.length));
         return this;
      }
   }

   public static class Builder {
      private TextureAtlasSprite texture;
      private final Direction side;
      private Color color = Color.WHITE;
      private Vec3 vec1;
      private Vec3 vec2;
      private Vec3 vec3;
      private Vec3 vec4;
      private float minU;
      private float minV;
      private float maxU;
      private float maxV;
      private int lightU;
      private int lightV;
      private int tintIndex = -1;
      private boolean shade;
      private boolean hasAmbientOcclusion = true;
      private boolean contractUVs = true;

      public Builder(TextureAtlasSprite texture, Direction side) {
         this.texture = texture;
         this.side = side;
      }

      public Quad.Builder light(int light) {
         return this.light(LightTexture.m_109883_(light), LightTexture.m_109894_(light));
      }

      public Quad.Builder light(int u, int v) {
         this.lightU = u;
         this.lightV = v;
         return this;
      }

      public Quad.Builder uv(float minU, float minV, float maxU, float maxV) {
         this.minU = minU;
         this.minV = minV;
         this.maxU = maxU;
         this.maxV = maxV;
         return this;
      }

      public Quad.Builder tex(TextureAtlasSprite texture) {
         this.texture = texture;
         return this;
      }

      public Quad.Builder tint(int tintIndex) {
         this.tintIndex = tintIndex;
         return this;
      }

      public Quad.Builder color(Color color) {
         this.color = color;
         return this;
      }

      public Quad.Builder setShade(boolean shade) {
         this.shade = shade;
         return this;
      }

      public Quad.Builder setHasAmbientOcclusion(boolean hasAmbientOcclusion) {
         this.hasAmbientOcclusion = hasAmbientOcclusion;
         return this;
      }

      public Quad.Builder contractUVs(boolean contractUVs) {
         this.contractUVs = contractUVs;
         return this;
      }

      public Quad.Builder pos(Vec3 tl, Vec3 bl, Vec3 br, Vec3 tr) {
         this.vec1 = tl;
         this.vec2 = bl;
         this.vec3 = br;
         this.vec4 = tr;
         return this;
      }

      public Quad.Builder rect(Vec3 start, double width, double height) {
         return this.rect(start, width, height, 0.0625);
      }

      public Quad.Builder rect(Vec3 start, double width, double height, double scale) {
         start = start.m_82490_(scale);
         Vec3 end;
         if (this.side.m_122434_().m_122479_()) {
            Vec3i normal = this.side.m_122436_();
            end = start.m_82520_(normal.m_123343_() * width * scale, 0.0, normal.m_123341_() * width * scale);
            if (this.side.m_122434_() == Axis.X) {
               return this.pos(start, start.m_82520_(0.0, height * scale, 0.0), end.m_82520_(0.0, height * scale, 0.0), end);
            }
         } else {
            end = start.m_82520_(width * scale, 0.0, 0.0);
         }

         return this.pos(start.m_82520_(0.0, height * scale, 0.0), start, end, end.m_82520_(0.0, height * scale, 0.0));
      }

      public Quad build() {
         Vertex[] vertices = new Vertex[4];
         Vector3f normal = this.vec3.m_82546_(this.vec2).m_82537_(this.vec1.m_82546_(this.vec2)).m_82541_().m_252839_();
         vertices[0] = Vertex.create(this.vec1, normal, this.color, this.texture, this.minU, this.minV).light(this.lightU, this.lightV);
         vertices[1] = Vertex.create(this.vec2, normal, this.color, this.texture, this.minU, this.maxV).light(this.lightU, this.lightV);
         vertices[2] = Vertex.create(this.vec3, normal, this.color, this.texture, this.maxU, this.maxV).light(this.lightU, this.lightV);
         vertices[3] = Vertex.create(this.vec4, normal, this.color, this.texture, this.maxU, this.minV).light(this.lightU, this.lightV);
         Quad quad = new Quad(this.texture, this.side, vertices, this.tintIndex, this.shade, this.hasAmbientOcclusion);
         if (this.contractUVs) {
            QuadUtils.contractUVs(quad);
         }

         return quad;
      }
   }
}
