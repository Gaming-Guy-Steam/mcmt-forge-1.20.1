package mekanism.common.network.to_client;

import mekanism.common.capabilities.Capabilities;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class PacketRadiationData implements IMekanismPacket {
   private final PacketRadiationData.RadiationPacketType type;
   private final double radiation;
   private final double maxMagnitude;

   private PacketRadiationData(PacketRadiationData.RadiationPacketType type, double radiation, double maxMagnitude) {
      this.type = type;
      this.radiation = radiation;
      this.maxMagnitude = maxMagnitude;
   }

   public static PacketRadiationData createEnvironmental(RadiationManager.LevelAndMaxMagnitude levelAndMaxMagnitude) {
      return new PacketRadiationData(PacketRadiationData.RadiationPacketType.ENVIRONMENTAL, levelAndMaxMagnitude.level(), levelAndMaxMagnitude.maxMagnitude());
   }

   public static PacketRadiationData createPlayer(double radiation) {
      return new PacketRadiationData(PacketRadiationData.RadiationPacketType.PLAYER, radiation, 0.0);
   }

   @Override
   public void handle(Context context) {
      if (this.type == PacketRadiationData.RadiationPacketType.ENVIRONMENTAL) {
         RadiationManager.get().setClientEnvironmentalRadiation(this.radiation, this.maxMagnitude);
      } else if (this.type == PacketRadiationData.RadiationPacketType.PLAYER) {
         LocalPlayer player = Minecraft.m_91087_().f_91074_;
         if (player != null) {
            player.getCapability(Capabilities.RADIATION_ENTITY).ifPresent(c -> c.set(this.radiation));
         }
      }
   }

   @Override
   public void encode(FriendlyByteBuf buffer) {
      buffer.m_130068_(this.type);
      buffer.writeDouble(this.radiation);
      if (this.type.tracksMaxMagnitude) {
         buffer.writeDouble(this.maxMagnitude);
      }
   }

   public static PacketRadiationData decode(FriendlyByteBuf buffer) {
      PacketRadiationData.RadiationPacketType type = (PacketRadiationData.RadiationPacketType)buffer.m_130066_(PacketRadiationData.RadiationPacketType.class);
      return new PacketRadiationData(type, buffer.readDouble(), type.tracksMaxMagnitude ? buffer.readDouble() : 0.0);
   }

   public static enum RadiationPacketType {
      ENVIRONMENTAL(true),
      PLAYER(false);

      private final boolean tracksMaxMagnitude;

      private RadiationPacketType(boolean tracksMaxMagnitude) {
         this.tracksMaxMagnitude = tracksMaxMagnitude;
      }
   }
}
