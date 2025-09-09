package mekanism.common.lib.effect;

import java.util.Random;
import mekanism.common.lib.Color;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class CustomEffect {
   private final int GRID_SIZE;
   private final ResourceLocation texture;
   protected final Random rand = new Random();
   private Vec3 pos = new Vec3(0.0, 0.0, 0.0);
   private Color color = Color.rgbai(255, 255, 255, 255);
   private float scale = 1.0F;
   protected int ticker;

   public CustomEffect(ResourceLocation texture) {
      this(texture, 4);
   }

   public CustomEffect(ResourceLocation texture, int gridSize) {
      this.texture = texture;
      this.GRID_SIZE = gridSize;
   }

   protected Vec3 randVec() {
      return new Vec3(this.rand.nextDouble() - 0.5, this.rand.nextDouble() - 0.5, this.rand.nextDouble() - 0.5).m_82541_();
   }

   public boolean tick() {
      this.ticker++;
      return false;
   }

   public void setPos(Vec3 pos) {
      this.pos = pos;
   }

   public void setScale(float scale) {
      this.scale = scale;
   }

   public void setColor(Color color) {
      this.color = color;
   }

   public Color getColor() {
      return this.color;
   }

   public Vec3 getPos(float partialTick) {
      return this.pos;
   }

   public float getScale() {
      return this.scale;
   }

   public ResourceLocation getTexture() {
      return this.texture;
   }

   public int getTextureGridSize() {
      return this.GRID_SIZE;
   }
}
