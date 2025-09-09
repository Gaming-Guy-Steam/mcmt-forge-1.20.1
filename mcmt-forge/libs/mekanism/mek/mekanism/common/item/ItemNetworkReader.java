package mekanism.common.item;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Optional;
import java.util.Set;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.MekanismAPI;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.heat.IHeatHandler;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.network.transmitter.Transmitter;
import mekanism.common.lib.transmitter.DynamicNetwork;
import mekanism.common.lib.transmitter.TransmitterNetworkRegistry;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class ItemNetworkReader extends ItemEnergized {
   public ItemNetworkReader(Properties properties) {
      super(MekanismConfig.gear.networkReaderChargeRate, MekanismConfig.gear.networkReaderMaxEnergy, properties.m_41497_(Rarity.UNCOMMON));
   }

   private void displayBorder(Player player, Object toDisplay, boolean brackets) {
      player.m_213846_(
         MekanismLang.NETWORK_READER_BORDER
            .translateColored(
               EnumColor.GRAY,
               new Object[]{
                  "-------------", EnumColor.DARK_BLUE, brackets ? MekanismLang.GENERIC_SQUARE_BRACKET.translate(new Object[]{toDisplay}) : toDisplay
               }
            )
      );
   }

   private void displayEndBorder(Player player) {
      this.displayBorder(player, "[=======]", false);
   }

   @NotNull
   public InteractionResult m_6225_(UseOnContext context) {
      Player player = context.m_43723_();
      Level world = context.m_43725_();
      if (!world.f_46443_ && player != null) {
         BlockPos pos = context.m_8083_();
         BlockEntity tile = WorldUtils.getTileEntity(world, pos);
         if (tile != null) {
            if (!player.m_7500_()) {
               FloatingLong energyPerUse = MekanismConfig.gear.networkReaderEnergyUsage.get();
               IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(context.m_43722_(), 0);
               if (energyContainer == null || energyContainer.extract(energyPerUse, Action.SIMULATE, AutomationType.MANUAL).smallerThan(energyPerUse)) {
                  return InteractionResult.FAIL;
               }

               energyContainer.extract(energyPerUse, Action.EXECUTE, AutomationType.MANUAL);
            }

            Direction opposite = context.m_43719_().m_122424_();
            if (tile instanceof TileEntityTransmitter transmitterTile) {
               this.displayTransmitterInfo(player, transmitterTile.getTransmitter(), tile, opposite);
            } else {
               Optional<IHeatHandler> heatHandler = CapabilityUtils.getCapability(tile, Capabilities.HEAT_HANDLER, opposite).resolve();
               if (heatHandler.isPresent()) {
                  IHeatHandler transfer = heatHandler.get();
                  this.displayBorder(player, MekanismLang.MEKANISM, true);
                  this.sendTemperature(player, transfer);
                  this.displayEndBorder(player);
               } else {
                  this.displayConnectedNetworks(player, world, pos);
               }
            }

            return InteractionResult.CONSUME;
         }

         if (player.m_6144_() && MekanismAPI.debug) {
            this.displayBorder(player, MekanismLang.DEBUG_TITLE, true);

            for (Component component : TransmitterNetworkRegistry.getInstance().toComponents()) {
               player.m_213846_(TextComponentUtil.build(EnumColor.DARK_GRAY, component));
            }

            this.displayEndBorder(player);
         }
      }

      return InteractionResult.PASS;
   }

   private void displayTransmitterInfo(Player player, Transmitter<?, ?, ?> transmitter, BlockEntity tile, Direction opposite) {
      this.displayBorder(player, MekanismLang.MEKANISM, true);
      if (transmitter.hasTransmitterNetwork()) {
         DynamicNetwork<?, ?, ?> transmitterNetwork = transmitter.getTransmitterNetwork();
         player.m_213846_(
            MekanismLang.NETWORK_READER_TRANSMITTERS.translateColored(EnumColor.GRAY, new Object[]{EnumColor.DARK_GRAY, transmitterNetwork.transmittersSize()})
         );
         player.m_213846_(
            MekanismLang.NETWORK_READER_ACCEPTORS.translateColored(EnumColor.GRAY, new Object[]{EnumColor.DARK_GRAY, transmitterNetwork.getAcceptorCount()})
         );
         this.sendMessageIfNonNull(player, MekanismLang.NETWORK_READER_NEEDED, transmitterNetwork.getNeededInfo());
         this.sendMessageIfNonNull(player, MekanismLang.NETWORK_READER_BUFFER, transmitterNetwork.getStoredInfo());
         this.sendMessageIfNonNull(player, MekanismLang.NETWORK_READER_THROUGHPUT, transmitterNetwork.getFlowInfo());
         this.sendMessageIfNonNull(player, MekanismLang.NETWORK_READER_CAPACITY, transmitterNetwork.getNetworkReaderCapacity());
         CapabilityUtils.getCapability(tile, Capabilities.HEAT_HANDLER, opposite).ifPresent(heatHandler -> this.sendTemperature(player, heatHandler));
      } else {
         player.m_213846_(MekanismLang.NO_NETWORK.translate(new Object[0]));
      }

      this.displayEndBorder(player);
   }

   private void displayConnectedNetworks(Player player, Level world, BlockPos pos) {
      Set<DynamicNetwork<?, ?, ?>> iteratedNetworks = new ObjectOpenHashSet();

      for (Direction side : EnumUtils.DIRECTIONS) {
         if (WorldUtils.getTileEntity(world, pos.m_121945_(side)) instanceof TileEntityTransmitter transmitterTile) {
            Transmitter<?, ?, ?> transmitter = transmitterTile.getTransmitter();
            DynamicNetwork<?, ?, ?> transmitterNetwork = transmitter.getTransmitterNetwork();
            if (transmitterNetwork.hasAcceptor(pos) && !iteratedNetworks.contains(transmitterNetwork)) {
               this.displayBorder(player, this.compileList(transmitter.getSupportedTransmissionTypes()), false);
               player.m_213846_(
                  MekanismLang.NETWORK_READER_CONNECTED_SIDES
                     .translateColored(EnumColor.GRAY, new Object[]{EnumColor.DARK_GRAY, this.compileList(transmitterNetwork.getAcceptorDirections(pos))})
               );
               this.displayEndBorder(player);
               iteratedNetworks.add(transmitterNetwork);
            }
         }
      }
   }

   private void sendTemperature(Player player, IHeatHandler handler) {
      Component temp = MekanismUtils.getTemperatureDisplay(handler.getTotalTemperature(), UnitDisplayUtils.TemperatureUnit.KELVIN, true);
      player.m_213846_(MekanismLang.NETWORK_READER_TEMPERATURE.translateColored(EnumColor.GRAY, new Object[]{EnumColor.DARK_GRAY, temp}));
   }

   private void sendMessageIfNonNull(Player player, ILangEntry langEntry, Object toSend) {
      if (toSend != null) {
         player.m_213846_(langEntry.translateColored(EnumColor.GRAY, EnumColor.DARK_GRAY, toSend));
      }
   }

   private <ENUM extends Enum<ENUM>> Component compileList(Set<ENUM> elements) {
      if (elements.isEmpty()) {
         return MekanismLang.GENERIC_SQUARE_BRACKET.translate(new Object[]{""});
      } else {
         Component component = null;

         for (ENUM element : elements) {
            if (component == null) {
               component = TextComponentUtil.build(element);
            } else {
               component = MekanismLang.GENERIC_WITH_COMMA.translate(new Object[]{component, element});
            }
         }

         return MekanismLang.GENERIC_SQUARE_BRACKET.translate(new Object[]{component});
      }
   }
}
