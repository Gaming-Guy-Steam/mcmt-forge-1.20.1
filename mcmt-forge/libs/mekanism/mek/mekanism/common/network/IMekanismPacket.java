package mekanism.common.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public interface IMekanismPacket {
   void handle(Context context);

   void encode(FriendlyByteBuf buffer);

   static <PACKET extends IMekanismPacket> void handle(PACKET message, Supplier<Context> ctx) {
      if (message != null) {
         Context context = ctx.get();
         context.enqueueWork(() -> message.handle(context));
         context.setPacketHandled(true);
      }
   }
}
