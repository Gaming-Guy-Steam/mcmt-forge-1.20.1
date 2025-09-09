package mekanism.common.network.to_server;

import mekanism.api.security.ISecurityUtils;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyManager;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IFrequencyHandler;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent.Context;

public class PacketGuiSetFrequency<FREQ extends Frequency> implements IMekanismPacket {
   private final FrequencyType<FREQ> type;
   private final PacketGuiSetFrequency.FrequencyUpdate updateType;
   private final Frequency.FrequencyIdentity data;
   private final BlockPos tilePosition;
   private final InteractionHand currentHand;

   private PacketGuiSetFrequency(
      PacketGuiSetFrequency.FrequencyUpdate updateType,
      FrequencyType<FREQ> type,
      Frequency.FrequencyIdentity data,
      BlockPos tilePosition,
      InteractionHand currentHand
   ) {
      this.updateType = updateType;
      this.type = type;
      this.data = data;
      this.tilePosition = tilePosition;
      this.currentHand = currentHand;
   }

   public static <FREQ extends Frequency> PacketGuiSetFrequency<FREQ> create(
      PacketGuiSetFrequency.FrequencyUpdate updateType, FrequencyType<FREQ> type, Frequency.FrequencyIdentity data, BlockPos tilePosition
   ) {
      return new PacketGuiSetFrequency<>(updateType, type, data, tilePosition, null);
   }

   public static <FREQ extends Frequency> PacketGuiSetFrequency<FREQ> create(
      PacketGuiSetFrequency.FrequencyUpdate updateType, FrequencyType<FREQ> type, Frequency.FrequencyIdentity data, InteractionHand currentHand
   ) {
      return new PacketGuiSetFrequency<>(updateType, type, data, null, currentHand);
   }

   @Override
   public void handle(Context context) {
      ServerPlayer player = context.getSender();
      if (player != null) {
         if (this.updateType.isTile()) {
            BlockEntity tile = WorldUtils.getTileEntity(player.m_9236_(), this.tilePosition);
            if (tile instanceof IFrequencyHandler frequencyHandler && ISecurityUtils.INSTANCE.canAccess(player, tile)) {
               if (this.updateType == PacketGuiSetFrequency.FrequencyUpdate.SET_TILE) {
                  frequencyHandler.setFrequency(this.type, this.data, player.m_20148_());
               } else if (this.updateType == PacketGuiSetFrequency.FrequencyUpdate.REMOVE_TILE) {
                  frequencyHandler.removeFrequency(this.type, this.data, player.m_20148_());
               }
            }
         } else {
            ItemStack stack = player.m_21120_(this.currentHand);
            if (ISecurityUtils.INSTANCE.canAccess(player, stack) && stack.m_41720_() instanceof IFrequencyItem item) {
               FrequencyManager<FREQ> manager = this.type.getManager(this.data, player.m_20148_());
               if (this.updateType == PacketGuiSetFrequency.FrequencyUpdate.SET_ITEM) {
                  item.setFrequency(stack, manager.getOrCreateFrequency(this.data, player.m_20148_()));
               } else if (this.updateType == PacketGuiSetFrequency.FrequencyUpdate.REMOVE_ITEM && manager.remove(this.data.key(), player.m_20148_())) {
                  Frequency.FrequencyIdentity current = item.getFrequencyIdentity(stack);
                  if (current != null && current.equals(this.data)) {
                     item.setFrequency(stack, null);
                  }
               }
            }
         }
      }
   }

   @Override
   public void encode(FriendlyByteBuf buffer) {
      buffer.m_130068_(this.updateType);
      this.type.write(buffer);
      this.type.getIdentitySerializer().write(buffer, this.data);
      if (this.updateType.isTile()) {
         buffer.m_130064_(this.tilePosition);
      } else {
         buffer.m_130068_(this.currentHand);
      }
   }

   public static <FREQ extends Frequency> PacketGuiSetFrequency<FREQ> decode(FriendlyByteBuf buffer) {
      PacketGuiSetFrequency.FrequencyUpdate updateType = (PacketGuiSetFrequency.FrequencyUpdate)buffer.m_130066_(PacketGuiSetFrequency.FrequencyUpdate.class);
      FrequencyType<FREQ> type = FrequencyType.load(buffer);
      Frequency.FrequencyIdentity data = type.getIdentitySerializer().read(buffer);
      BlockPos pos = updateType.isTile() ? buffer.m_130135_() : null;
      InteractionHand hand = updateType.isTile() ? null : (InteractionHand)buffer.m_130066_(InteractionHand.class);
      return new PacketGuiSetFrequency<>(updateType, type, data, pos, hand);
   }

   public static enum FrequencyUpdate {
      SET_TILE,
      SET_ITEM,
      REMOVE_TILE,
      REMOVE_ITEM;

      boolean isTile() {
         return this == SET_TILE || this == REMOVE_TILE;
      }
   }
}
