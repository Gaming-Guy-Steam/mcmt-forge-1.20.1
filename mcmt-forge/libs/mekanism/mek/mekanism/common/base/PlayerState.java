package mekanism.common.base;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import mekanism.api.functions.FloatSupplier;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.math.FloatingLong;
import mekanism.client.sound.PlayerSound;
import mekanism.client.sound.SoundHandler;
import mekanism.common.CommonPlayerTickHandler;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.mekasuit.ModuleGravitationalModulatingUnit;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.network.to_client.PacketResetPlayerClient;
import mekanism.common.network.to_server.PacketGearStateUpdate;
import mekanism.common.registration.impl.GameEventRegistryObject;
import mekanism.common.registries.MekanismGameEvents;
import mekanism.common.registries.MekanismModules;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;

public class PlayerState {
   private static final UUID STEP_ASSIST_MODIFIER_UUID = UUID.fromString("026E638A-570D-48F2-BA91-3E86BBB26576");
   private static final UUID SWIM_BOOST_MODIFIER_UUID = UUID.fromString("B8BEEC12-741C-47C3-A74D-AA00F0D2ACF0");
   private final Set<UUID> activeJetpacks = Collections.newSetFromMap(new ConcurrentHashMap<>());
   private final Set<UUID> activeScubaMasks = Collections.newSetFromMap(new ConcurrentHashMap<>());
   private final Set<UUID> activeGravitationalModulators = Collections.newSetFromMap(new ConcurrentHashMap<>());
   private final Set<UUID> activeFlamethrowers = Collections.newSetFromMap(new ConcurrentHashMap<>());
   private final Map<UUID, PlayerState.FlightInfo> flightInfoMap = new ConcurrentHashMap<>();
   private LevelAccessor world;

   public void clear(boolean isRemote) {
      this.activeJetpacks.clear();
      this.activeScubaMasks.clear();
      this.activeGravitationalModulators.clear();
      this.activeFlamethrowers.clear();
      if (isRemote) {
         SoundHandler.clearPlayerSounds();
      } else {
         this.flightInfoMap.clear();
      }
   }

   public void clearPlayer(UUID uuid, boolean isRemote) {
      this.activeJetpacks.remove(uuid);
      this.activeScubaMasks.remove(uuid);
      this.activeGravitationalModulators.remove(uuid);
      this.activeFlamethrowers.remove(uuid);
      if (isRemote) {
         SoundHandler.clearPlayerSounds(uuid);
         if (Minecraft.m_91087_().f_91074_ == null || Minecraft.m_91087_().f_91074_.m_20148_().equals(uuid)) {
            SoundHandler.radiationSoundMap.clear();
         }
      }

      RadiationManager.get().resetPlayer(uuid);
      if (!isRemote) {
         Mekanism.packetHandler().sendToAll(new PacketResetPlayerClient(uuid));
      }
   }

   public void clearPlayerServerSideOnly(UUID uuid) {
      this.flightInfoMap.remove(uuid);
   }

   public void reapplyServerSideOnly(Player player) {
      UUID uuid = player.m_20148_();
      PlayerState.FlightInfo flightInfo = this.flightInfoMap.get(uuid);
      if (flightInfo != null && (flightInfo.wasFlyingAllowed || flightInfo.wasFlying)) {
         this.updateClientServerFlight(player, flightInfo.wasFlyingAllowed, flightInfo.wasFlying);
      }
   }

   public void init(LevelAccessor world) {
      this.world = world;
   }

   public void setJetpackState(UUID uuid, boolean isActive, boolean isLocal) {
      boolean alreadyActive = this.isJetpackOn(uuid);
      boolean changed = alreadyActive != isActive;
      if (alreadyActive && !isActive) {
         this.activeJetpacks.remove(uuid);
      } else if (!alreadyActive && isActive) {
         this.activeJetpacks.add(uuid);
      }

      if (changed && this.world.m_5776_()) {
         if (isLocal) {
            Mekanism.packetHandler().sendToServer(new PacketGearStateUpdate(PacketGearStateUpdate.GearType.JETPACK, uuid, isActive));
         }

         if (isActive && MekanismConfig.client.enablePlayerSounds.get()) {
            SoundHandler.startSound(this.world, uuid, PlayerSound.SoundType.JETPACK);
         }
      }
   }

   public boolean isJetpackOn(Player p) {
      return this.isJetpackOn(p.m_20148_());
   }

   public boolean isJetpackOn(UUID uuid) {
      return this.activeJetpacks.contains(uuid);
   }

