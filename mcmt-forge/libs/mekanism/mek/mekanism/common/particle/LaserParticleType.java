package mekanism.common.particle;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;
import org.jetbrains.annotations.NotNull;

public class LaserParticleType extends ParticleType<LaserParticleData> {
   public LaserParticleType() {
      super(false, LaserParticleData.DESERIALIZER);
   }

   @NotNull
   public Codec<LaserParticleData> m_7652_() {
      return LaserParticleData.CODEC;
   }
}
