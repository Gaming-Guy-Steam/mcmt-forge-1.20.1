package mekanism.common.network.to_client;

import java.util.UUID;
import java.util.function.Predicate;
import mekanism.api.chemical.merged.BoxedChemical;
import mekanism.common.content.network.BoxedChemicalNetwork;
import mekanism.common.content.network.EnergyNetwork;
import mekanism.common.content.network.FluidNetwork;
import mekanism.common.lib.transmitter.DynamicBufferedNetwork;
import mekanism.common.lib.transmitter.DynamicNetwork;
import mekanism.common.lib.transmitter.TransmitterNetworkRegistry;
import mekanism.common.network.BasePacketHandler;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.network.NetworkEvent.Context;
import org.jetbrains.annotations.NotNull;

public class PacketTransmitterUpdate implements IMekanismPacket {
   private final PacketTransmitterUpdate.PacketType packetType;
   private final UUID networkID;
   private final float scale;
   @NotNull
   private BoxedChemical chemical = BoxedChemical.EMPTY;
   @NotNull
   private FluidStack fluidStack = FluidStack.EMPTY;

   public PacketTransmitterUpdate(EnergyNetwork network) {
      this(network, PacketTransmitterUpdate.PacketType.ENERGY);
   }

   public PacketTransmitterUpdate(BoxedChemicalNetwork network, @NotNull BoxedChemical chemical) {
      this(network, PacketTransmitterUpdate.PacketType.CHEMICAL);
      this.chemical = chemical;
   }

   public PacketTransmitterUpdate(FluidNetwork network, @NotNull FluidStack fluidStack) {
      this(network, PacketTransmitterUpdate.PacketType.FLUID);
      this.fluidStack = fluidStack;
   }

   private PacketTransmitterUpdate(DynamicBufferedNetwork<?, ?, ?, ?> network, PacketTransmitterUpdate.PacketType type) {
      this(type, network.getUUID(), network.currentScale);
   }

   private PacketTransmitterUpdate(PacketTransmitterUpdate.PacketType type, UUID networkID, float scale) {
      this.packetType = type;
      this.networkID = networkID;
      this.scale = scale;
   }

   @Override
   public void handle(Context context) {
      DynamicNetwork<?, ?, ?> clientNetwork = TransmitterNetworkRegistry.getInstance().getClientNetwork(this.networkID);
      if (clientNetwork != null && this.packetType.networkTypeMatches(clientNetwork)) {
         if (this.packetType == PacketTransmitterUpdate.PacketType.CHEMICAL) {
            ((BoxedChemicalNetwork)clientNetwork).setLastChemical(this.chemical);
         } else if (this.packetType == PacketTransmitterUpdate.PacketType.FLUID) {
            ((FluidNetwork)clientNetwork).setLastFluid(this.fluidStack);
         }

         ((DynamicBufferedNetwork)clientNetwork).currentScale = this.scale;
      }
   }

   @Override
   public void encode(FriendlyByteBuf buffer) {
      buffer.m_130068_(this.packetType);
      buffer.m_130077_(this.networkID);
      buffer.writeFloat(this.scale);
      BasePacketHandler.log("Sending '{}' update message for network with id {}", this.packetType, this.networkID);
      if (this.packetType == PacketTransmitterUpdate.PacketType.FLUID) {
         this.fluidStack.writeToPacket(buffer);
      } else if (this.packetType == PacketTransmitterUpdate.PacketType.CHEMICAL) {
         this.chemical.write(buffer);
      }
   }

   public static PacketTransmitterUpdate decode(FriendlyByteBuf buffer) {
      PacketTransmitterUpdate packet = new PacketTransmitterUpdate(
         (PacketTransmitterUpdate.PacketType)buffer.m_130066_(PacketTransmitterUpdate.PacketType.class), buffer.m_130259_(), buffer.readFloat()
      );
      if (packet.packetType == PacketTransmitterUpdate.PacketType.FLUID) {
         packet.fluidStack = FluidStack.readFromPacket(buffer);
      } else if (packet.packetType == PacketTransmitterUpdate.PacketType.CHEMICAL) {
         packet.chemical = BoxedChemical.read(buffer);
      }

      return packet;
   }

   public static enum PacketType {
      ENERGY(net -> net instanceof EnergyNetwork),
      FLUID(net -> net instanceof FluidNetwork),
      CHEMICAL(net -> net instanceof BoxedChemicalNetwork);

      private final Predicate<DynamicNetwork<?, ?, ?>> networkTypePredicate;

      private PacketType(Predicate<DynamicNetwork<?, ?, ?>> networkTypePredicate) {
         this.networkTypePredicate = networkTypePredicate;
      }

      private boolean networkTypeMatches(DynamicNetwork<?, ?, ?> network) {
         return this.networkTypePredicate.test(network);
      }
   }
}
