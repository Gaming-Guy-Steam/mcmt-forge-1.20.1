package mekanism.common.network.to_server;

import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;
import org.jetbrains.annotations.Nullable;

public class PacketWindowSelect implements IMekanismPacket {
   @Nullable
   private final SelectedWindowData selectedWindow;

   public PacketWindowSelect(@Nullable SelectedWindowData selectedWindow) {
      this.selectedWindow = selectedWindow;
   }

   @Override
   public void handle(Context context) {
      ServerPlayer player = context.getSender();
      if (player != null && player.f_36096_ instanceof MekanismContainer container) {
         container.setSelectedWindow(player.m_20148_(), this.selectedWindow);
      }
   }

   @Override
   public void encode(FriendlyByteBuf buffer) {
      if (this.selectedWindow == null) {
         buffer.writeByte(-1);
      } else {
         buffer.writeByte(this.selectedWindow.extraData);
         buffer.m_130068_(this.selectedWindow.type);
      }
   }

   public static PacketWindowSelect decode(FriendlyByteBuf buffer) {
      byte extraData = buffer.readByte();
      if (extraData == -1) {
         return new PacketWindowSelect(null);
      } else {
         SelectedWindowData.WindowType windowType = (SelectedWindowData.WindowType)buffer.m_130066_(SelectedWindowData.WindowType.class);
         return new PacketWindowSelect(
            windowType == SelectedWindowData.WindowType.UNSPECIFIED ? SelectedWindowData.UNSPECIFIED : new SelectedWindowData(windowType, extraData)
         );
      }
   }
}
