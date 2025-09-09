package mekanism.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.NotNull;

public class JetpackFlameParticle extends FlameParticle {
   private JetpackFlameParticle(ClientLevel world, double posX, double posY, double posZ, double velX, double velY, double velZ) {
      super(world, posX, posY, posZ, velX, velY, velZ);
   }

   public int m_6355_(float partialTick) {
      return 190 + (int)(20.0F * (1.0F - ((Double)Minecraft.m_91087_().f_91066_.m_231927_().m_231551_()).floatValue()));
   }

   public void m_5744_(@NotNull VertexConsumer vertexBuilder, @NotNull Camera renderInfo, float partialTicks) {
      if (this.f_107224_ > 0) {
         super.m_5744_(vertexBuilder, renderInfo, partialTicks);
      }
   }

   public static class Factory implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet spriteSet;

      public Factory(SpriteSet spriteSet) {
         this.spriteSet = spriteSet;
      }

      public Particle createParticle(
         @NotNull SimpleParticleType type, @NotNull ClientLevel world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed
      ) {
         JetpackFlameParticle particle = new JetpackFlameParticle(world, x, y, z, xSpeed, ySpeed, zSpeed);
         particle.m_108335_(this.spriteSet);
         return particle;
      }
   }
}
