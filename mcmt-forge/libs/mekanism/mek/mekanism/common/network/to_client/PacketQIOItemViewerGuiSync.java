package mekanism.common.network.to_client;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.network.BasePacketHandler;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class PacketQIOItemViewerGuiSync implements IMekanismPacket {
   private final PacketQIOItemViewerGuiSync.Type type;
   private final Object2LongMap<HashedItem.UUIDAwareHashedItem> itemMap;
   private final long countCapacity;
   private final int typeCapacity;

   private PacketQIOItemViewerGuiSync(
      PacketQIOItemViewerGuiSync.Type type, Object2LongMap<HashedItem.UUIDAwareHashedItem> itemMap, long countCapacity, int typeCapacity
   ) {
      this.type = type;
      this.itemMap = itemMap;
      this.countCapacity = countCapacity;
      this.typeCapacity = typeCapacity;
   }

   public static PacketQIOItemViewerGuiSync batch(Object2LongMap<HashedItem.UUIDAwareHashedItem> itemMap, long countCapacity, int typeCapacity) {
      return new PacketQIOItemViewerGuiSync(PacketQIOItemViewerGuiSync.Type.BATCH, itemMap, countCapacity, typeCapacity);
   }

   public static PacketQIOItemViewerGuiSync update(Object2LongMap<HashedItem.UUIDAwareHashedItem> itemMap, long countCapacity, int typeCapacity) {
      return new PacketQIOItemViewerGuiSync(PacketQIOItemViewerGuiSync.Type.UPDATE, itemMap, countCapacity, typeCapacity);
   }

   public static PacketQIOItemViewerGuiSync kill() {
      return new PacketQIOItemViewerGuiSync(PacketQIOItemViewerGuiSync.Type.UPDATE, null, 0L, 0);
   }

   @Override
   public void handle(Context context) {
      LocalPlayer player = Minecraft.m_91087_().f_91074_;
      if (player != null && player.f_36096_ instanceof QIOItemViewerContainer container) {
         switch (this.type) {
            case BATCH:
               container.handleBatchUpdate(this.itemMap, this.countCapacity, this.typeCapacity);
               break;
            case UPDATE:
               container.handleUpdate(this.itemMap, this.countCapacity, this.typeCapacity);
               break;
            case KILL:
               container.handleKill();
         }
      }
   }

   @Override
   public void encode(FriendlyByteBuf buffer) {
      buffer.m_130068_(this.type);
      if (this.type == PacketQIOItemViewerGuiSync.Type.BATCH || this.type == PacketQIOItemViewerGuiSync.Type.UPDATE) {
         buffer.m_130103_(this.countCapacity);
         buffer.m_130130_(this.typeCapacity);
         BasePacketHandler.writeMap(buffer, this.itemMap, (key, value, buf) -> {
            buf.m_130055_(key.getInternalStack());
            BasePacketHandler.writeOptional(buf, key.getUUID(), FriendlyByteBuf::m_130077_);
            buf.m_130103_(value);
         });
      }
   }

   public static PacketQIOItemViewerGuiSync decode(FriendlyByteBuf buffer) {
      PacketQIOItemViewerGuiSync.Type type = (PacketQIOItemViewerGuiSync.Type)buffer.m_130066_(PacketQIOItemViewerGuiSync.Type.class);
      long countCapacity = 0L;
      int typeCapacity = 0;
      Object2LongMap<HashedItem.UUIDAwareHashedItem> map = null;
      if (type == PacketQIOItemViewerGuiSync.Type.BATCH || type == PacketQIOItemViewerGuiSync.Type.UPDATE) {
         countCapacity = buffer.m_130258_();
         typeCapacity = buffer.m_130242_();
         map = BasePacketHandler.readMap(
            buffer,
            Object2LongOpenHashMap::new,
            buf -> new HashedItem.UUIDAwareHashedItem(buf.m_130267_(), BasePacketHandler.readOptional(buf, FriendlyByteBuf::m_130259_)),
            FriendlyByteBuf::m_130258_
         );
      }

      return new PacketQIOItemViewerGuiSync(type, map, countCapacity, typeCapacity);
   }

   public static enum Type {
      BATCH,
      UPDATE,
      KILL;
   }
}
