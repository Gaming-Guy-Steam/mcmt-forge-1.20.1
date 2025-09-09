package mekanism.common.base;

import com.mojang.authlib.GameProfile;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import mekanism.common.Mekanism;
import mekanism.common.util.MekanismUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;

public class MekFakePlayer extends FakePlayer {
   private static WeakReference<MekFakePlayer> INSTANCE;
   private UUID emulatingUUID = null;

   private MekFakePlayer(ServerLevel world) {
      super(world, new MekFakePlayer.FakeGameProfile());
      ((MekFakePlayer.FakeGameProfile)this.m_36316_()).myFakePlayer = this;
   }

   public boolean m_7301_(@NotNull MobEffectInstance effect) {
      return false;
   }

   public void setEmulatingUUID(UUID uuid) {
      this.emulatingUUID = uuid;
   }

   @NotNull
   public UUID m_20148_() {
      return this.emulatingUUID == null ? super.m_20148_() : this.emulatingUUID;
   }

   public static <R> R withFakePlayer(ServerLevel world, Function<MekFakePlayer, R> fakePlayerConsumer) {
      MekFakePlayer actual = INSTANCE == null ? null : INSTANCE.get();
      if (actual == null) {
         actual = new MekFakePlayer(world);
         INSTANCE = new WeakReference<>(actual);
      }

      actual.m_284127_(world);
      R result = fakePlayerConsumer.apply(actual);
      actual.emulatingUUID = null;
      actual.m_284127_(world.m_7654_().m_129783_());
      return result;
   }

   public static <R> R withFakePlayer(ServerLevel world, double x, double y, double z, Function<MekFakePlayer, R> fakePlayerConsumer) {
      return withFakePlayer(world, fakePlayer -> {
         fakePlayer.m_20343_(x, y, z);
         return fakePlayerConsumer.apply(fakePlayer);
      });
   }

   public static void releaseInstance(ServerLevel world) {
      MekFakePlayer actual = INSTANCE == null ? null : INSTANCE.get();
      if (actual != null && actual.m_284548_() == world) {
         actual.m_284127_(world.m_7654_().m_129783_());
      }
   }

   private static class FakeGameProfile extends GameProfile {
      private MekFakePlayer myFakePlayer = null;

      public FakeGameProfile() {
         super(Mekanism.gameProfile.getId(), Mekanism.gameProfile.getName());
      }

      private UUID getEmulatingUUID() {
         return this.myFakePlayer == null ? null : this.myFakePlayer.emulatingUUID;
      }

      public UUID getId() {
         UUID emulatingUUID = this.getEmulatingUUID();
         return emulatingUUID == null ? super.getId() : emulatingUUID;
      }

      public String getName() {
         UUID emulatingUUID = this.getEmulatingUUID();
         return emulatingUUID == null ? super.getName() : MekanismUtils.getLastKnownUsername(emulatingUUID);
      }

      public boolean equals(final Object o) {
         if (this == o) {
            return true;
         } else {
            return !(o instanceof GameProfile that) ? false : Objects.equals(this.getId(), that.getId()) && Objects.equals(this.getName(), that.getName());
         }
      }

      public int hashCode() {
         UUID id = this.getId();
         String name = this.getName();
         int result = id == null ? 0 : id.hashCode();
         return 31 * result + (name == null ? 0 : name.hashCode());
      }
   }
}
