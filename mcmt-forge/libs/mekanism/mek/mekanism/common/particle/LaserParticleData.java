package mekanism.common.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import mekanism.common.registries.MekanismParticleTypes;
import mekanism.common.util.RegistryUtils;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleOptions.Deserializer;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public record LaserParticleData(Direction direction, double distance, float energyScale) implements ParticleOptions {
   public static final Deserializer<LaserParticleData> DESERIALIZER = new Deserializer<LaserParticleData>() {
      @NotNull
      public LaserParticleData fromCommand(@NotNull ParticleType<LaserParticleData> type, @NotNull StringReader reader) throws CommandSyntaxException {
         reader.expect(' ');
         Direction direction = Direction.m_122376_(reader.readInt());
         reader.expect(' ');
         double distance = reader.readDouble();
         reader.expect(' ');
         float energyScale = reader.readFloat();
         return new LaserParticleData(direction, distance, energyScale);
      }

      @NotNull
      public LaserParticleData fromNetwork(@NotNull ParticleType<LaserParticleData> type, FriendlyByteBuf buf) {
         return new LaserParticleData((Direction)buf.m_130066_(Direction.class), buf.readDouble(), buf.readFloat());
      }
   };
   public static final Codec<LaserParticleData> CODEC = RecordCodecBuilder.create(
      val -> val.group(
            Direction.f_175356_.fieldOf("direction").forGetter(data -> data.direction),
            Codec.DOUBLE.fieldOf("distance").forGetter(data -> data.distance),
            Codec.FLOAT.fieldOf("energyScale").forGetter(data -> data.energyScale)
         )
         .apply(val, LaserParticleData::new)
   );

   @NotNull
   public ParticleType<?> m_6012_() {
      return (ParticleType<?>)MekanismParticleTypes.LASER.get();
   }

   public void m_7711_(@NotNull FriendlyByteBuf buffer) {
      buffer.m_130068_(this.direction);
      buffer.writeDouble(this.distance);
      buffer.writeFloat(this.energyScale);
   }

   @NotNull
   public String m_5942_() {
      return String.format(Locale.ROOT, "%s %d %.2f %.2f", RegistryUtils.getName(this.m_6012_()), this.direction.ordinal(), this.distance, this.energyScale);
   }
}
