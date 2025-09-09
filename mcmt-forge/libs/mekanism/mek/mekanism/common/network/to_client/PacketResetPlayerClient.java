package mekanism.common.network.to_client;

import java.util.UUID;
import mekanism.common.Mekanism;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class PacketResetPlayerClient implements IMekanismPacket {
   private final UUID uuid;

   public PacketResetPlayerClient(UUID uuid) {
      this.uuid = uuid;
   }

   @Override
   public void handle(Context context) {
      Mekanism.playerState.clearPlayer(this.uuid, true);
   }

   @Override
   public void encode(FriendlyByteBuf buffer) {
      buffer.m_130077_(this.uuid);
   }

   public static PacketResetPlayerClient decode(FriendlyByteBuf buffer) {
      return new PacketResetPlayerClient(buffer.m_130259_());
   }
}
