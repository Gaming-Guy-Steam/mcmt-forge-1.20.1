package mekanism.client.sound;

import java.lang.ref.WeakReference;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registration.impl.SoundEventRegistryObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.sounds.SoundEventListener;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class PlayerSound extends AbstractTickableSoundInstance {
   @NotNull
   private final WeakReference<Player> playerReference;
   private final int subtitleFrequency;
   private float lastX;
   private float lastY;
   private float lastZ;
   private float fadeUpStep = 0.1F;
   private float fadeDownStep = 0.1F;
   private int consecutiveTicks;

   public PlayerSound(@NotNull Player player, @NotNull SoundEventRegistryObject<?> sound) {
      this(player, sound.get(), 60);
   }

   public PlayerSound(@NotNull Player player, @NotNull SoundEvent sound, int subtitleFrequency) {
      super(sound, SoundSource.PLAYERS, player.m_9236_().m_213780_());
      this.playerReference = new WeakReference<>(player);
      this.subtitleFrequency = subtitleFrequency;
      this.lastX = (float)player.m_20185_();
      this.lastY = (float)player.m_20186_();
      this.lastZ = (float)player.m_20189_();
      this.f_119578_ = true;
      this.f_119579_ = 0;
      this.f_119573_ = 0.1F;
   }

   @Nullable
   private Player getPlayer() {
      return this.playerReference.get();
   }

   protected void setFade(float fadeUpStep, float fadeDownStep) {
      this.fadeUpStep = fadeUpStep;
      this.fadeDownStep = fadeDownStep;
   }

   public double m_7772_() {
      Player player = this.getPlayer();
      if (player != null) {
         this.lastX = (float)player.m_20185_();
      }

      return this.lastX;
   }

   public double m_7780_() {
      Player player = this.getPlayer();
      if (player != null) {
         this.lastY = (float)player.m_20186_();
      }

      return this.lastY;
   }

   public double m_7778_() {
      Player player = this.getPlayer();
      if (player != null) {
         this.lastZ = (float)player.m_20189_();
      }

      return this.lastZ;
   }

   public void m_7788_() {
      Player player = this.getPlayer();
      if (player != null && player.m_6084_()) {
         if (this.shouldPlaySound(player)) {
            if (this.f_119573_ < 1.0F) {
               this.f_119573_ = Math.min(1.0F, this.f_119573_ + this.fadeUpStep);
            }

            if (this.consecutiveTicks % this.subtitleFrequency == 0) {
               SoundManager soundHandler = Minecraft.m_91087_().m_91106_();

               for (SoundEventListener soundEventListener : soundHandler.f_120349_.f_120231_) {
                  WeighedSoundEvents soundEventAccessor = this.m_6775_(soundHandler);
                  if (soundEventAccessor != null) {
                     soundEventListener.m_6985_(this, soundEventAccessor);
                  }
               }

               this.consecutiveTicks = 1;
            } else {
               this.consecutiveTicks++;
            }
         } else if (this.f_119573_ > 0.0F) {
            this.consecutiveTicks = 0;
            this.f_119573_ = Math.max(0.0F, this.f_119573_ - this.fadeDownStep);
         }
      } else {
         this.m_119609_();
         this.f_119573_ = 0.0F;
         this.consecutiveTicks = 0;
      }
   }

   public abstract boolean shouldPlaySound(@NotNull Player player);

   public float m_7769_() {
      return super.m_7769_() * MekanismConfig.client.baseSoundVolume.get();
   }

   public boolean m_7784_() {
      return true;
   }

   public boolean m_7767_() {
      Player player = this.getPlayer();
      return player == null ? super.m_7767_() : !player.m_20067_();
   }

   public static enum SoundType {
      FLAMETHROWER,
      JETPACK,
      SCUBA_MASK,
      GRAVITATIONAL_MODULATOR;
   }
}
