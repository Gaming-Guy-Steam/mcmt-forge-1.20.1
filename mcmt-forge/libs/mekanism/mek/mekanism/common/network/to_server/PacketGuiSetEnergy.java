package mekanism.common.network.to_server;

import java.util.function.BiConsumer;
import mekanism.api.math.FloatingLong;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.laser.TileEntityLaserAmplifier;
import mekanism.common.tile.machine.TileEntityResistiveHeater;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent.Context;

public class PacketGuiSetEnergy implements IMekanismPacket {
   private final PacketGuiSetEnergy.GuiEnergyValue interaction;
   private final BlockPos tilePosition;
   private final FloatingLong value;

   public PacketGuiSetEnergy(PacketGuiSetEnergy.GuiEnergyValue interaction, BlockPos tilePosition, FloatingLong value) {
      this.interaction = interaction;
      this.tilePosition = tilePosition;
      this.value = value;
   }

   @Override
   public void handle(Context context) {
      Player player = context.getSender();
      if (player != null) {
         TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, player.m_9236_(), this.tilePosition);
         if (tile != null) {
            this.interaction.consume(tile, this.value);
         }
      }
   }

   @Override
   public void encode(FriendlyByteBuf buffer) {
      buffer.m_130068_(this.interaction);
      buffer.m_130064_(this.tilePosition);
      this.value.writeToBuffer(buffer);
   }

   public static PacketGuiSetEnergy decode(FriendlyByteBuf buffer) {
      return new PacketGuiSetEnergy(
         (PacketGuiSetEnergy.GuiEnergyValue)buffer.m_130066_(PacketGuiSetEnergy.GuiEnergyValue.class), buffer.m_130135_(), FloatingLong.readFromBuffer(buffer)
      );
   }

   public static enum GuiEnergyValue {
      MIN_THRESHOLD((tile, value) -> {
         if (tile instanceof TileEntityLaserAmplifier amplifier) {
            amplifier.setMinThresholdFromPacket(value);
         }
      }),
      MAX_THRESHOLD((tile, value) -> {
         if (tile instanceof TileEntityLaserAmplifier amplifier) {
            amplifier.setMaxThresholdFromPacket(value);
         }
      }),
      ENERGY_USAGE((tile, value) -> {
         if (tile instanceof TileEntityResistiveHeater heater) {
            heater.setEnergyUsageFromPacket(value);
         }
      });

      private final BiConsumer<TileEntityMekanism, FloatingLong> consumerForTile;

      private GuiEnergyValue(BiConsumer<TileEntityMekanism, FloatingLong> consumerForTile) {
         this.consumerForTile = consumerForTile;
      }

      public void consume(TileEntityMekanism tile, FloatingLong value) {
         this.consumerForTile.accept(tile, value);
      }
   }
}
