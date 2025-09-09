package mekanism.common.network.to_server;

import java.util.UUID;
import mekanism.common.Mekanism;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.to_client.PacketPlayerData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent.Context;

public class PacketGearStateUpdate implements IMekanismPacket {
   private final PacketGearStateUpdate.GearType gearType;
   private final boolean state;
   private final UUID uuid;

   public PacketGearStateUpdate(PacketGearStateUpdate.GearType gearType, UUID uuid, boolean state) {
      this.gearType = gearType;
      this.uuid = uuid;
      this.state = state;
   }

   @Override
   public void handle(Context context) {
      if (this.gearType == PacketGearStateUpdate.GearType.FLAMETHROWER) {
         Mekanism.playerState.setFlamethrowerState(this.uuid, this.state, false);
      } else if (this.gearType == PacketGearStateUpdate.GearType.JETPACK) {
         Mekanism.playerState.setJetpackState(this.uuid, this.state, false);
      } else if (this.gearType == PacketGearStateUpdate.GearType.SCUBA_MASK) {
         Mekanism.playerState.setScubaMaskState(this.uuid, this.state, false);
      } else if (this.gearType == PacketGearStateUpdate.GearType.GRAVITATIONAL_MODULATOR) {
         Mekanism.playerState.setGravitationalModulationState(this.uuid, this.state, false);
      }

      Player player = context.getSender();
      if (player != null) {
         Mekanism.packetHandler().sendToAllTracking(new PacketPlayerData(this.uuid), player);
      }
   }

   @Override
   public void encode(FriendlyByteBuf buffer) {
      buffer.m_130068_(this.gearType);
      buffer.m_130077_(this.uuid);
      buffer.writeBoolean(this.state);
   }

   public static PacketGearStateUpdate decode(FriendlyByteBuf buffer) {
      return new PacketGearStateUpdate(
         (PacketGearStateUpdate.GearType)buffer.m_130066_(PacketGearStateUpdate.GearType.class), buffer.m_130259_(), buffer.readBoolean()
      );
   }

   public static enum GearType {
      FLAMETHROWER,
      JETPACK,
      SCUBA_MASK,
      GRAVITATIONAL_MODULATOR;
   }
}
