package mekanism.client;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.Map.Entry;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.radial.RadialData;
import mekanism.client.gui.GuiRadialSelector;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.render.hud.MekanismStatusOverlay;
import mekanism.client.render.lib.ScrollIncrementer;
import mekanism.client.sound.GeigerSound;
import mekanism.client.sound.SoundHandler;
import mekanism.common.CommonPlayerTickHandler;
import mekanism.common.Mekanism;
import mekanism.common.base.HolidayManager;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.mekasuit.ModuleVisionEnhancementUnit;
import mekanism.common.item.gear.ItemFlamethrower;
import mekanism.common.item.gear.ItemHDPEElytra;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.item.interfaces.IJetpackItem;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.radial.IGenericRadialModeItem;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.network.to_server.PacketModeChange;
import mekanism.common.network.to_server.PacketPortableTeleporterTeleport;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismModules;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmorStandModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.FogType;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.client.event.InputEvent.MouseScrollingEvent;
import net.minecraftforge.client.event.RenderLivingEvent.Post;
import net.minecraftforge.client.event.RenderLivingEvent.Pre;
import net.minecraftforge.client.event.ViewportEvent.ComputeFogColor;
import net.minecraftforge.client.event.ViewportEvent.RenderFog;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientTickHandler {
   public static final Minecraft minecraft = Minecraft.m_91087_();
   public static final Random rand = new Random();
   public static final Map<Player, ClientTickHandler.TeleportData> portableTeleports = new Object2ObjectArrayMap(1);
   private static final ScrollIncrementer scrollIncrementer = new ScrollIncrementer(true);
   public static boolean firstTick = true;
   public static boolean visionEnhancement = false;
   public boolean initHoliday = false;
   public boolean shouldReset = false;

   public static boolean isJetpackInUse(Player player, ItemStack jetpack) {
      if (!player.m_5833_() && !jetpack.m_41619_()) {
         IJetpackItem.JetpackMode mode = ((IJetpackItem)jetpack.m_41720_()).getJetpackMode(jetpack);
         boolean guiOpen = minecraft.f_91080_ != null;
         boolean ascending = minecraft.f_91074_.f_108618_.f_108572_;
         boolean rising = ascending && !guiOpen;
         if (mode == IJetpackItem.JetpackMode.NORMAL) {
            return rising;
         }

         if (mode == IJetpackItem.JetpackMode.HOVER) {
            boolean descending = minecraft.f_91074_.f_108618_.f_108573_;
            if (rising && !descending) {
               return true;
            }

            return !CommonPlayerTickHandler.isOnGroundOrSleeping(player);
         }
      }

      return false;
   }

   public static boolean isScubaMaskOn(Player player) {
      return player != minecraft.f_91074_
         ? Mekanism.playerState.isScubaMaskOn(player)
         : CommonPlayerTickHandler.isScubaMaskOn(player, player.m_6844_(EquipmentSlot.CHEST));
   }

   public static boolean isGravitationalModulationOn(Player player) {
      return player != minecraft.f_91074_
         ? Mekanism.playerState.isGravitationalModulationOn(player)
         : CommonPlayerTickHandler.isGravitationalModulationOn(player);
   }

   public static boolean isVisionEnhancementOn(Player player) {
      IModule<ModuleVisionEnhancementUnit> module = IModuleHelper.INSTANCE.load(player.m_6844_(EquipmentSlot.HEAD), MekanismModules.VISION_ENHANCEMENT_UNIT);
      return module != null && module.isEnabled() && module.hasEnoughEnergy(MekanismConfig.gear.mekaSuitEnergyUsageVisionEnhancement);
   }

   public static boolean isFlamethrowerOn(Player player) {
      return player != minecraft.f_91074_ ? Mekanism.playerState.isFlamethrowerOn(player) : hasFlamethrower(player) && minecraft.f_91066_.f_92095_.m_90857_();
   }

   public static boolean hasFlamethrower(Player player) {
      ItemStack currentItem = player.m_150109_().m_36056_();
      return !currentItem.m_41619_() && currentItem.m_41720_() instanceof ItemFlamethrower && ChemicalUtil.hasGas(currentItem);
   }

   public static void portableTeleport(Player player, InteractionHand hand, Frequency.FrequencyIdentity identity) {
      int delay = MekanismConfig.gear.portableTeleporterDelay.get();
      if (delay == 0) {
         Mekanism.packetHandler().sendToServer(new PacketPortableTeleporterTeleport(hand, identity));
      } else {
         portableTeleports.put(player, new ClientTickHandler.TeleportData(hand, identity, minecraft.f_91073_.m_46467_() + delay));
      }
   }

   @SubscribeEvent
   public void onTick(ClientTickEvent event) {
      if (event.phase == Phase.START) {
         this.tickStart();
      }
   }

   public void tickStart() {
      MekanismClient.ticksPassed++;
      if (firstTick && minecraft.f_91073_ != null) {
         MekanismClient.launchClient();
         firstTick = false;
      }

      if (minecraft.f_91073_ != null) {
         this.shouldReset = true;
      } else if (this.shouldReset) {
         MekanismClient.reset();
         this.shouldReset = false;
      }

      if (minecraft.f_91073_ != null && minecraft.f_91074_ != null && !minecraft.m_91104_()) {
         if (!this.initHoliday || MekanismClient.ticksPassed % 1200L == 0L) {
            HolidayManager.notify(Minecraft.m_91087_().f_91074_);
            this.initHoliday = true;
         }

         SoundHandler.restartSounds();
         RadiationManager.get().tickClient(minecraft.f_91074_);
         UUID playerUUID = minecraft.f_91074_.m_20148_();
         ItemStack jetpack = IJetpackItem.getActiveJetpack(minecraft.f_91074_);
         boolean jetpackInUse = isJetpackInUse(minecraft.f_91074_, jetpack);
         Mekanism.playerState.setJetpackState(playerUUID, jetpackInUse, true);
         Mekanism.playerState.setScubaMaskState(playerUUID, isScubaMaskOn(minecraft.f_91074_), true);
         Mekanism.playerState.setGravitationalModulationState(playerUUID, isGravitationalModulationOn(minecraft.f_91074_), true);
         Mekanism.playerState.setFlamethrowerState(playerUUID, hasFlamethrower(minecraft.f_91074_), isFlamethrowerOn(minecraft.f_91074_), true);
         Iterator<Entry<Player, ClientTickHandler.TeleportData>> iter = portableTeleports.entrySet().iterator();

         while (iter.hasNext()) {
            Entry<Player, ClientTickHandler.TeleportData> entry = iter.next();
            Player player = entry.getKey();

            for (int i = 0; i < 100; i++) {
               double x = player.m_20185_() + rand.nextDouble() - 0.5;
               double y = player.m_20186_() + rand.nextDouble() * 2.0 - 2.0;
               double z = player.m_20189_() + rand.nextDouble() - 0.5;
               minecraft.f_91073_.m_7106_(ParticleTypes.f_123760_, x, y, z, 0.0, 1.0, 0.0);
            }

            ClientTickHandler.TeleportData data = entry.getValue();
            if (minecraft.f_91073_.m_46467_() == data.teleportTime) {
               Mekanism.packetHandler().sendToServer(new PacketPortableTeleporterTeleport(data.hand, data.identity));
               iter.remove();
            }
         }

         if (!jetpack.m_41619_()) {
            ItemStack primaryJetpack = IJetpackItem.getPrimaryJetpack(minecraft.f_91074_);
            if (!primaryJetpack.m_41619_()) {
               IJetpackItem.JetpackMode primaryMode = ((IJetpackItem)primaryJetpack.m_41720_()).getJetpackMode(primaryJetpack);
               IJetpackItem.JetpackMode mode = IJetpackItem.getPlayerJetpackMode(minecraft.f_91074_, primaryMode, () -> minecraft.f_91074_.f_108618_.f_108572_);
               MekanismClient.updateKey(minecraft.f_91074_.f_108618_.f_108572_, 0);
               if (jetpackInUse && IJetpackItem.handleJetpackMotion(minecraft.f_91074_, mode, () -> minecraft.f_91074_.f_108618_.f_108572_)) {
                  minecraft.f_91074_.m_183634_();
               }
            }
         }

         if (isScubaMaskOn(minecraft.f_91074_) && minecraft.f_91074_.m_20146_() == minecraft.f_91074_.m_6062_()) {
            for (MobEffectInstance effect : minecraft.f_91074_.m_21220_()) {
               if (MekanismUtils.shouldSpeedUpEffect(effect)) {
                  for (int i = 0; i < 9; i++) {
                     MekanismUtils.speedUpEffectSafely(minecraft.f_91074_, effect);
                  }
               }
            }
         }

         if (isVisionEnhancementOn(minecraft.f_91074_)) {
            visionEnhancement = true;
            minecraft.f_91074_.m_7292_(new MobEffectInstance(MobEffects.f_19611_, 220, 0, false, false, false));
         } else if (visionEnhancement) {
            visionEnhancement = false;
            MobEffectInstance effectx = minecraft.f_91074_.m_21124_(MobEffects.f_19611_);
            if (effectx != null && effectx.m_19557_() <= 220) {
               minecraft.f_91074_.m_21195_(MobEffects.f_19611_);
            }
         }

         if ((minecraft.f_91080_ == null || minecraft.f_91080_ instanceof GuiRadialSelector)
            && (!MekKeyHandler.isRadialPressed() || !this.updateSelectorRenderer(EquipmentSlot.MAINHAND) && !this.updateSelectorRenderer(EquipmentSlot.OFFHAND))
            && minecraft.f_91080_ != null) {
            minecraft.m_91152_(null);
         }

         if (MekanismConfig.client.enablePlayerSounds.get()) {
            RadiationManager.RadiationScale scale = RadiationManager.get().getClientScale();
            if (scale != RadiationManager.RadiationScale.NONE && !SoundHandler.radiationSoundMap.containsKey(scale)) {
               GeigerSound sound = GeigerSound.create(minecraft.f_91074_, scale);
               SoundHandler.radiationSoundMap.put(scale, sound);
               SoundHandler.playSound(sound);
            }
         }
      }
   }

   private boolean updateSelectorRenderer(EquipmentSlot slot) {
      if (minecraft.f_91074_ != null) {
         ItemStack stack = minecraft.f_91074_.m_6844_(slot);
         if (stack.m_41720_() instanceof IGenericRadialModeItem item) {
            RadialData<?> radialData = item.getRadialData(stack);
            if (radialData != null) {
               if (!(minecraft.f_91080_ instanceof GuiRadialSelector screen && screen.hasMatchingData(slot, radialData))) {
                  GuiRadialSelector newSelector = new GuiRadialSelector(slot, radialData, () -> minecraft.f_91074_);
                  newSelector.tryInheritCurrentPath(minecraft.f_91080_);
                  minecraft.m_91152_(newSelector);
               }

               return true;
            }
         }
      }

      return false;
   }

   @SubscribeEvent
   public void onMouseEvent(MouseScrollingEvent event) {
      if (MekanismConfig.client.allowModeScroll.get() && minecraft.f_91074_ != null && minecraft.f_91074_.m_6144_()) {
         this.handleModeScroll(event, EquipmentSlot.MAINHAND, event.getScrollDelta());
      }
   }

   private void handleModeScroll(Event event, EquipmentSlot slot, double delta) {
      if (delta != 0.0 && IModeItem.isModeItem(minecraft.f_91074_, slot)) {
         int shift = scrollIncrementer.scroll(delta);
         if (shift != 0) {
            MekanismStatusOverlay.INSTANCE.setTimer();
            Mekanism.packetHandler().sendToServer(new PacketModeChange(slot, shift));
         }

         event.setCanceled(true);
      }
   }

   @SubscribeEvent
   public void onFogLighting(ComputeFogColor event) {
      if (visionEnhancement) {
         float oldRatio = 0.1F;
         float newRatio = 1.0F - oldRatio;
         float red = oldRatio * event.getRed();
         float green = oldRatio * event.getGreen();
         float blue = oldRatio * event.getBlue();
         event.setRed(red + newRatio * 0.4F);
         event.setGreen(green + newRatio * 0.8F);
         event.setBlue(blue + newRatio * 0.4F);
      }
   }

   @SubscribeEvent
   public void onFog(RenderFog event) {
      if (visionEnhancement && event.getCamera().m_90592_() instanceof Player player) {
         IModule<ModuleVisionEnhancementUnit> module = IModuleHelper.INSTANCE.load(player.m_6844_(EquipmentSlot.HEAD), MekanismModules.VISION_ENHANCEMENT_UNIT);
         if (module != null) {
            event.setNearPlaneDistance(-8.0F);
            if (event.getFarPlaneDistance() < 20.0F) {
               float scalar;
               if (event.getType() == FogType.LAVA) {
                  scalar = 24.0F * event.getFarPlaneDistance();
               } else {
                  scalar = 5.0F + 2.5F * (float)Math.pow(Math.E, 0.16F * event.getFarPlaneDistance());
               }

               event.setFarPlaneDistance(Math.min(192.0F, scalar));
            }

            event.scaleFarPlaneDistance((float)Math.pow(module.getInstalledCount(), 1.25) / module.getData().getMaxStackSize());
            event.setCanceled(true);
         }
      }
   }

   @SubscribeEvent
   public void recipesUpdated(RecipesUpdatedEvent event) {
      MekanismRecipeType.clearCache();
   }

   @SubscribeEvent
   public void renderEntityPre(Pre<?, ?> evt) {
      if (evt.getRenderer().m_7200_() instanceof HumanoidModel<?> humanoidModel) {
         setModelVisibility(evt.getEntity(), humanoidModel, false);
      }
   }

   @SubscribeEvent
   public void renderEntityPost(Post<?, ?> evt) {
      if (evt.getRenderer().m_7200_() instanceof HumanoidModel<?> humanoidModel) {
         setModelVisibility(evt.getEntity(), humanoidModel, true);
      }
   }

   private static void setModelVisibility(LivingEntity entity, HumanoidModel<?> entityModel, boolean showModel) {
      if (entity.m_6844_(EquipmentSlot.HEAD).m_41720_() instanceof ItemMekaSuitArmor) {
         entityModel.f_102808_.f_104207_ = showModel;
         entityModel.f_102809_.f_104207_ = showModel;
         if (entityModel instanceof PlayerModel<?> playerModel) {
            playerModel.f_103379_.f_104207_ = showModel;
         }
      }

      ItemStack chest = entity.m_6844_(EquipmentSlot.CHEST);
      if (chest.m_41720_() instanceof ItemMekaSuitArmor) {
         entityModel.f_102810_.f_104207_ = showModel;
         if (!(entity instanceof ArmorStand)) {
            entityModel.f_102812_.f_104207_ = showModel;
            entityModel.f_102811_.f_104207_ = showModel;
         }

         if (entityModel instanceof PlayerModel<?> playerModel) {
            playerModel.f_103373_.f_104207_ = showModel;
            playerModel.f_103378_.f_104207_ = showModel;
            playerModel.f_103374_.f_104207_ = showModel;
            playerModel.f_103375_.f_104207_ = showModel;
         } else if (entityModel instanceof ArmorStandModel armorStandModel) {
            armorStandModel.f_170353_.f_104207_ = showModel;
            armorStandModel.f_170354_.f_104207_ = showModel;
            armorStandModel.f_102139_.f_104207_ = showModel;
         }
      } else if (chest.m_41720_() instanceof ItemHDPEElytra && entityModel instanceof PlayerModel<?> playerModel) {
         playerModel.f_103373_.f_104207_ = showModel;
      }

      if (entity.m_6844_(EquipmentSlot.LEGS).m_41720_() instanceof ItemMekaSuitArmor) {
         entityModel.f_102814_.f_104207_ = showModel;
         entityModel.f_102813_.f_104207_ = showModel;
         if (entityModel instanceof PlayerModel<?> playerModel) {
            playerModel.f_103376_.f_104207_ = showModel;
            playerModel.f_103377_.f_104207_ = showModel;
         }
      }
   }

   private record TeleportData(InteractionHand hand, Frequency.FrequencyIdentity identity, long teleportTime) {
   }
}
