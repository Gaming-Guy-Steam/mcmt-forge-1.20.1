package mekanism.common.network.to_server;

import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IColorableFrequency;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

public class PacketGuiSetFrequencyColor<FREQ extends Frequency & IColorableFrequency> implements IMekanismPacket {
   private final FrequencyType<FREQ> frequencyType;
   private final Frequency.FrequencyIdentity identity;
   private final boolean next;

   private PacketGuiSetFrequencyColor(FrequencyType<FREQ> frequencyType, Frequency.FrequencyIdentity identity, boolean next) {
      this.frequencyType = frequencyType;
      this.identity = identity;
      this.next = next;
   }

   public static <FREQ extends Frequency & IColorableFrequency> PacketGuiSetFrequencyColor<FREQ> create(FREQ freq, boolean next) {
      return new PacketGuiSetFrequencyColor<>((FrequencyType<FREQ>)freq.getType(), freq.getIdentity(), next);
   }

   @Override
   public void handle(Context context) {
      ServerPlayer player = context.getSender();
      if (player != null) {
         FREQ freq = this.frequencyType.getFrequency(this.identity, player.m_20148_());
         if (freq != null && freq.ownerMatches(player.m_20148_())) {
            freq.setColor(this.next ? freq.getColor().getNext() : freq.getColor().getPrevious());
         }
      }
   }

   @Override
   public void encode(FriendlyByteBuf buffer) {
      this.frequencyType.write(buffer);
      this.frequencyType.getIdentitySerializer().write(buffer, this.identity);
      buffer.writeBoolean(this.next);
   }

   public static <FREQ extends Frequency & IColorableFrequency> PacketGuiSetFrequencyColor<FREQ> decode(FriendlyByteBuf buffer) {
      FrequencyType<FREQ> frequencyType = FrequencyType.load(buffer);
      Frequency.FrequencyIdentity identity = frequencyType.getIdentitySerializer().read(buffer);
      return new PacketGuiSetFrequencyColor<>(frequencyType, identity, buffer.readBoolean());
   }
}
