package mekanism.common.network.to_server;

import mekanism.common.network.IMekanismPacket;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.util.WorldUtils;
import mekanism.common.util.text.InputValidator;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent.Context;

public class PacketAddTrusted implements IMekanismPacket {
   public static final int MAX_NAME_LENGTH = 16;
   private final BlockPos tilePosition;
   private final String name;

   public PacketAddTrusted(BlockPos tilePosition, String name) {
      this.tilePosition = tilePosition;
      this.name = name;
   }

   public static boolean validateNameLength(int length) {
      return length >= 3 && length <= 16;
   }

   @Override
   public void handle(Context context) {
      if (!this.name.isEmpty() && InputValidator.test(this.name, InputValidator.USERNAME)) {
         Player player = context.getSender();
         if (player != null) {
            TileEntitySecurityDesk tile = WorldUtils.getTileEntity(TileEntitySecurityDesk.class, player.m_9236_(), this.tilePosition);
            if (tile != null) {
               tile.addTrusted(this.name);
            }
         }
      }
   }

   @Override
   public void encode(FriendlyByteBuf buffer) {
      buffer.m_130064_(this.tilePosition);
      buffer.m_130072_(this.name, 16);
   }

   public static PacketAddTrusted decode(FriendlyByteBuf buffer) {
      return new PacketAddTrusted(buffer.m_130135_(), buffer.m_130136_(16));
   }
}
