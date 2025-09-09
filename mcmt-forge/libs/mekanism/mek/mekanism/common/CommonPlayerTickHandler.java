package mekanism.common;

import java.util.Map;
import java.util.Optional;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.functions.FloatSupplier;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.IBlastingItem;
import mekanism.common.content.gear.mekasuit.ModuleGravitationalModulatingUnit;
import mekanism.common.content.gear.mekasuit.ModuleHydraulicPropulsionUnit;
import mekanism.common.content.gear.mekasuit.ModuleHydrostaticRepulsorUnit;
import mekanism.common.content.gear.mekasuit.ModuleLocomotiveBoostingUnit;
import mekanism.common.entity.EntityFlame;
import mekanism.common.item.gear.ItemFlamethrower;
import mekanism.common.item.gear.ItemFreeRunners;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.item.gear.ItemScubaMask;
import mekanism.common.item.gear.ItemScubaTank;
import mekanism.common.item.interfaces.IJetpackItem;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.registries.MekanismGameEvents;
import mekanism.common.registries.MekanismModules;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;

public class CommonPlayerTickHandler {
   public static boolean isOnGroundOrSleeping(Player player) {
      return player.m_20096_() || player.m_5803_();
   }

   public static boolean isScubaMaskOn(Player player, ItemStack tank) {
      ItemStack mask = player.m_6844_(EquipmentSlot.HEAD);
      return !tank.m_41619_()
         && !mask.m_41619_()
         && tank.m_41720_() instanceof ItemScubaTank scubaTank
         && mask.m_41720_() instanceof ItemScubaMask
         && ChemicalUtil.hasGas(tank)
         && scubaTank.getFlowing(tank);
   }

   private static boolean isFlamethrowerOn(Player player, ItemStack currentItem) {
      return Mekanism.playerState.isFlamethrowerOn(player) && !currentItem.m_41619_() && currentItem.m_41720_() instanceof ItemFlamethrower;
   }

   public static float getStepBoost(Player player) {
      ItemStack stack = player.m_6844_(EquipmentSlot.FEET);
      if (!stack.m_41619_() && !player.m_6144_()) {
         if (stack.m_41720_() instanceof ItemFreeRunners freeRunners && freeRunners.getMode(stack).providesStepBoost()) {
            return 0.5F;
         }

         IModule<ModuleHydraulicPropulsionUnit> module = IModuleHelper.INSTANCE.load(stack, MekanismModules.HYDRAULIC_PROPULSION_UNIT);
         if (module != null && module.isEnabled()) {
            return module.getCustomInstance().getStepHeight();
         }
      }

      return 0.0F;
   }

   public static float getSwimBoost(Player player) {
      ItemStack stack = player.m_6844_(EquipmentSlot.LEGS);
      if (!stack.m_41619_()) {
         IModule<ModuleHydrostaticRepulsorUnit> module = IModuleHelper.INSTANCE.load(stack, MekanismModules.HYDROSTATIC_REPULSOR_UNIT);
         if (module != null && module.isEnabled() && module.getCustomInstance().isSwimBoost(module, player)) {
            return 1.0F;
         }
      }

      return 0.0F;
   }

   @SubscribeEvent
   public void onTick(PlayerTickEvent event) {
      if (event.phase == Phase.END && event.side.isServer()) {
         this.tickEnd(event.player);
      }
   }

