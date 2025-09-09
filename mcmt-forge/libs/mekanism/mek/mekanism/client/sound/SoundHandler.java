package mekanism.client.sound;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import mekanism.api.Upgrade;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.registration.impl.SoundEventRegistryObject;
import mekanism.common.tile.interfaces.ITileSound;
import mekanism.common.tile.interfaces.IUpgradeTile;
import mekanism.common.util.WorldUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance.Attenuation;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.event.sound.SoundEngineLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(
   modid = "mekanism",
   value = {Dist.CLIENT},
   bus = Bus.MOD
)
public class SoundHandler {
   private static final Map<UUID, PlayerSound> jetpackSounds = new Object2ObjectOpenHashMap();
   private static final Map<UUID, PlayerSound> scubaMaskSounds = new Object2ObjectOpenHashMap();
   private static final Map<UUID, PlayerSound[]> flamethrowerSounds = new Object2ObjectOpenHashMap();
   private static final Map<UUID, PlayerSound> gravitationalModulationSounds = new Object2ObjectOpenHashMap();
   public static final Map<RadiationManager.RadiationScale, GeigerSound> radiationSoundMap = new EnumMap<>(RadiationManager.RadiationScale.class);
   private static final Long2ObjectMap<SoundInstance> soundMap = new Long2ObjectOpenHashMap();
   private static boolean IN_MUFFLED_CHECK = false;
   private static SoundEngine soundEngine;
   private static boolean hadPlayerSounds;

   private SoundHandler() {
   }

   public static void clearPlayerSounds() {
      jetpackSounds.clear();
      scubaMaskSounds.clear();
      flamethrowerSounds.clear();
      gravitationalModulationSounds.clear();
   }

   public static void clearPlayerSounds(UUID uuid) {
      jetpackSounds.remove(uuid);
      scubaMaskSounds.remove(uuid);
      flamethrowerSounds.remove(uuid);
      gravitationalModulationSounds.remove(uuid);
   }

   public static void startSound(@NotNull LevelAccessor world, @NotNull UUID uuid, @NotNull PlayerSound.SoundType soundType) {
      switch (soundType) {
         case JETPACK:
            startSound(world, uuid, jetpackSounds, JetpackSound::new);
            break;
         case SCUBA_MASK:
            startSound(world, uuid, scubaMaskSounds, ScubaMaskSound::new);
            break;
         case FLAMETHROWER:
            startSounds(world, uuid, flamethrowerSounds, FlamethrowerSound.Active::new, FlamethrowerSound.Idle::new);
            break;
         case GRAVITATIONAL_MODULATOR:
            startSound(world, uuid, gravitationalModulationSounds, GravitationalModulationSound::new);
      }
   }

   private static void startSound(LevelAccessor world, UUID uuid, Map<UUID, PlayerSound> knownSounds, Function<Player, PlayerSound> soundCreator) {
      if (knownSounds.containsKey(uuid)) {
         if (playerSoundsEnabled()) {
            restartSounds(knownSounds.get(uuid));
         }
      } else {
         Player player = world.m_46003_(uuid);
         if (player != null) {
            PlayerSound sound = soundCreator.apply(player);
            playSound(sound);
            knownSounds.put(uuid, sound);
         }
      }
   }

   @SafeVarargs
   private static void startSounds(LevelAccessor world, UUID uuid, Map<UUID, PlayerSound[]> knownSounds, Function<Player, PlayerSound>... soundCreators) {
      if (knownSounds.containsKey(uuid)) {
         if (playerSoundsEnabled()) {
            restartSounds(knownSounds.get(uuid));
         }
      } else {
         Player player = world.m_46003_(uuid);
         if (player != null) {
            PlayerSound[] sounds = new PlayerSound[soundCreators.length];

            for (int i = 0; i < soundCreators.length; i++) {
               playSound(sounds[i] = soundCreators[i].apply(player));
            }

            knownSounds.put(uuid, sounds);
         }
      }
   }

   public static void restartSounds() {
      boolean hasPlayerSounds = playerSoundsEnabled();
      if (hasPlayerSounds != hadPlayerSounds) {
         hadPlayerSounds = hasPlayerSounds;
         if (hasPlayerSounds) {
            jetpackSounds.values().forEach(xva$0 -> restartSounds(xva$0));
            scubaMaskSounds.values().forEach(xva$0 -> restartSounds(xva$0));
            flamethrowerSounds.values().forEach(SoundHandler::restartSounds);
            gravitationalModulationSounds.values().forEach(xva$0 -> restartSounds(xva$0));
            radiationSoundMap.values().forEach(xva$0 -> restartSounds(xva$0));
         }
      }
   }

   private static void restartSounds(PlayerSound... sounds) {
      for (PlayerSound sound : sounds) {
         if (!sound.m_7801_() && soundEngine != null && !soundEngine.f_120226_.containsKey(sound)) {
            playSound(sound);
         }
      }
   }

   private static boolean playerSoundsEnabled() {
      return getVolume(SoundSource.MASTER) > 0.0F && getVolume(SoundSource.PLAYERS) > 0.0F;
   }

   private static float getVolume(SoundSource category) {
      return Minecraft.m_91087_().f_91066_.m_92147_(category);
   }

   public static void playSound(SoundEventRegistryObject<?> soundEventRO) {
      playSound(soundEventRO.get());
   }

   public static void playSound(SoundEvent sound) {
      playSound(SimpleSoundInstance.m_119755_(sound, 1.0F, MekanismConfig.client.baseSoundVolume.get()));
   }

   public static void playSound(SoundInstance sound) {
      Minecraft.m_91087_().m_91106_().m_120367_(sound);
   }