   public void setScubaMaskState(UUID uuid, boolean isActive, boolean isLocal) {
      boolean alreadyActive = this.isScubaMaskOn(uuid);
      boolean changed = alreadyActive != isActive;
      if (alreadyActive && !isActive) {
         this.activeScubaMasks.remove(uuid);
      } else if (!alreadyActive && isActive) {
         this.activeScubaMasks.add(uuid);
      }

      if (changed && this.world.m_5776_()) {
         if (isLocal) {
            Mekanism.packetHandler().sendToServer(new PacketGearStateUpdate(PacketGearStateUpdate.GearType.SCUBA_MASK, uuid, isActive));
         }

         if (isActive && MekanismConfig.client.enablePlayerSounds.get()) {
            SoundHandler.startSound(this.world, uuid, PlayerSound.SoundType.SCUBA_MASK);
         }
      }
   }

   public boolean isScubaMaskOn(Player p) {
      return this.isScubaMaskOn(p.m_20148_());
   }

   public boolean isScubaMaskOn(UUID uuid) {
      return this.activeScubaMasks.contains(uuid);
   }

   public void updateStepAssist(Player player) {
      this.updateAttribute(
         player, (Attribute)ForgeMod.STEP_HEIGHT_ADDITION.get(), STEP_ASSIST_MODIFIER_UUID, "Step Assist", () -> CommonPlayerTickHandler.getStepBoost(player)
      );
   }

   public void updateSwimBoost(Player player) {
      this.updateAttribute(
         player, (Attribute)ForgeMod.SWIM_SPEED.get(), SWIM_BOOST_MODIFIER_UUID, "Swim Boost", () -> CommonPlayerTickHandler.getSwimBoost(player)
      );
   }

   private void updateAttribute(Player player, Attribute attribute, UUID uuid, String name, FloatSupplier additionalSupplier) {
      AttributeInstance attributeInstance = player.m_21051_(attribute);
      if (attributeInstance != null) {
         AttributeModifier existing = attributeInstance.m_22111_(uuid);
         float additional = additionalSupplier.getAsFloat();
         if (existing != null) {
            if (existing.m_22218_() == additional) {
               return;
            }

            attributeInstance.m_22130_(existing);
         }

         if (additional > 0.0F) {
            attributeInstance.m_22118_(new AttributeModifier(uuid, name, additional, Operation.ADDITION));
         }
      }
   }

   public void setGravitationalModulationState(UUID uuid, boolean isActive, boolean isLocal) {
      boolean alreadyActive = this.isGravitationalModulationOn(uuid);
      boolean changed = alreadyActive != isActive;
      if (alreadyActive && !isActive) {
         this.activeGravitationalModulators.remove(uuid);
      } else if (!alreadyActive && isActive) {
         this.activeGravitationalModulators.add(uuid);
      }

      if (changed && this.world.m_5776_()) {
         if (isLocal) {
            Mekanism.packetHandler().sendToServer(new PacketGearStateUpdate(PacketGearStateUpdate.GearType.GRAVITATIONAL_MODULATOR, uuid, isActive));
         }

         if (isActive && MekanismConfig.client.enablePlayerSounds.get()) {
            SoundHandler.startSound(this.world, uuid, PlayerSound.SoundType.GRAVITATIONAL_MODULATOR);
         }
      }
   }

   public boolean isGravitationalModulationOn(Player p) {
      return this.isGravitationalModulationOn(p.m_20148_());
   }

   public boolean isGravitationalModulationOn(UUID uuid) {
      return this.activeGravitationalModulators.contains(uuid);
   }

