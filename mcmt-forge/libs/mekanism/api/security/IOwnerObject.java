package mekanism.api.security;

import java.util.Objects;
import java.util.UUID;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AutoRegisterCapability
public interface IOwnerObject {
   @Nullable
   UUID getOwnerUUID();

   @Nullable
   String getOwnerName();

   void setOwnerUUID(@Nullable UUID var1);

   default boolean ownerMatches(@NotNull Player player) {
      Objects.requireNonNull(player, "Player may not be null.");
      return player.m_20148_().equals(this.getOwnerUUID());
   }
}
