package mekanism.common.network.to_client;

import mekanism.common.network.IMekanismPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkEvent.Context;

public class PacketLaserHitBlock implements IMekanismPacket {
   private final BlockHitResult result;

   public PacketLaserHitBlock(BlockHitResult result) {
      this.result = result;
   }

   @Override
   public void handle(Context context) {
      if (Minecraft.m_91087_().f_91073_ != null) {
         Minecraft.m_91087_().f_91061_.addBlockHitEffects(this.result.m_82425_(), this.result);
      }
   }

   @Override
   public void encode(FriendlyByteBuf buffer) {
      buffer.m_130062_(this.result);
   }

   public static PacketLaserHitBlock decode(FriendlyByteBuf buffer) {
      return new PacketLaserHitBlock(buffer.m_130283_());
   }
}
