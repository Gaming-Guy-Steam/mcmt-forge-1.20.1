package mekanism.common.network.to_client.container;

import java.util.ArrayList;
import java.util.List;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.to_client.container.property.PropertyData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class PacketUpdateContainer implements IMekanismPacket {
   private final short windowId;
   private final List<PropertyData> data;

   public PacketUpdateContainer(short windowId, List<PropertyData> data) {
      this.windowId = windowId;
      this.data = data;
   }

   @Override
   public void handle(Context context) {
      LocalPlayer player = Minecraft.m_91087_().f_91074_;
      if (player != null && player.f_36096_ instanceof MekanismContainer container && container.f_38840_ == this.windowId) {
         this.data.forEach(data -> data.handleWindowProperty(container));
      }
   }

   @Override
   public void encode(FriendlyByteBuf buffer) {
      buffer.writeByte(this.windowId);
      buffer.m_236828_(this.data, (buf, data) -> data.writeToPacket(buf));
   }

   public static PacketUpdateContainer decode(FriendlyByteBuf buffer) {
      short windowId = buffer.readUnsignedByte();
      int size = buffer.m_130242_();
      List<PropertyData> data = new ArrayList<>(size);

      for (int i = 0; i < size; i++) {
         PropertyData propertyData = PropertyData.fromBuffer(buffer);
         if (propertyData != null) {
            data.add(propertyData);
         }
      }

      return new PacketUpdateContainer(windowId, data);
   }
}