   private void tickEnd(Player player) {
      Mekanism.playerState.updateStepAssist(player);
      Mekanism.playerState.updateSwimBoost(player);
      if (player instanceof ServerPlayer serverPlayer) {
         RadiationManager.get().tickServer(serverPlayer);
      }

      ItemStack currentItem = player.m_150109_().m_36056_();
      if (isFlamethrowerOn(player, currentItem)) {
         EntityFlame flame = EntityFlame.create(player);
         if (flame != null) {
            if (flame.m_6084_()) {
               player.m_9236_().m_7967_(flame);
            }

            if (MekanismUtils.isPlayingMode(player)) {
               ((ItemFlamethrower)currentItem.m_41720_()).useGas(currentItem, 1L);
            }
         }
      }

      ItemStack jetpack = IJetpackItem.getActiveJetpack(player);
      if (!jetpack.m_41619_()) {
         ItemStack primaryJetpack = IJetpackItem.getPrimaryJetpack(player);
         if (!primaryJetpack.m_41619_()) {
            IJetpackItem.JetpackMode primaryMode = ((IJetpackItem)primaryJetpack.m_41720_()).getJetpackMode(primaryJetpack);
            IJetpackItem.JetpackMode mode = IJetpackItem.getPlayerJetpackMode(player, primaryMode, () -> Mekanism.keyMap.has(player.m_20148_(), 0));
            if (mode != IJetpackItem.JetpackMode.DISABLED) {
               if (IJetpackItem.handleJetpackMotion(player, mode, () -> Mekanism.keyMap.has(player.m_20148_(), 0))) {
                  player.m_183634_();
                  if (player instanceof ServerPlayer serverPlayer) {
                     serverPlayer.f_8906_.f_9737_ = 0;
                  }
               }

               ((IJetpackItem)jetpack.m_41720_()).useJetpackFuel(jetpack);
               if (player.m_9236_().m_46467_() % 10L == 0L) {
                  player.m_146850_(MekanismGameEvents.JETPACK_BURN.get());
               }
            }
         }
      }

      ItemStack chest = player.m_6844_(EquipmentSlot.CHEST);
      if (isScubaMaskOn(player, chest)) {
         ItemScubaTank tank = (ItemScubaTank)chest.m_41720_();
         int max = player.m_6062_();
         tank.useGas(chest, 1L);
         GasStack received = tank.useGas(chest, max - player.m_20146_());
         if (!received.isEmpty()) {
            player.m_20301_(player.m_20146_() + (int)received.getAmount());
         }

         if (player.m_20146_() == max) {
            for (MobEffectInstance effect : player.m_21220_()) {
               if (MekanismUtils.shouldSpeedUpEffect(effect)) {
                  for (int i = 0; i < 9; i++) {
                     MekanismUtils.speedUpEffectSafely(player, effect);
                  }
               }
            }
         }
      }

      Mekanism.playerState.updateFlightInfo(player);
   }

   public static boolean isGravitationalModulationReady(Player player) {
      if (!MekanismUtils.isPlayingMode(player)) {
         return false;
      } else {
         IModule<ModuleGravitationalModulatingUnit> module = IModuleHelper.INSTANCE
            .load(player.m_6844_(EquipmentSlot.CHEST), MekanismModules.GRAVITATIONAL_MODULATING_UNIT);
         return module != null && module.isEnabled() && module.hasEnoughEnergy(MekanismConfig.gear.mekaSuitEnergyUsageGravitationalModulation);
      }
   }

   public static boolean isGravitationalModulationOn(Player player) {
      return isGravitationalModulationReady(player) && player.m_150110_().f_35935_;
   }

   @SubscribeEvent
   public void onEntityAttacked(LivingAttackEvent event) {
      LivingEntity entity = event.getEntity();
      if (!(event.getAmount() <= 0.0F) && entity.m_6084_()) {
         if (event.getSource().m_269533_(MekanismTags.DamageTypes.IS_PREVENTABLE_MAGIC)) {
            ItemStack headStack = entity.m_6844_(EquipmentSlot.HEAD);
            if (!headStack.m_41619_() && headStack.m_41720_() instanceof ItemScubaMask) {
               ItemStack chestStack = entity.m_6844_(EquipmentSlot.CHEST);
               if (!chestStack.m_41619_()
                  && chestStack.m_41720_() instanceof ItemScubaTank tank
                  && tank.getFlowing(chestStack)
                  && ChemicalUtil.hasGas(chestStack)) {
                  event.setCanceled(true);
                  return;
               }
            }
         }

         if (event.getSource().m_269533_(DamageTypeTags.f_268549_)) {
            CommonPlayerTickHandler.FallEnergyInfo info = this.getFallAbsorptionEnergyInfo(entity);
            if (info != null && this.tryAbsorbAll(event, info.container, info.damageRatio, info.energyCost)) {
               return;
            }
         }

         if (entity instanceof Player player && ItemMekaSuitArmor.tryAbsorbAll(player, event.getSource(), event.getAmount())) {
            event.setCanceled(true);
         }
      }
   }

