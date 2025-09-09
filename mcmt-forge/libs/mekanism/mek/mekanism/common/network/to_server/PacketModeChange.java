package mekanism.common.network.to_server;

import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent.Context;

public class PacketModeChange implements IMekanismPacket {
   private final boolean displayChangeMessage;
   private final EquipmentSlot slot;
   private final int shift;

   public PacketModeChange(EquipmentSlot slot, boolean holdingShift) {
      this(slot, holdingShift ? -1 : 1, true);
   }

   public PacketModeChange(EquipmentSlot slot, int shift) {
      this(slot, shift, false);
   }

   private PacketModeChange(EquipmentSlot slot, int shift, boolean displayChangeMessage) {
      this.slot = slot;
      this.shift = shift;
      this.displayChangeMessage = displayChangeMessage;
   }

   @Override
   public void handle(Context context) {
      Player player = context.getSender();
      if (player != null) {
         ItemStack stack = player.m_6844_(this.slot);
         if (!stack.m_41619_() && stack.m_41720_() instanceof IModeItem modeItem) {
            IModeItem.DisplayChange displayChange;
            if (this.displayChangeMessage) {
               displayChange = this.slot == EquipmentSlot.MAINHAND ? IModeItem.DisplayChange.MAIN_HAND : IModeItem.DisplayChange.OTHER;
            } else {
               displayChange = IModeItem.DisplayChange.NONE;
            }

            modeItem.changeMode(player, stack, this.shift, displayChange);
         }
      }
   }

   @Override
   public void encode(FriendlyByteBuf buffer) {
      buffer.m_130068_(this.slot);
      buffer.m_130130_(this.shift);
      buffer.writeBoolean(this.displayChangeMessage);
   }

   public static PacketModeChange decode(FriendlyByteBuf buffer) {
      return new PacketModeChange((EquipmentSlot)buffer.m_130066_(EquipmentSlot.class), buffer.m_130242_(), buffer.readBoolean());
   }
}
