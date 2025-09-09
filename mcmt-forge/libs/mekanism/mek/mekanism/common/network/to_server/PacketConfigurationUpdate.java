package mekanism.common.network.to_server;

import mekanism.api.RelativeSide;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent.Context;
import org.jetbrains.annotations.NotNull;

public class PacketConfigurationUpdate implements IMekanismPacket {
   private final PacketConfigurationUpdate.ConfigurationPacket packetType;
   private final BlockPos pos;
   private TransmissionType transmission;
   private RelativeSide inputSide;
   private int clickType;

   public PacketConfigurationUpdate(@NotNull PacketConfigurationUpdate.ConfigurationPacket type, BlockPos pos, TransmissionType trans) {
      this.packetType = type;
      this.pos = pos;
      this.transmission = trans;
   }

   public PacketConfigurationUpdate(BlockPos pos, int click) {
      this.packetType = PacketConfigurationUpdate.ConfigurationPacket.EJECT_COLOR;
      this.pos = pos;
      this.clickType = click;
   }

   public PacketConfigurationUpdate(BlockPos pos) {
      this.packetType = PacketConfigurationUpdate.ConfigurationPacket.STRICT_INPUT;
      this.pos = pos;
   }

   public PacketConfigurationUpdate(
      @NotNull PacketConfigurationUpdate.ConfigurationPacket type, BlockPos pos, int click, RelativeSide inputSide, TransmissionType trans
   ) {
      this.packetType = type;
      this.pos = pos;
      switch (this.packetType) {
         case EJECT:
         case CLEAR_ALL:
            this.transmission = trans;
            break;
         case EJECT_COLOR:
            this.clickType = click;
            break;
         case SIDE_DATA:
            this.clickType = click;
            this.inputSide = inputSide;
            this.transmission = trans;
            break;
         case INPUT_COLOR:
            this.clickType = click;
            this.inputSide = inputSide;
      }
   }

   @Override
   public void handle(Context context) {
      Player player = context.getSender();
      if (player != null) {
         BlockEntity tile = WorldUtils.getTileEntity(player.m_9236_(), this.pos);
         if (tile instanceof ISideConfiguration config) {
            switch (this.packetType) {
               case EJECT:
                  ConfigInfo infox = config.getConfig().getConfig(this.transmission);
                  if (infox != null) {
                     infox.setEjecting(!infox.isEjecting());
                     WorldUtils.saveChunk(tile);
                  }
                  break;
               case CLEAR_ALL:
                  TileComponentConfig configComponentx = config.getConfig();
                  ConfigInfo infoxx = configComponentx.getConfig(this.transmission);
                  if (infoxx != null) {
                     for (RelativeSide side : EnumUtils.SIDES) {
                        if (infoxx.isSideEnabled(side) && infoxx.getDataType(side) != DataType.NONE) {
                           infoxx.setDataType(DataType.NONE, side);
                           configComponentx.sideChanged(this.transmission, side);
                        }
                     }
                  }
                  break;
               case EJECT_COLOR:
                  TileComponentEjector ejectorx = config.getEjector();
                  switch (this.clickType) {
                     case 0:
                        ejectorx.setOutputColor(TransporterUtils.increment(ejectorx.getOutputColor()));
                        return;
                     case 1:
                        ejectorx.setOutputColor(TransporterUtils.decrement(ejectorx.getOutputColor()));
                        return;
                     case 2:
                        ejectorx.setOutputColor(null);
                        return;
                     default:
                        return;
                  }
               case SIDE_DATA:
                  TileComponentConfig configComponent = config.getConfig();
                  ConfigInfo info = configComponent.getConfig(this.transmission);
                  if (info != null) {
                     DataType type = info.getDataType(this.inputSide);
                     boolean changed = false;
                     if (this.clickType == 0) {
                        changed = type != info.incrementDataType(this.inputSide);
                     } else if (this.clickType == 1) {
                        changed = type != info.decrementDataType(this.inputSide);
                     } else if (this.clickType == 2 && type != DataType.NONE) {
                        changed = true;
                        info.setDataType(DataType.NONE, this.inputSide);
                     }

                     if (changed) {
                        configComponent.sideChanged(this.transmission, this.inputSide);
                     }
                  }
                  break;
               case INPUT_COLOR:
                  TileComponentEjector ejectorx = config.getEjector();
                  switch (this.clickType) {
                     case 0:
                        ejectorx.setInputColor(this.inputSide, TransporterUtils.increment(ejectorx.getInputColor(this.inputSide)));
                        return;
                     case 1:
                        ejectorx.setInputColor(this.inputSide, TransporterUtils.decrement(ejectorx.getInputColor(this.inputSide)));
                        return;
                     case 2:
                        ejectorx.setInputColor(this.inputSide, null);
                        return;
                     default:
                        return;
                  }
               case STRICT_INPUT:
                  TileComponentEjector ejector = config.getEjector();
                  ejector.setStrictInput(!ejector.hasStrictInput());
            }
         }
      }
   }

   @Override
   public void encode(FriendlyByteBuf buffer) {
      buffer.m_130068_(this.packetType);
      buffer.m_130064_(this.pos);
      switch (this.packetType) {
         case EJECT:
         case CLEAR_ALL:
            buffer.m_130068_(this.transmission);
            break;
         case EJECT_COLOR:
            buffer.m_130130_(this.clickType);
            break;
         case SIDE_DATA:
            buffer.m_130130_(this.clickType);
            buffer.m_130068_(this.inputSide);
            buffer.m_130068_(this.transmission);
            break;
         case INPUT_COLOR:
            buffer.m_130130_(this.clickType);
            buffer.m_130068_(this.inputSide);
      }
   }

   public static PacketConfigurationUpdate decode(FriendlyByteBuf buffer) {
      PacketConfigurationUpdate.ConfigurationPacket packetType = (PacketConfigurationUpdate.ConfigurationPacket)buffer.m_130066_(
         PacketConfigurationUpdate.ConfigurationPacket.class
      );
      BlockPos pos = buffer.m_130135_();
      int clickType = 0;
      RelativeSide inputSide = null;
      TransmissionType transmission = null;
      switch (packetType) {
         case EJECT:
         case CLEAR_ALL:
            transmission = (TransmissionType)buffer.m_130066_(TransmissionType.class);
            break;
         case EJECT_COLOR:
            clickType = buffer.m_130242_();
            break;
         case SIDE_DATA:
            clickType = buffer.m_130242_();
            inputSide = (RelativeSide)buffer.m_130066_(RelativeSide.class);
            transmission = (TransmissionType)buffer.m_130066_(TransmissionType.class);
            break;
         case INPUT_COLOR:
            clickType = buffer.m_130242_();
            inputSide = (RelativeSide)buffer.m_130066_(RelativeSide.class);
      }

      return new PacketConfigurationUpdate(packetType, pos, clickType, inputSide, transmission);
   }

   public static enum ConfigurationPacket {
      EJECT,
      SIDE_DATA,
      EJECT_COLOR,
      INPUT_COLOR,
      STRICT_INPUT,
      CLEAR_ALL;
   }
}
