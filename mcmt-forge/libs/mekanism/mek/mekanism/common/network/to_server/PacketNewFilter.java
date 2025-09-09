package mekanism.common.network.to_server;

import mekanism.common.content.filter.BaseFilter;
import mekanism.common.content.filter.IFilter;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent.Context;

public class PacketNewFilter implements IMekanismPacket {
   private final BlockPos pos;
   private final IFilter<?> filter;

   public PacketNewFilter(BlockPos pos, IFilter<?> filter) {
      this.pos = pos;
      this.filter = filter;
   }

   @Override
   public void handle(Context context) {
      Player player = context.getSender();
      if (player != null && WorldUtils.getTileEntity(player.m_9236_(), this.pos) instanceof ITileFilterHolder<?> filterHolder) {
         filterHolder.getFilterManager().tryAddFilter(this.filter, true);
      }
   }

   @Override
   public void encode(FriendlyByteBuf buffer) {
      buffer.m_130064_(this.pos);
      this.filter.write(buffer);
   }

   public static PacketNewFilter decode(FriendlyByteBuf buffer) {
      return new PacketNewFilter(buffer.m_130135_(), BaseFilter.readFromPacket(buffer));
   }
}