   @SubscribeEvent
   public void onLivingHurt(LivingHurtEvent event) {
      LivingEntity entity = event.getEntity();
      if (!(event.getAmount() <= 0.0F) && entity.m_6084_()) {
         if (event.getSource().m_269533_(DamageTypeTags.f_268549_)) {
            CommonPlayerTickHandler.FallEnergyInfo info = this.getFallAbsorptionEnergyInfo(entity);
            if (info != null && this.handleDamage(event, info.container, info.damageRatio, info.energyCost)) {
               return;
            }
         }

         if (entity instanceof Player player) {
            float ratioAbsorbed = ItemMekaSuitArmor.getDamageAbsorbed(player, event.getSource(), event.getAmount());
            if (ratioAbsorbed > 0.0F) {
               float damageRemaining = event.getAmount() * Math.max(0.0F, 1.0F - ratioAbsorbed);
               if (damageRemaining <= 0.0F) {
                  event.setCanceled(true);
               } else {
                  event.setAmount(damageRemaining);
               }
            }
         }
      }
   }

   private boolean tryAbsorbAll(
      LivingAttackEvent event, @Nullable IEnergyContainer energyContainer, FloatSupplier absorptionRatio, FloatingLongSupplier energyCost
   ) {
      if (energyContainer != null && absorptionRatio.getAsFloat() == 1.0F) {
         FloatingLong energyRequirement = energyCost.get().multiply((double)event.getAmount());
         if (energyRequirement.isZero()) {
            event.setCanceled(true);
            return true;
         }

         FloatingLong simulatedExtract = energyContainer.extract(energyRequirement, Action.SIMULATE, AutomationType.MANUAL);
         if (simulatedExtract.equals(energyRequirement)) {
            energyContainer.extract(energyRequirement, Action.EXECUTE, AutomationType.MANUAL);
            event.setCanceled(true);
            return true;
         }
      }

      return false;
   }

   private boolean handleDamage(
      LivingHurtEvent event, @Nullable IEnergyContainer energyContainer, FloatSupplier absorptionRatio, FloatingLongSupplier energyCost
   ) {
      if (energyContainer != null) {
         float absorption = absorptionRatio.getAsFloat();
         float amount = event.getAmount() * absorption;
         FloatingLong energyRequirement = energyCost.get().multiply((double)amount);
         float ratioAbsorbed;
         if (energyRequirement.isZero()) {
            ratioAbsorbed = absorption;
         } else {
            ratioAbsorbed = absorption * energyContainer.extract(energyRequirement, Action.EXECUTE, AutomationType.MANUAL).divide((double)amount).floatValue();
         }

         if (ratioAbsorbed > 0.0F) {
            float damageRemaining = event.getAmount() * Math.max(0.0F, 1.0F - ratioAbsorbed);
            if (damageRemaining <= 0.0F) {
               event.setCanceled(true);
               return true;
            }

            event.setAmount(damageRemaining);
         }
      }

      return false;
   }

