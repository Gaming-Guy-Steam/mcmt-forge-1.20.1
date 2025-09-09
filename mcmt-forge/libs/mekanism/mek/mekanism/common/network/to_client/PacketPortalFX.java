package mekanism.common.network.to_client;

import mekanism.common.network.IMekanismPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class PacketPortalFX implements IMekanismPacket {
   private final BlockPos pos;
   private final Direction direction;

   public PacketPortalFX(BlockPos pos) {
      this(pos, Direction.UP);
   }

   public PacketPortalFX(BlockPos pos, Direction direction) {
      this.pos = pos;
      this.direction = direction;
   }

   @Override
   public void handle(Context context) {
      ClientLevel world = Minecraft.m_91087_().f_91073_;
      if (world != null) {
         BlockPos secondPos = this.pos.m_121945_(this.direction);

         for (int i = 0; i < 50; i++) {
            world.m_7106_(
               ParticleTypes.f_123760_,
               this.pos.m_123341_() + world.f_46441_.m_188501_(),
               this.pos.m_123342_() + world.f_46441_.m_188501_(),
               this.pos.m_123343_() + world.f_46441_.m_188501_(),
               0.0,
               0.0,
               0.0
            );
            world.m_7106_(
               ParticleTypes.f_123760_,
               secondPos.m_123341_() + world.f_46441_.m_188501_(),
               secondPos.m_123342_() + world.f_46441_.m_188501_(),
               secondPos.m_123343_() + world.f_46441_.m_188501_(),
               0.0,
               0.0,
               0.0
            );
         }
      }
   }

   @Override
   public void encode(FriendlyByteBuf buffer) {
      buffer.m_130064_(this.pos);
      buffer.m_130068_(this.direction);
   }

   public static PacketPortalFX decode(FriendlyByteBuf buffer) {
      return new PacketPortalFX(buffer.m_130135_(), (Direction)buffer.m_130066_(Direction.class));
   }
}
