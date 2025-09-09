package mekanism.common.block.attribute;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import mekanism.common.lib.math.Pos3D;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;

public class AttributeParticleFX implements Attribute {
   private final List<Function<RandomSource, AttributeParticleFX.Particle>> particleFunctions = new ArrayList<>();

   public List<Function<RandomSource, AttributeParticleFX.Particle>> getParticleFunctions() {
      return this.particleFunctions;
   }

   public AttributeParticleFX addDense(ParticleOptions type, int density, Function<RandomSource, Pos3D> posSupplier) {
      Function<RandomSource, AttributeParticleFX.Particle> particleFunction = random -> new AttributeParticleFX.Particle(type, posSupplier.apply(random));

      for (int i = 0; i < density; i++) {
         this.particleFunctions.add(particleFunction);
      }

      return this;
   }

   public AttributeParticleFX add(ParticleOptions type, Function<RandomSource, Pos3D> posSupplier) {
      this.particleFunctions.add(random -> new AttributeParticleFX.Particle(type, posSupplier.apply(random)));
      return this;
   }

   public static class Particle {
      private final ParticleOptions type;
      private final Pos3D pos;

      protected Particle(ParticleOptions type, Pos3D pos) {
         this.type = type;
         this.pos = pos;
      }

      public ParticleOptions getType() {
         return this.type;
      }

      public Pos3D getPos() {
         return this.pos;
      }
   }
}
