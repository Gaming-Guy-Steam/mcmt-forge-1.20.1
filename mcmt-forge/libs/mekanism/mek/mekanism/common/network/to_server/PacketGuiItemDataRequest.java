package mekanism.common.network.to_server;

import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

public class PacketGuiItemDataRequest implements IMekanismPacket {
   private final PacketGuiItemDataRequest.Type type;

   private PacketGuiItemDataRequest(PacketGuiItemDataRequest.Type type) {
      this.type = type;
   }

   public static PacketGuiItemDataRequest qioItemViewer() {
      return new PacketGuiItemDataRequest(PacketGuiItemDataRequest.Type.QIO_ITEM_VIEWER);
   }

   @Override
   public void handle(Context context) {
      ServerPlayer player = context.getSender();
      if (player != null && this.type == PacketGuiItemDataRequest.Type.QIO_ITEM_VIEWER && player.f_36096_ instanceof QIOItemViewerContainer container) {
         QIOFrequency freq = container.getFrequency();
         if (!player.m_9236_().m_5776_() && freq != null) {
            freq.openItemViewer(player);
         }
      }
   }

   @Override
   public void encode(FriendlyByteBuf buffer) {
      buffer.m_130068_(this.type);
   }

   public static PacketGuiItemDataRequest decode(FriendlyByteBuf buffer) {
      return new PacketGuiItemDataRequest((PacketGuiItemDataRequest.Type)buffer.m_130066_(PacketGuiItemDataRequest.Type.class));
   }

   private static enum Type {
      QIO_ITEM_VIEWER;
   }
}
