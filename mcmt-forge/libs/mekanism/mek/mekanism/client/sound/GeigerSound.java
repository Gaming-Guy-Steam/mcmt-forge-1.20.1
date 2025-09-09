package mekanism.client.sound;

import java.util.Objects;
import mekanism.common.lib.radiation.RadiationManager;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class GeigerSound extends PlayerSound {
   private final RadiationManager.RadiationScale scale;

   public static GeigerSound create(@NotNull Player player, RadiationManager.RadiationScale scale) {
      if (scale == RadiationManager.RadiationScale.NONE) {
         throw new IllegalArgumentException("Can't create a GeigerSound with a RadiationScale of NONE.");
      } else {
         int subtitleFrequency;
         if (scale == RadiationManager.RadiationScale.MEDIUM) {
            subtitleFrequency = 50;
         } else if (scale == RadiationManager.RadiationScale.ELEVATED) {
            subtitleFrequency = 40;
         } else if (scale == RadiationManager.RadiationScale.HIGH) {
            subtitleFrequency = 30;
         } else if (scale == RadiationManager.RadiationScale.EXTREME) {
            subtitleFrequency = 20;
         } else {
            subtitleFrequency = 60;
         }

         return new GeigerSound(player, scale, subtitleFrequency);
      }
   }

   private GeigerSound(@NotNull Player player, RadiationManager.RadiationScale scale, int subtitleFrequency) {
      super(player, Objects.requireNonNull(scale.getSoundEvent()), subtitleFrequency);
      this.scale = scale;
      this.setFade(1.0F, 1.0F);
   }

   @Override
   public boolean shouldPlaySound(@NotNull Player player) {
      return this.scale == RadiationManager.get().getClientScale();
   }

   @Override
   public float m_7769_() {
      return super.m_7769_() * 0.05F;
   }
}
