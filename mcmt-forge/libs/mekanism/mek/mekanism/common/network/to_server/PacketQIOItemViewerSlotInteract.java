package mekanism.common.network.to_server;

import java.util.UUID;
import mekanism.common.Mekanism;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.content.qio.QIOGlobalItemLookup;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.util.InventoryUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent.Context;

public class PacketQIOItemViewerSlotInteract implements IMekanismPacket {
   private final PacketQIOItemViewerSlotInteract.Type type;
   private final UUID typeUUID;
   private final int count;

   private PacketQIOItemViewerSlotInteract(PacketQIOItemViewerSlotInteract.Type type, UUID typeUUID, int count) {
      this.type = type;
      this.typeUUID = typeUUID;
      this.count = count;
   }

   public static PacketQIOItemViewerSlotInteract take(UUID typeUUID, int count) {
      return new PacketQIOItemViewerSlotInteract(PacketQIOItemViewerSlotInteract.Type.TAKE, typeUUID, count);
   }

   public static PacketQIOItemViewerSlotInteract put(int count) {
      return new PacketQIOItemViewerSlotInteract(PacketQIOItemViewerSlotInteract.Type.PUT, null, count);
   }

   public static PacketQIOItemViewerSlotInteract shiftTake(UUID typeUUID) {
      return new PacketQIOItemViewerSlotInteract(PacketQIOItemViewerSlotInteract.Type.SHIFT_TAKE, typeUUID, 0);
   }

   @Override
   public void handle(Context context) {
      ServerPlayer player = context.getSender();
      if (player != null && player.f_36096_ instanceof QIOItemViewerContainer container) {
         QIOFrequency freq = container.getFrequency();
         if (freq != null) {
            if (this.type == PacketQIOItemViewerSlotInteract.Type.PUT) {
               ItemStack curStack = player.f_36096_.m_142621_();
               if (!curStack.m_41619_() && this.count > 0) {
                  ItemStack toAdd;
                  if (this.count < curStack.m_41613_()) {
                     toAdd = curStack.m_255036_(this.count);
                  } else {
                     toAdd = curStack;
                  }

                  ItemStack rejects = freq.addItem(toAdd);
                  int placed = toAdd.m_41613_() - rejects.m_41613_();
                  if (placed > 0) {
                     curStack.m_41774_(placed);
                     this.updateCarried(player, container);
                  }
               }
            } else {
               HashedItem itemType = QIOGlobalItemLookup.INSTANCE.getTypeByUUID(this.typeUUID);
               if (itemType != null) {
                  if (this.type == PacketQIOItemViewerSlotInteract.Type.TAKE) {
                     ItemStack curStack = player.f_36096_.m_142621_();
                     int toRemove = Math.min(this.count, itemType.getMaxStackSize() - curStack.m_41613_());
                     if (toRemove > 0 && InventoryUtils.areItemsStackable(curStack, itemType.getInternalStack())) {
                        ItemStack extracted = freq.removeByType(itemType, toRemove);
                        if (!extracted.m_41619_()) {
                           if (curStack.m_41619_()) {
                              player.f_36096_.m_142503_(extracted);
                           } else {
                              curStack.m_41769_(extracted.m_41613_());
                           }

                           this.updateCarried(player, container);
                        }
                     }
                  } else if (this.type == PacketQIOItemViewerSlotInteract.Type.SHIFT_TAKE) {
                     ItemStack maxExtract = itemType.createStack(itemType.getMaxStackSize());
                     ItemStack simulatedExcess = container.simulateInsertIntoPlayerInventory(player.m_20148_(), maxExtract);
                     ItemStack extracted = freq.removeByType(itemType, maxExtract.m_41613_() - simulatedExcess.m_41613_());
                     if (!extracted.m_41619_()) {
                        ItemStack remainder = container.insertIntoPlayerInventory(player.m_20148_(), extracted);
                        if (!remainder.m_41619_()) {
                           remainder = freq.addItem(remainder);
                           if (!remainder.m_41619_()) {
                              Mekanism.logger.error("QIO shift-click transfer resulted in lost items ({}). This shouldn't happen!", remainder);
                              player.m_36176_(remainder, false);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void updateCarried(ServerPlayer player, QIOItemViewerContainer container) {
      player.f_8906_.m_9829_(new ClientboundContainerSetSlotPacket(-1, container.m_182425_(), -1, player.f_36096_.m_142621_()));
   }

   @Override
   public void encode(FriendlyByteBuf buffer) {
      buffer.m_130068_(this.type);
      switch (this.type) {
         case TAKE:
            buffer.m_130077_(this.typeUUID);
            buffer.m_130130_(this.count);
            break;
         case SHIFT_TAKE:
            buffer.m_130077_(this.typeUUID);
            break;
         case PUT:
            buffer.m_130130_(this.count);
      }
   }

   public static PacketQIOItemViewerSlotInteract decode(FriendlyByteBuf buffer) {
      PacketQIOItemViewerSlotInteract.Type type = (PacketQIOItemViewerSlotInteract.Type)buffer.m_130066_(PacketQIOItemViewerSlotInteract.Type.class);
      UUID typeUUID = null;
      int count = 0;
      switch (type) {
         case TAKE:
            typeUUID = buffer.m_130259_();
            count = buffer.m_130242_();
            break;
         case SHIFT_TAKE:
            typeUUID = buffer.m_130259_();
            break;
         case PUT:
            count = buffer.m_130242_();
      }

      return new PacketQIOItemViewerSlotInteract(type, typeUUID, count);
   }

   public static enum Type {
      TAKE,
      SHIFT_TAKE,
      PUT;
   }
}
