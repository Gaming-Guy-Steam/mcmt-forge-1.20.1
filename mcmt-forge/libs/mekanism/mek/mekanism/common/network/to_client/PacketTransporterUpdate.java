package mekanism.common.network.to_client;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import mekanism.common.content.network.transmitter.DiversionTransporter;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.network.BasePacketHandler;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporterBase;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class PacketTransporterUpdate implements IMekanismPacket {
   private final boolean isDiversion;
   private final boolean isSync;
   private final BlockPos pos;
   private LogisticalTransporterBase transporter;
   private DiversionTransporter.DiversionControl[] modes;
   private int stackId;
   private TransporterStack stack;
   private Int2ObjectMap<TransporterStack> updates;
   private IntSet deletes;

   public PacketTransporterUpdate(LogisticalTransporterBase tile, int stackId, TransporterStack stack) {
      this(tile, true);
      this.stackId = stackId;
      this.stack = stack;
   }

   public PacketTransporterUpdate(LogisticalTransporterBase tile, Int2ObjectMap<TransporterStack> updates, IntSet deletes) {
      this(tile, false);
      this.updates = updates;
      this.deletes = deletes;
   }

   private PacketTransporterUpdate(LogisticalTransporterBase transporter, boolean isSync) {
      this.isSync = isSync;
      this.pos = transporter.getTilePos();
      this.isDiversion = transporter instanceof DiversionTransporter;
      if (this.isDiversion) {
         this.modes = ((DiversionTransporter)transporter).modes;
      }

      this.transporter = transporter;
   }

   private PacketTransporterUpdate(BlockPos pos, boolean isSync, boolean isDiversion) {
      this.pos = pos;
      this.isSync = isSync;
      this.isDiversion = isDiversion;
   }

   @Override
   public void handle(Context context) {
      TileEntityLogisticalTransporterBase tile = WorldUtils.getTileEntity(TileEntityLogisticalTransporterBase.class, Minecraft.m_91087_().f_91073_, this.pos);
      if (tile != null) {
         LogisticalTransporterBase transporter = tile.getTransmitter();
         if (this.isSync) {
            transporter.addStack(this.stackId, this.stack);
         } else {
            ObjectIterator diversionTransporter = this.updates.int2ObjectEntrySet().iterator();

            while (diversionTransporter.hasNext()) {
               Entry<TransporterStack> entry = (Entry<TransporterStack>)diversionTransporter.next();
               transporter.addStack(entry.getIntKey(), (TransporterStack)entry.getValue());
            }

            IntIterator var6 = this.deletes.iterator();

            while (var6.hasNext()) {
               int toDelete = (Integer)var6.next();
               transporter.deleteStack(toDelete);
            }
         }

         if (this.isDiversion && transporter instanceof DiversionTransporter diversionTransporter) {
            System.arraycopy(this.modes, 0, diversionTransporter.modes, 0, this.modes.length);
         }
      }
   }

   @Override
   public void encode(FriendlyByteBuf buffer) {
      buffer.m_130064_(this.pos);
      buffer.writeBoolean(this.isSync);
      buffer.writeBoolean(this.isDiversion);
      if (this.isSync) {
         buffer.m_130130_(this.stackId);
         this.stack.write(this.transporter, buffer);
      } else {
         BasePacketHandler.writeMap(buffer, this.updates, (key, value, buf) -> {
            buf.m_130130_(key);
            value.write(this.transporter, buf);
         });
         buffer.m_236828_(this.deletes, FriendlyByteBuf::m_130130_);
      }

      if (this.isDiversion) {
         for (DiversionTransporter.DiversionControl mode : this.modes) {
            buffer.m_130068_(mode);
         }
      }
   }

   public static PacketTransporterUpdate decode(FriendlyByteBuf buffer) {
      PacketTransporterUpdate packet = new PacketTransporterUpdate(buffer.m_130135_(), buffer.readBoolean(), buffer.readBoolean());
      if (packet.isSync) {
         packet.stackId = buffer.m_130242_();
         packet.stack = TransporterStack.readFromPacket(buffer);
      } else {
         packet.updates = BasePacketHandler.readMap(buffer, Int2ObjectOpenHashMap::new, FriendlyByteBuf::m_130242_, TransporterStack::readFromPacket);
         packet.deletes = (IntSet)buffer.m_236838_(IntOpenHashSet::new, FriendlyByteBuf::m_130242_);
      }

      if (packet.isDiversion) {
         packet.modes = new DiversionTransporter.DiversionControl[EnumUtils.DIRECTIONS.length];

         for (int i = 0; i < packet.modes.length; i++) {
            packet.modes[i] = (DiversionTransporter.DiversionControl)buffer.m_130066_(DiversionTransporter.DiversionControl.class);
         }
      }

      return packet;
   }
}
