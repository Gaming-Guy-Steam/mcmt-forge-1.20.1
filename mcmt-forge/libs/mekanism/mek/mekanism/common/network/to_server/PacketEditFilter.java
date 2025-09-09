package mekanism.common.network.to_server;

import javax.annotation.Nullable;
import mekanism.common.content.filter.BaseFilter;
import mekanism.common.content.filter.IFilter;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent.Context;

public class PacketEditFilter<FILTER extends IFilter<FILTER>> implements IMekanismPacket {
   private static final PacketEditFilter<?> ERROR = new PacketEditFilter(BlockPos.f_121853_, null, null);
   private final FILTER filter;
   @Nullable
   private final FILTER edited;
   private final BlockPos pos;

   public PacketEditFilter(BlockPos pos, FILTER filter, @Nullable FILTER edited) {
      this.pos = pos;
      this.filter = filter;
      this.edited = edited;
   }

   @Override
   public void handle(Context context) {
      Player player = context.getSender();
      if (player != null && this.filter != null) {
         if (WorldUtils.getTileEntity(player.m_9236_(), this.pos) instanceof ITileFilterHolder<?> filterHolder) {
            filterHolder.getFilterManager().tryEditFilter(this.filter, this.edited);
         }
      }
   }

   @Override
   public void encode(FriendlyByteBuf buffer) {
      buffer.m_130064_(this.pos);
      this.filter.write(buffer);
      if (this.edited == null) {
         buffer.writeBoolean(false);
      } else {
         buffer.writeBoolean(true);
         this.edited.write(buffer);
      }
   }

   public static <FILTER extends IFilter<FILTER>> PacketEditFilter<?> decode(FriendlyByteBuf buffer) {
      BlockPos pos = buffer.m_130135_();
      FILTER filter = (FILTER)BaseFilter.readFromPacket(buffer);
      IFilter<?> edited = null;
      if (buffer.readBoolean()) {
         edited = BaseFilter.readFromPacket(buffer);
         if (edited.getFilterType() != filter.getFilterType()) {
            return ERROR;
         }
      }

      return new PacketEditFilter<>(pos, filter, edited);
   }
}
