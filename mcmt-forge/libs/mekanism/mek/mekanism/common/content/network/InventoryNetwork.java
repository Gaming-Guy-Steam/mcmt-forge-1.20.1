package mekanism.common.content.network;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import mekanism.api.Coord4D;
import mekanism.api.RelativeSide;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.PathfinderCache;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.lib.inventory.TransitRequest;
import mekanism.common.lib.transmitter.DynamicNetwork;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InventoryNetwork extends DynamicNetwork<IItemHandler, InventoryNetwork, LogisticalTransporterBase> {
   private final Map<BlockPos, LogisticalTransporterBase> positionedTransmitters = new Object2ObjectOpenHashMap();

   public InventoryNetwork(UUID networkID) {
      super(networkID);
   }

   public InventoryNetwork(Collection<InventoryNetwork> networks) {
      this(UUID.randomUUID());
      this.adoptAllAndRegister(networks);
   }

   public List<InventoryNetwork.AcceptorData> calculateAcceptors(
      TransitRequest request, TransporterStack stack, Long2ObjectMap<ChunkAccess> chunkMap, Map<Coord4D, Set<TransporterStack>> additionalFlowingStacks
   ) {
      List<InventoryNetwork.AcceptorData> toReturn = new ArrayList<>();

      for (Entry<BlockPos, Map<Direction, LazyOptional<IItemHandler>>> entry : this.acceptorCache.getAcceptorEntrySet()) {
         BlockPos pos = entry.getKey();
         if (!pos.equals(stack.homeLocation)) {
            BlockEntity acceptor = WorldUtils.getTileEntity(this.getWorld(), chunkMap, pos);
            if (acceptor != null) {
               Map<TransitRequest.TransitResponse, InventoryNetwork.AcceptorData> dataMap = new HashMap<>();
               Coord4D position = new Coord4D(pos, this.getWorld());

               for (Entry<Direction, LazyOptional<IItemHandler>> acceptorEntry : entry.getValue().entrySet()) {
                  Optional<IItemHandler> handler = acceptorEntry.getValue().resolve();
                  if (handler.isPresent()) {
                     Direction side = acceptorEntry.getKey();
                     if (acceptor instanceof ISideConfiguration config && config.getEjector().hasStrictInput()) {
                        EnumColor configColor = config.getEjector().getInputColor(RelativeSide.fromDirections(config.getDirection(), side));
                        if (configColor != null && configColor != stack.color) {
                           continue;
                        }
                     }

                     TransitRequest.TransitResponse response = TransporterManager.getPredictedInsert(
                        position, side, handler.get(), request, additionalFlowingStacks
                     );
                     if (!response.isEmpty()) {
                        Direction opposite = side.m_122424_();
                        InventoryNetwork.AcceptorData data = dataMap.get(response);
                        if (data == null) {
                           data = new InventoryNetwork.AcceptorData(pos, response, opposite);
                           dataMap.put(response, data);
                           toReturn.add(data);
                        } else {
                           data.sides.add(opposite);
                        }
                     }
                  }
               }
            }
         }
      }

      return toReturn;
   }

   @Nullable
   public LogisticalTransporterBase getTransmitter(BlockPos pos) {
      return this.positionedTransmitters.get(pos);
   }

   protected void addTransmitterFromCommit(LogisticalTransporterBase transmitter) {
      super.addTransmitterFromCommit(transmitter);
      this.positionedTransmitters.put(transmitter.getTilePos(), transmitter);
   }

   public void addTransmitter(LogisticalTransporterBase transmitter) {
      super.addTransmitter(transmitter);
      this.positionedTransmitters.put(transmitter.getTilePos(), transmitter);
   }

   public void removeTransmitter(LogisticalTransporterBase transmitter) {
      this.removePositionedTransmitter(transmitter);
      super.removeTransmitter(transmitter);
   }

   private void removePositionedTransmitter(LogisticalTransporterBase transmitter) {
      BlockPos pos = transmitter.getTilePos();
      LogisticalTransporterBase currentTransmitter = this.getTransmitter(pos);
      if (currentTransmitter != null) {
         if (currentTransmitter != transmitter) {
            Level world = this.world;
            if (world == null) {
               world = transmitter.getTileWorld();
            }

            if (world != null && world.m_5776_()) {
               return;
            }

            Mekanism.logger
               .warn("Removed transmitter at position: {} in {} was different than expected.", pos, world == null ? null : world.m_46472_().m_135782_());
         }

         this.positionedTransmitters.remove(pos);
      }
   }

   protected void removeInvalid(@Nullable LogisticalTransporterBase triggerTransmitter) {
      Iterator<LogisticalTransporterBase> iterator = this.transmitters.iterator();

      while (iterator.hasNext()) {
         LogisticalTransporterBase transmitter = iterator.next();
         if (!transmitter.isValid()) {
            iterator.remove();
            this.removePositionedTransmitter(transmitter);
         }
      }
   }

   public List<LogisticalTransporterBase> adoptTransmittersAndAcceptorsFrom(InventoryNetwork net) {
      this.positionedTransmitters.putAll(net.positionedTransmitters);
      return super.adoptTransmittersAndAcceptorsFrom(net);
   }

   @Override
   public void commit() {
      super.commit();
      PathfinderCache.onChanged(this);
   }

   @Override
   public void deregister() {
      super.deregister();
      this.positionedTransmitters.clear();
      PathfinderCache.onChanged(this);
   }

   @Override
   public String toString() {
      return "[InventoryNetwork] " + this.transmittersSize() + " transmitters, " + this.getAcceptorCount() + " acceptors.";
   }

   @NotNull
   @Override
   public Component getTextComponent() {
      return MekanismLang.NETWORK_DESCRIPTION.translate(new Object[]{MekanismLang.INVENTORY_NETWORK, this.transmittersSize(), this.getAcceptorCount()});
   }

   public static class AcceptorData {
      private final BlockPos location;
      private final TransitRequest.TransitResponse response;
      private final Set<Direction> sides;

      protected AcceptorData(BlockPos pos, TransitRequest.TransitResponse ret, Direction side) {
         this.location = pos;
         this.response = ret;
         this.sides = EnumSet.of(side);
      }

      public TransitRequest.TransitResponse getResponse() {
         return this.response;
      }

      public BlockPos getLocation() {
         return this.location;
      }

      public Set<Direction> getSides() {
         return this.sides;
      }
   }
}
