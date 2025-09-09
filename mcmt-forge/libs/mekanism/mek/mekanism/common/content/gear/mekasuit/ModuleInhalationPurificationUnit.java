package mekanism.common.content.gear.mekasuit;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleBooleanData;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.api.math.FloatingLong;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.MekanismUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

@ParametersAreNotNullByDefault
public class ModuleInhalationPurificationUnit implements ICustomModule<ModuleInhalationPurificationUnit> {
   private static final ICustomModule.ModuleDamageAbsorbInfo INHALATION_ABSORB_INFO = new ICustomModule.ModuleDamageAbsorbInfo(
      MekanismConfig.gear.mekaSuitMagicDamageRatio, MekanismConfig.gear.mekaSuitEnergyUsageMagicReduce
   );
   private IModuleConfigItem<Boolean> beneficialEffects;
   private IModuleConfigItem<Boolean> neutralEffects;
   private IModuleConfigItem<Boolean> harmfulEffects;

   @Override
   public void init(IModule<ModuleInhalationPurificationUnit> module, ModuleConfigItemCreator configItemCreator) {
      this.beneficialEffects = configItemCreator.createConfigItem(
         "beneficial_effects", MekanismLang.MODULE_PURIFICATION_BENEFICIAL, new ModuleBooleanData(false)
      );
      this.neutralEffects = configItemCreator.createConfigItem("neutral_effects", MekanismLang.MODULE_PURIFICATION_NEUTRAL, new ModuleBooleanData());
      this.harmfulEffects = configItemCreator.createConfigItem("harmful_effects", MekanismLang.MODULE_PURIFICATION_HARMFUL, new ModuleBooleanData());
   }

   @Override
   public void tickClient(IModule<ModuleInhalationPurificationUnit> module, Player player) {
      if (!player.m_5833_()) {
         FloatingLong usage = MekanismConfig.gear.mekaSuitEnergyUsagePotionTick.get();
         boolean free = usage.isZero() || player.m_7500_();
         FloatingLong energy = free ? FloatingLong.ZERO : module.getContainerEnergy().copy();
         if (free || energy.greaterOrEqual(usage)) {
            for (MobEffectInstance effect : player.m_21220_().stream().filter(this::canHandle).toList()) {
               if (free) {
                  this.speedupEffect(player, effect);
               } else {
                  energy = energy.minusEqual(usage);
                  this.speedupEffect(player, effect);
                  if (energy.smallerThan(usage)) {
                     break;
                  }
               }
            }
         }
      }
   }

   @Override
   public void tickServer(IModule<ModuleInhalationPurificationUnit> module, Player player) {
      FloatingLong usage = MekanismConfig.gear.mekaSuitEnergyUsagePotionTick.get();
      boolean free = usage.isZero() || player.m_7500_();
      IEnergyContainer energyContainer = free ? null : module.getEnergyContainer();
      if (free || energyContainer != null && energyContainer.getEnergy().greaterOrEqual(usage)) {
         for (MobEffectInstance effect : player.m_21220_().stream().filter(this::canHandle).toList()) {
            if (free) {
               this.speedupEffect(player, effect);
            } else {
               if (module.useEnergy(player, energyContainer, usage, true).isZero()) {
                  break;
               }

               this.speedupEffect(player, effect);
               if (energyContainer.getEnergy().smallerThan(usage)) {
                  break;
               }
            }
         }
      }
   }

   @Nullable
   @Override
   public ICustomModule.ModuleDamageAbsorbInfo getDamageAbsorbInfo(IModule<ModuleInhalationPurificationUnit> module, DamageSource damageSource) {
      return damageSource.m_269533_(MekanismTags.DamageTypes.IS_PREVENTABLE_MAGIC) ? INHALATION_ABSORB_INFO : null;
   }

   private void speedupEffect(Player player, MobEffectInstance effect) {
      for (int i = 0; i < 9; i++) {
         MekanismUtils.speedUpEffectSafely(player, effect);
      }
   }

   private boolean canHandle(MobEffectInstance effectInstance) {
      if (MekanismUtils.shouldSpeedUpEffect(effectInstance)) {
         if (switch (effectInstance.m_19544_().m_19483_()) {
            case BENEFICIAL -> (Boolean)this.beneficialEffects.get();
            case HARMFUL -> (Boolean)this.harmfulEffects.get();
            case NEUTRAL -> (Boolean)this.neutralEffects.get();
            default -> throw new IncompatibleClassChangeError();
         }) {
            return true;
         }
      }

      return false;
   }
}
