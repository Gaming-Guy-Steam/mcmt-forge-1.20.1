package mekanism.common.network.to_client;

import mekanism.common.Mekanism;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.tile.base.TileEntityUpdateable;
import mekanism.common.util.WorldUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class PacketUpdateTile implements IMekanismPacket {
   private final CompoundTag updateTag;
   private final BlockPos pos;

   public PacketUpdateTile(TileEntityUpdateable tile) {
      this(tile.m_58899_(), tile.getReducedUpdateTag());
   }

   private PacketUpdateTile(BlockPos pos, CompoundTag updateTag) {
      this.pos = pos;
      this.updateTag = updateTag;
   }

   @Override
   public void handle(Context context) {
      ClientLevel world = Minecraft.m_91087_().f_91073_;
      if (WorldUtils.isBlockLoaded(world, this.pos)) {
         TileEntityUpdateable tile = WorldUtils.getTileEntity(TileEntityUpdateable.class, world, this.pos, true);
         if (tile == null) {
            Mekanism.logger
               .warn("Update tile packet received for position: {} in world: {}, but no valid tile was found.", this.pos, world.m_46472_().m_135782_());
         } else {
            tile.handleUpdatePacket(this.updateTag);
         }
      }
   }

   @Override
   public void encode(FriendlyByteBuf buffer) {
      buffer.m_130064_(this.pos);
      buffer.m_130079_(this.updateTag);
   }

   public static PacketUpdateTile decode(FriendlyByteBuf buffer) {
      return new PacketUpdateTile(buffer.m_130135_(), buffer.m_130260_());
   }
}
