package mekanism.common.network.to_server;

import mekanism.common.network.IMekanismPacket;
import mekanism.common.util.SecurityUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent.Context;

public class PacketSecurityMode implements IMekanismPacket {
   private final InteractionHand currentHand;
   private final boolean increment;

   public PacketSecurityMode(InteractionHand hand, boolean increment) {
      this.currentHand = hand;
      this.increment = increment;
   }

   @Override
   public void handle(Context context) {
      Player player = context.getSender();
      if (player != null) {
         ItemStack stack = player.m_21120_(this.currentHand);
         if (this.increment) {
            SecurityUtils.get().incrementSecurityMode(player, stack);
         } else {
            SecurityUtils.get().decrementSecurityMode(player, stack);
         }
      }
   }

   @Override
   public void encode(FriendlyByteBuf buffer) {
      buffer.m_130068_(this.currentHand);
      buffer.writeBoolean(this.increment);
   }

   public static PacketSecurityMode decode(FriendlyByteBuf buffer) {
      return new PacketSecurityMode((InteractionHand)buffer.m_130066_(InteractionHand.class), buffer.readBoolean());
   }
}
