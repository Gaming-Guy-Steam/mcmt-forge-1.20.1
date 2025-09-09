package mekanism.common.registries;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import mekanism.api.text.IHasTranslationKey;
import mekanism.common.Mekanism;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class MekanismDamageTypes {
   private static final Map<String, MekanismDamageTypes.MekanismDamageType> INTERNAL_DAMAGE_TYPES = new HashMap<>();
   public static final Map<String, MekanismDamageTypes.MekanismDamageType> DAMAGE_TYPES = Collections.unmodifiableMap(INTERNAL_DAMAGE_TYPES);
   public static final MekanismDamageTypes.MekanismDamageType LASER = new MekanismDamageTypes.MekanismDamageType("laser", 0.1F);
   public static final MekanismDamageTypes.MekanismDamageType RADIATION = new MekanismDamageTypes.MekanismDamageType("radiation");

   public record MekanismDamageType(ResourceKey<DamageType> key, float exhaustion) implements IHasTranslationKey {
      public MekanismDamageType(ResourceKey<DamageType> key, float exhaustion) {
         MekanismDamageTypes.INTERNAL_DAMAGE_TYPES.put(key.m_135782_().toString(), this);
         this.key = key;
         this.exhaustion = exhaustion;
      }

      private MekanismDamageType(String name) {
         this(name, 0.0F);
      }

      private MekanismDamageType(String name, float exhaustion) {
         this(ResourceKey.m_135785_(Registries.f_268580_, Mekanism.rl(name)), exhaustion);
      }

      public String getMsgId() {
         return this.registryName().m_135827_() + "." + this.registryName().m_135815_();
      }

      public ResourceLocation registryName() {
         return this.key.m_135782_();
      }

      @NotNull
      @Override
      public String getTranslationKey() {
         return "death.attack." + this.getMsgId();
      }

      public DamageSource source(Level level) {
         return this.source(level.m_9598_());
      }

      public DamageSource source(RegistryAccess registryAccess) {
         return new DamageSource(registryAccess.m_175515_(Registries.f_268580_).m_246971_(this.key()));
      }
   }
}
