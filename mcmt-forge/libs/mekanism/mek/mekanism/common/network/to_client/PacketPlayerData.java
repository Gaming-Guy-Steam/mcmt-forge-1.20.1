package mekanism.common.network.to_client;

import java.util.UUID;
import mekanism.common.Mekanism;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class PacketPlayerData implements IMekanismPacket {
   private final UUID uuid;
   private final boolean activeFlamethrower;
   private final boolean activeJetpack;
   private final boolean activeScubaMask;
   private final boolean activeModulator;

   public PacketPlayerData(UUID uuid) {
      this.uuid = uuid;
      this.activeFlamethrower = Mekanism.playerState.isFlamethrowerOn(uuid);
      this.activeJetpack = Mekanism.playerState.isJetpackOn(uuid);
      this.activeScubaMask = Mekanism.playerState.isScubaMaskOn(uuid);
      this.activeModulator = Mekanism.playerState.isGravitationalModulationOn(uuid);
   }

   private PacketPlayerData(UUID uuid, boolean activeFlamethrower, boolean activeJetpack, boolean activeGasMask, boolean activeModulator) {
      this.uuid = uuid;
      this.activeFlamethrower = activeFlamethrower;
      this.activeJetpack = activeJetpack;
      this.activeScubaMask = activeGasMask;
      this.activeModulator = activeModulator;
   }

   @Override
   public void handle(Context context) {
      Mekanism.playerState.setFlamethrowerState(this.uuid, this.activeFlamethrower, false);
      Mekanism.playerState.setJetpackState(this.uuid, this.activeJetpack, false);
      Mekanism.playerState.setScubaMaskState(this.uuid, this.activeScubaMask, false);
      Mekanism.playerState.setGravitationalModulationState(this.uuid, this.activeModulator, false);
   }

   @Override
   public void encode(FriendlyByteBuf buffer) {
      buffer.m_130077_(this.uuid);
      buffer.writeBoolean(this.activeFlamethrower);
      buffer.writeBoolean(this.activeJetpack);
      buffer.writeBoolean(this.activeScubaMask);
      buffer.writeBoolean(this.activeModulator);
   }

   public static PacketPlayerData decode(FriendlyByteBuf buffer) {
      return new PacketPlayerData(buffer.m_130259_(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean());
   }
}
