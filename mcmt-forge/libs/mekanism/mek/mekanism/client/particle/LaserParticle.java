package mekanism.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import mekanism.common.lib.math.Pos3D;
import mekanism.common.particle.LaserParticleData;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class LaserParticle extends TextureSheetParticle {
   private static final float RADIAN_45 = (float)Math.toRadians(45.0);
   private static final float RADIAN_90 = (float)Math.toRadians(90.0);
   private final Direction direction;
   private final float halfLength;

   private LaserParticle(ClientLevel world, Vec3 start, Vec3 end, Direction dir, float energyScale) {
      super(world, (start.f_82479_ + end.f_82479_) / 2.0, (start.f_82480_ + end.f_82480_) / 2.0, (start.f_82481_ + end.f_82481_) / 2.0);
      this.f_107225_ = 5;
      this.f_107227_ = 1.0F;
      this.f_107228_ = 0.0F;
      this.f_107229_ = 0.0F;
      this.f_107230_ = 0.11F;
      this.f_107663_ = energyScale;
      this.halfLength = (float)(end.m_82554_(start) / 2.0);
      this.direction = dir;
      this.updateBoundingBox();
   }

   public void m_5744_(@NotNull VertexConsumer vertexBuilder, Camera renderInfo, float partialTicks) {
      Vec3 view = renderInfo.m_90583_();
      float newX = (float)(Mth.m_14139_(partialTicks, this.f_107209_, this.f_107212_) - view.m_7096_());
      float newY = (float)(Mth.m_14139_(partialTicks, this.f_107210_, this.f_107213_) - view.m_7098_());
      float newZ = (float)(Mth.m_14139_(partialTicks, this.f_107211_, this.f_107214_) - view.m_7094_());
      float uMin = this.m_5970_();
      float uMax = this.m_5952_();
      float vMin = this.m_5951_();
      float vMax = this.m_5950_();
      Quaternionf quaternion = this.direction.m_253075_();
      quaternion.mul(Axis.f_252436_.m_252961_(RADIAN_45));
      this.drawComponent(vertexBuilder, this.getResultVector(quaternion, newX, newY, newZ), uMin, uMax, vMin, vMax);
      Quaternionf quaternion2 = new Quaternionf(quaternion);
      quaternion2.mul(Axis.f_252436_.m_252961_(RADIAN_90));
      this.drawComponent(vertexBuilder, this.getResultVector(quaternion2, newX, newY, newZ), uMin, uMax, vMin, vMax);
   }

   private Vector3f[] getResultVector(Quaternionf quaternion, float newX, float newY, float newZ) {
      Vector3f[] resultVector = new Vector3f[]{
         new Vector3f(-this.f_107663_, -this.halfLength, 0.0F),
         new Vector3f(-this.f_107663_, this.halfLength, 0.0F),
         new Vector3f(this.f_107663_, this.halfLength, 0.0F),
         new Vector3f(this.f_107663_, -this.halfLength, 0.0F)
      };

      for (Vector3f vec : resultVector) {
         quaternion.transform(vec);
         vec.add(newX, newY, newZ);
      }

      return resultVector;
   }

   private void drawComponent(VertexConsumer vertexBuilder, Vector3f[] resultVector, float uMin, float uMax, float vMin, float vMax) {
      this.addVertex(vertexBuilder, resultVector[0], uMax, vMax);
      this.addVertex(vertexBuilder, resultVector[1], uMax, vMin);
      this.addVertex(vertexBuilder, resultVector[2], uMin, vMin);
      this.addVertex(vertexBuilder, resultVector[3], uMin, vMax);
      this.addVertex(vertexBuilder, resultVector[1], uMax, vMin);
      this.addVertex(vertexBuilder, resultVector[0], uMax, vMax);
      this.addVertex(vertexBuilder, resultVector[3], uMin, vMax);
      this.addVertex(vertexBuilder, resultVector[2], uMin, vMin);
   }

   private void addVertex(VertexConsumer vertexBuilder, Vector3f pos, float u, float v) {
      vertexBuilder.m_5483_(pos.x(), pos.y(), pos.z())
         .m_7421_(u, v)
         .m_85950_(this.f_107227_, this.f_107228_, this.f_107229_, this.f_107230_)
         .m_7120_(240, 240)
         .m_5752_();
   }

   @NotNull
   public ParticleRenderType m_7556_() {
      return ParticleRenderType.f_107431_;
   }

   protected void m_107250_(float particleWidth, float particleHeight) {
      if (particleWidth != this.f_107221_ || particleHeight != this.f_107222_) {
         this.f_107221_ = particleWidth;
         this.f_107222_ = particleHeight;
      }
   }

   public void m_107264_(double x, double y, double z) {
      this.f_107212_ = x;
      this.f_107213_ = y;
      this.f_107214_ = z;
      if (this.direction != null) {
         this.updateBoundingBox();
      }
   }

   private void updateBoundingBox() {
      float halfDiameter = this.f_107663_ / 2.0F;

      this.m_107259_(
         switch (this.direction) {
            case DOWN, UP -> new AABB(
               this.f_107212_ - halfDiameter,
               this.f_107213_ - this.halfLength,
               this.f_107214_ - halfDiameter,
               this.f_107212_ + halfDiameter,
               this.f_107213_ + this.halfLength,
               this.f_107214_ + halfDiameter
            );
            case NORTH, SOUTH -> new AABB(
               this.f_107212_ - halfDiameter,
               this.f_107213_ - halfDiameter,
               this.f_107214_ - this.halfLength,
               this.f_107212_ + halfDiameter,
               this.f_107213_ + halfDiameter,
               this.f_107214_ + this.halfLength
            );
            case WEST, EAST -> new AABB(
               this.f_107212_ - this.halfLength,
               this.f_107213_ - halfDiameter,
               this.f_107214_ - halfDiameter,
               this.f_107212_ + this.halfLength,
               this.f_107213_ + halfDiameter,
               this.f_107214_ + halfDiameter
            );
            default -> throw new IncompatibleClassChangeError();
         }
      );
   }

   public static class Factory implements ParticleProvider<LaserParticleData> {
      private final SpriteSet spriteSet;

      public Factory(SpriteSet spriteSet) {
         this.spriteSet = spriteSet;
      }

      public LaserParticle createParticle(
         LaserParticleData data, @NotNull ClientLevel world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed
      ) {
         Pos3D start = new Pos3D(x, y, z);
         Pos3D end = start.translate(data.direction(), data.distance());
         LaserParticle particleLaser = new LaserParticle(world, start, end, data.direction(), data.energyScale());
         particleLaser.m_108335_(this.spriteSet);
         return particleLaser;
      }
   }
}