   public void updateFlightInfo(Player player) {
      boolean isFlyingGameMode = !MekanismUtils.isPlayingMode(player);
      boolean hasGravitationalModulator = CommonPlayerTickHandler.isGravitationalModulationReady(player);
      PlayerState.FlightInfo flightInfo = this.flightInfoMap.computeIfAbsent(player.m_20148_(), uuid -> new PlayerState.FlightInfo());
      if (!isFlyingGameMode && !hasGravitationalModulator) {
         if (flightInfo.hadFlightItem) {
            if (player.m_150110_().f_35936_) {
               this.updateClientServerFlight(player, false);
            }

            flightInfo.hadFlightItem = false;
         }

         flightInfo.wasFlyingGameMode = false;
         flightInfo.wasFlying = player.m_150110_().f_35935_;
         flightInfo.wasFlyingAllowed = player.m_150110_().f_35936_;
      } else {
         if (!flightInfo.hadFlightItem) {
            if (!player.m_150110_().f_35936_) {
               this.updateClientServerFlight(player, true);
            }

            flightInfo.hadFlightItem = true;
         } else if (flightInfo.wasFlyingGameMode && !isFlyingGameMode) {
            this.updateClientServerFlight(player, true, flightInfo.wasFlying);
         } else if (flightInfo.wasFlyingAllowed && !player.m_150110_().f_35936_) {
            this.updateClientServerFlight(player, true, flightInfo.wasFlying);
         }

         flightInfo.wasFlyingGameMode = isFlyingGameMode;
         flightInfo.wasFlying = player.m_150110_().f_35935_;
         flightInfo.wasFlyingAllowed = player.m_150110_().f_35936_;
         if (player.m_150110_().f_35935_ && hasGravitationalModulator) {
            IModule<ModuleGravitationalModulatingUnit> module = IModuleHelper.INSTANCE
               .load(player.m_6844_(EquipmentSlot.CHEST), MekanismModules.GRAVITATIONAL_MODULATING_UNIT);
            if (module != null) {
               FloatingLong usage = MekanismConfig.gear.mekaSuitEnergyUsageGravitationalModulation.get();
               GameEventRegistryObject<GameEvent> gameEvent = MekanismGameEvents.GRAVITY_MODULATE;
               if (Mekanism.keyMap.has(player.m_20148_(), 1)) {
                  FloatingLong boostUsage = usage.multiply(4L);
                  if (module.canUseEnergy(player, boostUsage, false)) {
                     float boost = module.getCustomInstance().getBoost();
                     if (boost > 0.0F) {
                        player.m_19920_(boost, new Vec3(0.0, 0.0, 1.0));
                        usage = boostUsage;
                        gameEvent = MekanismGameEvents.GRAVITY_MODULATE_BOOSTED;
                     }
                  }
               }

               module.useEnergy(player, usage);
               if (MekanismConfig.gear.mekaSuitGravitationalVibrations.get() && player.m_9236_().m_46467_() % 10L == 0L) {
                  player.m_146850_(gameEvent.get());
               }
            }
         }
      }
   }

   private void updateClientServerFlight(Player player, boolean allowFlying) {
      this.updateClientServerFlight(player, allowFlying, allowFlying && player.m_150110_().f_35935_);
   }

   private void updateClientServerFlight(Player player, boolean allowFlying, boolean isFlying) {
      player.m_150110_().f_35936_ = allowFlying;
      player.m_150110_().f_35935_ = isFlying;
      if (player instanceof ServerPlayer) {
         player.m_6885_();
      }
   }

   public void setFlamethrowerState(UUID uuid, boolean isActive, boolean isLocal) {
      this.setFlamethrowerState(uuid, isActive, isActive, isLocal);
   }

   public void setFlamethrowerState(UUID uuid, boolean hasFlameThrower, boolean isActive, boolean isLocal) {
      boolean alreadyActive = this.isFlamethrowerOn(uuid);
      boolean changed = alreadyActive != isActive;
      if (alreadyActive && !isActive) {
         this.activeFlamethrowers.remove(uuid);
      } else if (!alreadyActive && isActive) {
         this.activeFlamethrowers.add(uuid);
      }

      if (this.world == null) {
         throw new NullPointerException(
            "mekanism.common.base.PlayerState#world is null. This should not happen. Optifine is known to cause this on client side."
         );
      } else {
         if (this.world.m_5776_()) {
            boolean startSound;
            if (changed) {
               if (isLocal) {
                  Mekanism.packetHandler().sendToServer(new PacketGearStateUpdate(PacketGearStateUpdate.GearType.FLAMETHROWER, uuid, isActive));
               }

               startSound = isActive;
            } else {
               startSound = !isActive && hasFlameThrower;
            }

            if (startSound && MekanismConfig.client.enablePlayerSounds.get()) {
               SoundHandler.startSound(this.world, uuid, PlayerSound.SoundType.FLAMETHROWER);
            }
         }
      }
   }

   public boolean isFlamethrowerOn(Player p) {
      return this.isFlamethrowerOn(p.m_20148_());
   }

   public boolean isFlamethrowerOn(UUID uuid) {
      return this.activeFlamethrowers.contains(uuid);
   }

   private static class FlightInfo {
      public boolean hadFlightItem;
      public boolean wasFlyingGameMode;
      public boolean wasFlyingAllowed;
      public boolean wasFlying;
   }
}
