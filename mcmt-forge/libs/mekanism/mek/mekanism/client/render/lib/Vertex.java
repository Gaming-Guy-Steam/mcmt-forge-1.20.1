package mekanism.client.render.lib;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import mekanism.common.lib.Color;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class Vertex {
   private final Map<VertexFormatElement, int[]> miscData;
   private Vec3 pos;
   private Vector3f normal;
   private int red;
   private int green;
   private int blue;
   private int alpha;
   private float texU;
   private float texV;
   private int overlayU;
   private int overlayV;
   private int lightU;
   private int lightV;

   public Vertex() {
      this.miscData = new HashMap<>();
   }

   public Vertex(Vec3 pos, Vector3f normal, Color color, float texU, float texV, int overlayU, int overlayV, int lightU, int lightV) {
      this(pos, normal, color.r(), color.g(), color.b(), color.a(), texU, texV, overlayU, overlayV, lightU, lightV);
   }

   public Vertex(Vec3 pos, Vector3f normal, int red, int green, int blue, int alpha, float texU, float texV, int overlayU, int overlayV, int lightU, int lightV) {
      this(pos, normal, red, green, blue, alpha, texU, texV, overlayU, overlayV, lightU, lightV, new HashMap<>());
   }

   public Vertex(
      Vec3 pos,
      Vector3f normal,
      int red,
      int green,
      int blue,
      int alpha,
      float texU,
      float texV,
      int overlayU,
      int overlayV,
      int lightU,
      int lightV,
      Map<VertexFormatElement, int[]> miscData
   ) {
      this.pos = pos;
      this.normal = normal;
      this.red = red;
      this.green = green;
      this.blue = blue;
      this.alpha = alpha;
      this.texU = texU;
      this.texV = texV;
      this.overlayU = overlayU;
      this.overlayV = overlayV;
      this.lightU = lightU;
      this.lightV = lightV;
      this.miscData = miscData;
   }

   public static Vertex create(
      Vec3 pos, Vector3f normal, Color color, TextureAtlasSprite sprite, float texU, float texV, int overlayU, int overlayV, int lightU, int lightV
   ) {
      return new Vertex(pos, normal, color, sprite.m_118367_(texU), sprite.m_118393_(texV), overlayU, overlayV, lightU, lightV);
   }

   public static Vertex create(Vec3 pos, Vector3f normal, Color color, TextureAtlasSprite sprite, float texU, float texV, int lightU, int lightV) {
      return create(pos, normal, color, sprite, texU, texV, 0, 10, lightU, lightV);
   }

   public static Vertex create(Vec3 pos, Vector3f normal, Color color, TextureAtlasSprite sprite, float u, float v) {
      return create(pos, normal, color, sprite, u, v, 0, 0);
   }

   public static Vertex create(Vec3 pos, Vector3f normal, TextureAtlasSprite sprite, float u, float v) {
      return create(pos, normal, Color.WHITE, sprite, u, v);
   }

   public Vec3 getPos() {
      return this.pos;
   }

   public Vector3f getNormal() {
      return this.normal;
   }

   public Vec3 getNormalD() {
      return new Vec3(this.getNormal());
   }

   public float getTexU() {
      return this.texU;
   }

   public float getTexV() {
      return this.texV;
   }

   public int getOverlayU() {
      return this.overlayU;
   }

   public int getOverlayV() {
      return this.overlayV;
   }

   public int getRawLightU() {
      return this.lightU;
   }

   public int getRawLightV() {
      return this.lightV;
   }

   public Vertex color(Color color) {
      return this.color(color.r(), color.g(), color.b(), color.a());
   }

   public Vertex color(int red, int green, int blue, int alpha) {
      this.red = red;
      this.green = green;
      this.blue = blue;
      this.alpha = alpha;
      return this;
   }

   public Vertex pos(Vec3 pos) {
      this.pos = pos;
      return this;
   }

   public Vertex normal(float x, float y, float z) {
      return this.normal(new Vector3f(x, y, z));
   }

   public Vertex normal(Vector3f normal) {
      this.normal = normal;
      return this;
   }

   public Vertex normal(Vec3 normal) {
      return this.normal(normal.m_252839_());
   }

   public Vertex texRaw(float u, float v) {
      this.texU = u;
      this.texV = v;
      return this;
   }

   public Vertex overlay(int u, int v) {
      this.overlayU = u;
      this.overlayV = v;
      return this;
   }

   public Vertex lightRaw(int u, int v) {
      this.lightU = u;
      this.lightV = v;
      return this;
   }

   public Vertex light(int u, int v) {
      return this.lightRaw(u << 4, v << 4);
   }

   public Vertex misc(VertexFormatElement element, int... data) {
      this.miscData.put(element, data);
      return this;
   }

   public Vertex flip() {
      return this.flip(true);
   }

   public Vertex flip(boolean deepCopy) {
      return this.copy(deepCopy).normal(-this.normal.x(), -this.normal.y(), -this.normal.z());
   }

   public Vertex copy(boolean deepCopy) {
      if (!deepCopy) {
         return new Vertex(
            this.pos,
            this.normal,
            this.red,
            this.green,
            this.blue,
            this.alpha,
            this.texU,
            this.texV,
            this.overlayU,
            this.overlayV,
            this.lightU,
            this.lightV,
            this.miscData
         );
      } else {
         Map<VertexFormatElement, int[]> miscCopy = new HashMap<>();

         for (Entry<VertexFormatElement, int[]> entry : this.miscData.entrySet()) {
            miscCopy.put(entry.getKey(), Arrays.copyOf(entry.getValue(), entry.getValue().length));
         }

         return new Vertex(
            this.pos,
            new Vector3f(this.normal),
            this.red,
            this.green,
            this.blue,
            this.alpha,
            this.texU,
            this.texV,
            this.overlayU,
            this.overlayV,
            this.lightU,
            this.lightV,
            miscCopy
         );
      }
   }

   public void write(VertexConsumer consumer) {
      consumer.m_5483_(this.pos.f_82479_, this.pos.f_82480_, this.pos.f_82481_);
      consumer.m_6122_(this.red, this.green, this.blue, this.alpha);
      consumer.m_7421_(this.texU, this.texV);
      consumer.m_7122_(this.overlayU, this.overlayV);
      consumer.m_7120_(this.lightU, this.lightV);
      consumer.m_5601_(this.normal.x(), this.normal.y(), this.normal.z());

      for (Entry<VertexFormatElement, int[]> entry : this.miscData.entrySet()) {
         consumer.misc(entry.getKey(), entry.getValue());
      }

      consumer.m_5752_();
   }
}