   @SubscribeEvent
   public void onLivingJump(LivingJumpEvent event) {
      if (event.getEntity() instanceof Player player) {
         IModule<ModuleHydraulicPropulsionUnit> module = IModuleHelper.INSTANCE
            .load(player.m_6844_(EquipmentSlot.FEET), MekanismModules.HYDRAULIC_PROPULSION_UNIT);
         if (module != null && module.isEnabled() && Mekanism.keyMap.has(player.m_20148_(), 1)) {
            float boost = module.getCustomInstance().getBoost();
            FloatingLong usage = MekanismConfig.gear.mekaSuitBaseJumpEnergyUsage.get().multiply((double)(boost / 0.1F));
            IEnergyContainer energyContainer = module.getEnergyContainer();
            if (module.canUseEnergy(player, energyContainer, usage, false)) {
               IModule<ModuleLocomotiveBoostingUnit> boostModule = IModuleHelper.INSTANCE
                  .load(player.m_6844_(EquipmentSlot.LEGS), MekanismModules.LOCOMOTIVE_BOOSTING_UNIT);
               if (boostModule != null && boostModule.isEnabled() && boostModule.getCustomInstance().canFunction(boostModule, player)) {
                  boost = (float)Math.sqrt(boost);
               }

               player.m_20256_(player.m_20184_().m_82520_(0.0, boost, 0.0));
               module.useEnergy(player, energyContainer, usage, true);
            }
         }
      }
   }

   @Nullable
   private CommonPlayerTickHandler.FallEnergyInfo getFallAbsorptionEnergyInfo(LivingEntity base) {
      ItemStack feetStack = base.m_6844_(EquipmentSlot.FEET);
      if (!feetStack.m_41619_()) {
         if (feetStack.m_41720_() instanceof ItemFreeRunners boots) {
            if (boots.getMode(feetStack).preventsFallDamage()) {
               return new CommonPlayerTickHandler.FallEnergyInfo(
                  StorageUtils.getEnergyContainer(feetStack, 0), MekanismConfig.gear.freeRunnerFallDamageRatio, MekanismConfig.gear.freeRunnerFallEnergyCost
               );
            }
         } else if (feetStack.m_41720_() instanceof ItemMekaSuitArmor) {
            return new CommonPlayerTickHandler.FallEnergyInfo(
               StorageUtils.getEnergyContainer(feetStack, 0), MekanismConfig.gear.mekaSuitFallDamageRatio, MekanismConfig.gear.mekaSuitEnergyUsageFall
            );
         }
      }

      return null;
   }

   @SubscribeEvent
   public void getBreakSpeed(BreakSpeed event) {
      Player player = event.getEntity();
      float speed = event.getNewSpeed();
      Optional<BlockPos> position = event.getPosition();
      if (position.isPresent()) {
         BlockPos pos = position.get();
         ItemStack mainHand = player.m_21205_();
         if (!mainHand.m_41619_() && mainHand.m_41720_() instanceof IBlastingItem tool) {
            Map<BlockPos, BlockState> blocks = tool.getBlastedBlocks(player.m_9236_(), player, mainHand, pos, event.getState());
            if (!blocks.isEmpty()) {
               float targetHardness = event.getState().m_60800_(player.m_9236_(), pos);
               float maxHardness = blocks.entrySet()
                  .stream()
                  .map(entry -> entry.getValue().m_60800_(player.m_9236_(), entry.getKey()))
                  .reduce(targetHardness, Float::max);
               speed *= targetHardness / maxHardness;
            }
         }
      }

      ItemStack legs = player.m_6844_(EquipmentSlot.LEGS);
      if (!legs.m_41619_() && IModuleHelper.INSTANCE.isEnabled(legs, MekanismModules.GYROSCOPIC_STABILIZATION_UNIT)) {
         if (player.isEyeInFluidType((FluidType)ForgeMod.WATER_TYPE.get()) && !EnchantmentHelper.m_44934_(player)) {
            speed *= 5.0F;
         }

         if (!player.m_20096_()) {
            speed *= 5.0F;
         }
      }

      event.setNewSpeed(speed);
   }

   private record FallEnergyInfo(@Nullable IEnergyContainer container, FloatSupplier damageRatio, FloatingLongSupplier energyCost) {
   }
}
