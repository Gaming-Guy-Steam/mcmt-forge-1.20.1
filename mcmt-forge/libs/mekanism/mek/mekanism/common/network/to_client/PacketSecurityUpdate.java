package mekanism.common.network.to_client;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import mekanism.client.MekanismClient;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.security.SecurityData;
import mekanism.common.lib.security.SecurityFrequency;
import mekanism.common.network.BasePacketHandler;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.util.MekanismUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;
import org.jetbrains.annotations.Nullable;

public class PacketSecurityUpdate implements IMekanismPacket {
   private final boolean isUpdate;
   @Nullable
   private SecurityData securityData;
   private String playerUsername;
   private UUID playerUUID;
   private Map<UUID, SecurityData> securityMap = new Object2ObjectOpenHashMap();
   private Map<UUID, String> uuidMap = new Object2ObjectOpenHashMap();

   public PacketSecurityUpdate(SecurityFrequency frequency) {
      this(frequency.getOwner());
      this.securityData = new SecurityData(frequency);
   }

   public PacketSecurityUpdate(UUID uuid) {
      this(true);
      this.playerUUID = uuid;
      this.playerUsername = MekanismUtils.getLastKnownUsername(uuid);
   }

   public PacketSecurityUpdate() {
      this(false);
   }

   private PacketSecurityUpdate(boolean isUpdate) {
      this.isUpdate = isUpdate;
   }

   @Override
   public void handle(Context context) {
      if (this.isUpdate) {
         MekanismClient.clientUUIDMap.put(this.playerUUID, this.playerUsername);
         if (this.securityData != null) {
            MekanismClient.clientSecurityMap.put(this.playerUUID, this.securityData);
         }
      } else {
         MekanismClient.clientSecurityMap.clear();
         MekanismClient.clientSecurityMap.putAll(this.securityMap);
         MekanismClient.clientUUIDMap.putAll(this.uuidMap);
      }
   }

   @Override
   public void encode(FriendlyByteBuf buffer) {
      buffer.writeBoolean(this.isUpdate);
      if (this.isUpdate) {
         buffer.m_130077_(this.playerUUID);
         buffer.m_130070_(this.playerUsername);
         BasePacketHandler.writeOptional(buffer, this.securityData, (buf, data) -> data.write(buf));
      } else {
         List<SecurityFrequency> frequencies = new ArrayList<>(FrequencyType.SECURITY.getManager(null).getFrequencies());
         frequencies.removeIf(frequency -> frequency.getOwner() == null);
         buffer.m_236828_(frequencies, (buf, frequency) -> {
            UUID owner = frequency.getOwner();
            buf.m_130077_(owner);
            new SecurityData(frequency).write(buf);
            buf.m_130070_(MekanismUtils.getLastKnownUsername(owner));
         });
      }
   }

   public static PacketSecurityUpdate decode(FriendlyByteBuf buffer) {
      PacketSecurityUpdate packet = new PacketSecurityUpdate(buffer.readBoolean());
      if (packet.isUpdate) {
         packet.playerUUID = buffer.m_130259_();
         packet.playerUsername = BasePacketHandler.readString(buffer);
         packet.securityData = BasePacketHandler.readOptional(buffer, SecurityData::read);
      } else {
         int frequencySize = buffer.m_130242_();
         packet.securityMap = new Object2ObjectOpenHashMap(frequencySize);
         packet.uuidMap = new Object2ObjectOpenHashMap(frequencySize);

         for (int i = 0; i < frequencySize; i++) {
            UUID uuid = buffer.m_130259_();
            packet.securityMap.put(uuid, SecurityData.read(buffer));
            packet.uuidMap.put(uuid, BasePacketHandler.readString(buffer));
         }
      }

      return packet;
   }
}
