package mekanism.common.network.to_server;

import mekanism.common.Mekanism;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent.Context;

public class PacketKey implements IMekanismPacket {
   private final int key;
   private final boolean add;

   public PacketKey(int key, boolean add) {
      this.key = key;
      this.add = add;
   }

   @Override
   public void handle(Context context) {
      Player player = context.getSender();
      if (player != null) {
         if (this.add) {
            Mekanism.keyMap.add(player.m_20148_(), this.key);
         } else {
            Mekanism.keyMap.remove(player.m_20148_(), this.key);
         }
      }
   }

   @Override
   public void encode(FriendlyByteBuf buffer) {
      buffer.m_130130_(this.key);
      buffer.writeBoolean(this.add);
   }

   public static PacketKey decode(FriendlyByteBuf buffer) {
      return new PacketKey(buffer.m_130242_(), buffer.readBoolean());
   }
}
