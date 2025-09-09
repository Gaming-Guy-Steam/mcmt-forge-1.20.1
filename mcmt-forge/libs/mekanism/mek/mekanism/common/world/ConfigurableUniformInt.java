package mekanism.common.world;

import com.mojang.serialization.Codec;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismIntProviderTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviderType;
import org.jetbrains.annotations.NotNull;

public class ConfigurableUniformInt extends IntProvider {
   public static final ConfigurableUniformInt SALT = new ConfigurableUniformInt();
   public static final Codec<ConfigurableUniformInt> CODEC = Codec.unit(SALT);

   private ConfigurableUniformInt() {
   }

   public int m_214085_(@NotNull RandomSource random) {
      return Mth.m_216287_(random, this.m_142739_(), this.m_142737_());
   }

   public int m_142739_() {
      return MekanismConfig.world.salt.minRadius.get();
   }

   public int m_142737_() {
      return MekanismConfig.world.salt.maxRadius.get();
   }

   @NotNull
   public IntProviderType<?> m_141948_() {
      return MekanismIntProviderTypes.CONFIGURABLE_UNIFORM.get();
   }

   public String toString() {
      return "[" + this.m_142739_() + "-" + this.m_142737_() + "]";
   }
}
