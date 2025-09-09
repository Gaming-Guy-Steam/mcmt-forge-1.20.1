package mekanism.common.lib.radiation.capability;

import mekanism.api.radiation.capability.IRadiationEntity;
import mekanism.common.Mekanism;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.CapabilityCache;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.registries.MekanismDamageTypes;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

public class DefaultRadiationEntity implements IRadiationEntity {
   private double radiation = 1.0E-7;

   @Override
   public double getRadiation() {
      return this.radiation;
   }

   @Override
   public void radiate(double magnitude) {
      if (magnitude > 0.0) {
         this.radiation += magnitude;
      }
   }

   @Override
   public void update(@NotNull LivingEntity entity) {
      if (!(entity instanceof Player player && !MekanismUtils.isPlayingMode(player))) {
         RandomSource rand = entity.m_9236_().m_213780_();
         double minSeverity = MekanismConfig.general.radiationNegativeEffectsMinSeverity.get();
         double severityScale = RadiationManager.RadiationScale.getScaledDoseSeverity(this.radiation);
         double chance = minSeverity + rand.m_188500_() * (1.0 - minSeverity);
         if (severityScale > chance) {
            float strength = Math.max(1.0F, (float)Math.log1p(this.radiation));
            if (rand.m_188499_()) {
               if (entity instanceof ServerPlayer playerx) {
                  MinecraftServer server = entity.m_20194_();
                  int totemTimesUsed = -1;
                  if (server != null && server.m_7035_()) {
                     totemTimesUsed = playerx.m_8951_().m_13015_(Stats.f_12982_.m_12902_(Items.f_42747_));
                  }

                  if (entity.m_6469_(MekanismDamageTypes.RADIATION.source(entity.m_9236_()), strength)) {
                     boolean hardcoreTotem = totemTimesUsed != -1 && totemTimesUsed < playerx.m_8951_().m_13015_(Stats.f_12982_.m_12902_(Items.f_42747_));
                     MekanismCriteriaTriggers.DAMAGE.trigger(playerx, MekanismDamageTypes.RADIATION, hardcoreTotem);
                  }
               } else {
                  entity.m_6469_(MekanismDamageTypes.RADIATION.source(entity.m_9236_()), strength);
               }
            }

            if (entity instanceof ServerPlayer playerx) {
               playerx.m_36324_().m_38703_(strength);
            }
         }
      }
   }

   @Override
   public void set(double magnitude) {
      this.radiation = Math.max(1.0E-7, magnitude);
   }

   @Override
   public void decay() {
      this.set(this.radiation * MekanismConfig.general.radiationTargetDecayRate.get());
   }

   public CompoundTag serializeNBT() {
      CompoundTag ret = new CompoundTag();
      ret.m_128347_("radiation", this.radiation);
      return ret;
   }

   public void deserializeNBT(CompoundTag nbt) {
      this.set(nbt.m_128459_("radiation"));
   }

   public static class Provider implements ICapabilitySerializable<CompoundTag> {
      public static final ResourceLocation NAME = Mekanism.rl("radiation");
      private final IRadiationEntity defaultImpl = new DefaultRadiationEntity();
      private final CapabilityCache capabilityCache = new CapabilityCache();

      public Provider() {
         this.capabilityCache.addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.RADIATION_ENTITY, this.defaultImpl));
      }

      @NotNull
      public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, Direction side) {
         return this.capabilityCache.getCapability(capability, side);
      }

      public void invalidate() {
         this.capabilityCache.invalidate(Capabilities.RADIATION_ENTITY, null);
      }

      public CompoundTag serializeNBT() {
         return (CompoundTag)this.defaultImpl.serializeNBT();
      }

      public void deserializeNBT(CompoundTag nbt) {
         this.defaultImpl.deserializeNBT(nbt);
      }
   }
}