   public static SoundInstance startTileSound(SoundEvent soundEvent, SoundSource category, float volume, RandomSource random, BlockPos pos) {
      return startTileSound(soundEvent, category, volume, random, pos, true);
   }

   public static SoundInstance startTileSound(SoundEvent soundEvent, SoundSource category, float volume, RandomSource random, BlockPos pos, boolean looping) {
      SoundInstance s = (SoundInstance)soundMap.get(pos.m_121878_());
      if (s == null || !Minecraft.m_91087_().m_91106_().m_120403_(s)) {
         SoundInstance var7 = new SoundHandler.TileTickableSound(soundEvent, category, random, pos, volume, looping);
         if (!isClientPlayerInRange(var7)) {
            return null;
         }

         playSound(var7);
         s = (SoundInstance)soundMap.get(pos.m_121878_());
      }

      return s;
   }

   public static void stopTileSound(BlockPos pos) {
      long posKey = pos.m_121878_();
      SoundInstance s = (SoundInstance)soundMap.get(posKey);
      if (s != null) {
         Minecraft.m_91087_().m_91106_().m_120399_(s);
         soundMap.remove(posKey);
      }
   }

   private static boolean isClientPlayerInRange(SoundInstance sound) {
      if (!sound.m_7796_() && sound.m_7438_() != Attenuation.NONE) {
         Player player = Minecraft.m_91087_().f_91074_;
         if (player == null) {
            return false;
         } else {
            Sound s = sound.m_5891_();
            if (s == null) {
               sound.m_6775_(Minecraft.m_91087_().m_91106_());
               s = sound.m_5891_();
            }

            int attenuationDistance = s.m_119798_();
            float scaledDistance = Math.max(sound.m_7769_(), 1.0F) * attenuationDistance;
            return player.m_20182_().m_82531_(sound.m_7772_(), sound.m_7780_(), sound.m_7778_()) < scaledDistance * scaledDistance;
         }
      } else {
         return true;
      }
   }

   @SubscribeEvent
   public static void onSoundEngineSetup(SoundEngineLoadEvent event) {
      if (soundEngine == null) {
         soundEngine = event.getEngine();
      }
   }

   public static void onTilePlaySound(PlaySoundEvent event) {
      SoundInstance resultSound = event.getSound();
      if (resultSound != null && !IN_MUFFLED_CHECK) {
         ResourceLocation soundLoc = event.getOriginalSound().m_7904_();
         if (soundLoc.m_135827_().startsWith("mekanism")) {
            if (event.getOriginalSound() instanceof PlayerSound sound) {
               event.setSound(sound);
            } else {
               if (event.getName().startsWith("tile.")) {
                  BlockPos pos = BlockPos.m_274561_(resultSound.m_7772_() - 0.5, resultSound.m_7780_() - 0.5, resultSound.m_7778_() - 0.5);
                  soundMap.put(pos.m_121878_(), resultSound);
               }
            }
         }
      }
   }

   private static class TileTickableSound extends AbstractTickableSoundInstance {
      private final float originalVolume;
      private final int checkInterval = 20 + ThreadLocalRandom.current().nextInt(20);

      TileTickableSound(SoundEvent soundEvent, SoundSource category, RandomSource random, BlockPos pos, float volume, boolean looping) {
         super(soundEvent, category, random);
         this.originalVolume = volume * MekanismConfig.client.baseSoundVolume.get();
         this.f_119575_ = pos.m_123341_() + 0.5F;
         this.f_119576_ = pos.m_123342_() + 0.5F;
         this.f_119577_ = pos.m_123343_() + 0.5F;
         this.f_119573_ = this.originalVolume * this.getTileVolumeFactor();
         this.f_119578_ = looping;
         this.f_119579_ = 0;
      }

      public void m_7788_() {
         if (Minecraft.m_91087_().f_91073_.m_46467_() % this.checkInterval == 0L) {
            if (!SoundHandler.isClientPlayerInRange(this)) {
               this.m_119609_();
               return;
            }

            SoundHandler.IN_MUFFLED_CHECK = true;
            this.f_119573_ = this.originalVolume;
            SoundInstance s = ForgeHooksClient.playSound(SoundHandler.soundEngine, this);
            SoundHandler.IN_MUFFLED_CHECK = false;
            if (s == this) {
               this.f_119573_ = this.originalVolume * this.getTileVolumeFactor();
            } else if (s == null) {
               this.m_119609_();
            } else {
               this.f_119573_ = s.m_7769_() * this.getTileVolumeFactor();
            }
         }
      }

      private float getTileVolumeFactor() {
         BlockEntity tile = WorldUtils.getTileEntity(Minecraft.m_91087_().f_91073_, BlockPos.m_274561_(this.m_7772_(), this.m_7780_(), this.m_7778_()));
         float retVolume = 1.0F;
         if (tile instanceof IUpgradeTile upgradeTile && upgradeTile.supportsUpgrade(Upgrade.MUFFLING)) {
            int mufflerCount = upgradeTile.getComponent().getUpgrades(Upgrade.MUFFLING);
            retVolume = 1.0F - (float)mufflerCount / Upgrade.MUFFLING.getMax();
         }

         if (tile instanceof ITileSound tileSound) {
            retVolume *= tileSound.getVolume();
         }

         return retVolume;
      }

      public float m_7769_() {
         if (this.f_119570_ == null) {
            this.m_6775_(Minecraft.m_91087_().m_91106_());
         }

         return super.m_7769_();
      }

      public boolean m_7784_() {
         return true;
      }
   }
}
