package mekanism.client.render.lib;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class QuadUtils {
   private static final float eps = 0.00390625F;

   private QuadUtils() {
   }

   public static List<Quad> unpack(List<BakedQuad> quads) {
      return quads.stream().map(Quad::new).toList();
   }

   public static List<BakedQuad> bake(List<Quad> quads) {
      return quads.stream().map(Quad::bake).toList();
   }

   public static List<Quad> flip(List<Quad> quads) {
      return quads.stream().map(Quad::flip).toList();
   }

   public static List<Quad> transformQuads(List<Quad> orig, QuadTransformation transformation) {
      List<Quad> list = new ArrayList<>(orig.size());

      for (Quad quad : orig) {
         transformation.transform(quad);
         list.add(quad);
      }

      return list;
   }

   public static List<BakedQuad> transformBakedQuads(List<BakedQuad> orig, QuadTransformation transformation) {
      List<BakedQuad> list = new ArrayList<>(orig.size());

      for (BakedQuad bakedQuad : orig) {
         Quad quad = new Quad(bakedQuad);
         if (transformation.transform(quad)) {
            list.add(quad.bake());
         } else {
            list.add(bakedQuad);
         }
      }

      return list;
   }

   public static List<BakedQuad> transformAndBake(List<Quad> orig, QuadTransformation transformation) {
      List<BakedQuad> list = new ArrayList<>(orig.size());

      for (Quad quad : orig) {
         transformation.transform(quad);
         list.add(quad.bake());
      }

      return list;
   }

   public static void remapUVs(Quad quad, TextureAtlasSprite newTexture) {
      TextureAtlasSprite texture = quad.getTexture();
      float uMin = texture.m_118409_();
      float uMax = texture.m_118410_();
      float vMin = texture.m_118411_();
      float vMax = texture.m_118412_();
      quad.vertexTransform(v -> {
         float newU = (v.getTexU() - uMin) * 16.0F / (uMax - uMin);
         float newV = (v.getTexV() - vMin) * 16.0F / (vMax - vMin);
         v.texRaw(newTexture.m_118367_(newU), newTexture.m_118393_(newV));
      });
   }

   public static void contractUVs(Quad quad) {
      TextureAtlasSprite texture = quad.getTexture();
      float sizeX = texture.m_245424_().m_246492_() / (texture.m_118410_() - texture.m_118409_());
      float sizeY = texture.m_245424_().m_245330_() / (texture.m_118412_() - texture.m_118411_());
      float ep = 1.0F / (Math.max(sizeX, sizeY) * 256.0F);
      float[] newUs = contract(quad, Vertex::getTexU, ep);
      float[] newVs = contract(quad, Vertex::getTexV, ep);

      for (int i = 0; i < quad.getVertices().length; i++) {
         quad.getVertices()[i].texRaw(newUs[i], newVs[i]);
      }
   }

   private static float[] contract(Quad quad, Function<Vertex, Float> uvf, float ep) {
      float center = 0.0F;
      float[] ret = new float[4];

      for (int v = 0; v < 4; v++) {
         center += uvf.apply(quad.getVertices()[v]);
      }

      center /= 4.0F;

      for (int v = 0; v < 4; v++) {
         float orig = uvf.apply(quad.getVertices()[v]);
         float shifted = orig * 0.99609375F + center * 0.00390625F;
         float delta = orig - shifted;
         if (Math.abs(delta) < ep) {
            float centerDelta = Math.abs(orig - center);
            if (centerDelta < 2.0F * ep) {
               shifted = (orig + center) / 2.0F;
            } else {
               shifted = orig + (delta < 0.0F ? ep : -ep);
            }
         }

         ret[v] = shifted;
      }

      return ret;
   }
}
