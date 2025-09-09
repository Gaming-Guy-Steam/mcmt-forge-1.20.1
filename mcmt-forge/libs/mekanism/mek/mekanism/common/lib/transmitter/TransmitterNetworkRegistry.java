package mekanism.common.lib.transmitter;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap.Entry;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import mekanism.api.Chunk3D;
import mekanism.api.Coord4D;
import mekanism.api.MekanismAPI;
import mekanism.common.Mekanism;
import mekanism.common.content.network.transmitter.Transmitter;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.level.ChunkTicketLevelUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.Nullable;

public class TransmitterNetworkRegistry {
   private static final TransmitterNetworkRegistry INSTANCE = new TransmitterNetworkRegistry();
   private static boolean loaderRegistered = false;
   private final Multimap<Chunk3D, Transmitter<?, ?, ?>> transmitters = HashMultimap.create();
   private Object2BooleanMap<Chunk3D> changedTicketChunks = new Object2BooleanOpenHashMap();
   private final Set<DynamicNetwork<?, ?, ?>> networks = new ObjectOpenHashSet();
   private final Map<UUID, DynamicNetwork<?, ?, ?>> clientNetworks = new Object2ObjectOpenHashMap();
   private Map<Coord4D, Transmitter<?, ?, ?>> newOrphanTransmitters = new Object2ObjectOpenHashMap();
   private Set<Transmitter<?, ?, ?>> invalidTransmitters = new ObjectOpenHashSet();
   private Set<DynamicNetwork<?, ?, ?>> networksToChange = new ObjectOpenHashSet();

   public void addClientNetwork(UUID networkID, DynamicNetwork<?, ?, ?> network) {
      if (!this.clientNetworks.containsKey(networkID)) {
         this.clientNetworks.put(networkID, network);
      }
   }

   @Nullable
   public DynamicNetwork<?, ?, ?> getClientNetwork(UUID networkID) {
      return this.clientNetworks.get(networkID);
   }

   public void removeClientNetwork(DynamicNetwork<?, ?, ?> network) {
      this.clientNetworks.remove(network.getUUID());
   }

   public void clearClientNetworks() {
      this.clientNetworks.clear();
   }

   public static void initiate() {
      if (!loaderRegistered) {
         loaderRegistered = true;
         MinecraftForge.EVENT_BUS.register(INSTANCE);
      }
   }

   public static void reset() {
      getInstance().networks.clear();
      getInstance().networksToChange.clear();
      getInstance().invalidTransmitters.clear();
      getInstance().newOrphanTransmitters.clear();
      getInstance().transmitters.clear();
      getInstance().changedTicketChunks.clear();
   }

   public static void trackTransmitter(Transmitter<?, ?, ?> transmitter) {
      getInstance().transmitters.put(transmitter.getTileChunk(), transmitter);
   }

   public static void untrackTransmitter(Transmitter<?, ?, ?> transmitter) {
      getInstance().transmitters.remove(transmitter.getTileChunk(), transmitter);
   }

   public static void invalidateTransmitter(Transmitter<?, ?, ?> transmitter) {
      TransmitterNetworkRegistry registry = getInstance();
      registry.invalidTransmitters.add(transmitter);
      Coord4D coord = transmitter.getTileCoord();
      Transmitter<?, ?, ?> removed = registry.newOrphanTransmitters.remove(coord);
      if (removed != null && removed != transmitter) {
         Mekanism.logger.error("Different orphan transmitter was registered at location during removal! {}", coord);
         registry.newOrphanTransmitters.put(coord, transmitter);
      }
   }

   public static void registerOrphanTransmitter(Transmitter<?, ?, ?> transmitter) {
      if (!getInstance().invalidTransmitters.remove(transmitter)) {
         Coord4D coord = transmitter.getTileCoord();
         Transmitter<?, ?, ?> previous = getInstance().newOrphanTransmitters.put(coord, transmitter);
         if (previous != null && previous != transmitter && previous.isValid()) {
            Mekanism.logger.error("Different orphan transmitter was already registered at location! {}", coord);
         }
      }
   }

   public static void registerChangedNetwork(DynamicNetwork<?, ?, ?> network) {
      getInstance().networksToChange.add(network);
   }

   public static TransmitterNetworkRegistry getInstance() {
      return INSTANCE;
   }

   public void registerNetwork(DynamicNetwork<?, ?, ?> network) {
      this.networks.add(network);
   }

   public void removeNetwork(DynamicNetwork<?, ?, ?> network) {
      this.networks.remove(network);
      this.networksToChange.remove(network);
   }

   @SubscribeEvent
   public void onTick(ServerTickEvent event) {
      if (event.phase == Phase.END && event.side.isServer()) {
         this.handleChangedChunks();
         this.removeInvalidTransmitters();
         this.assignOrphans();
         this.commitChanges();

         for (DynamicNetwork<?, ?, ?> net : this.networks) {
            net.onUpdate();
         }
      }
   }

   @SubscribeEvent
   public void onTicketLevelChange(ChunkTicketLevelUpdatedEvent event) {
      int newTicketLevel = event.getNewTicketLevel();
      int oldTicketLevel = event.getOldTicketLevel();
      boolean loaded;
      if (oldTicketLevel > 32 && newTicketLevel <= 32) {
         loaded = true;
      } else {
         if (newTicketLevel <= 32 || oldTicketLevel > 32) {
            return;
         }

         loaded = false;
      }

      Chunk3D chunk = new Chunk3D(event.getLevel().m_46472_(), event.getChunkPos());
      if (this.transmitters.containsKey(chunk)) {
         if (this.changedTicketChunks.getOrDefault(chunk, loaded) != loaded) {
            this.changedTicketChunks.removeBoolean(chunk);
         } else {
            this.changedTicketChunks.put(chunk, loaded);
         }
      }
   }

   private void handleChangedChunks() {
      if (!this.changedTicketChunks.isEmpty()) {
         Object2BooleanMap<Chunk3D> changed = this.changedTicketChunks;
         this.changedTicketChunks = new Object2BooleanOpenHashMap();
         if (MekanismAPI.debug) {
            Mekanism.logger.info("Dealing with {} changed chunks", changed.size());
         }

         ObjectIterator var2 = changed.object2BooleanEntrySet().iterator();

         while (var2.hasNext()) {
            Entry<Chunk3D> entry = (Entry<Chunk3D>)var2.next();
            Chunk3D chunk = (Chunk3D)entry.getKey();
            boolean loaded = entry.getBooleanValue();
            Collection<Transmitter<?, ?, ?>> chunkTransmitters = this.transmitters.get(chunk);

            for (Transmitter<?, ?, ?> transmitter : chunkTransmitters) {
               transmitter.getTransmitterTile().chunkAccessibilityChange(loaded);
            }

            if (MekanismAPI.debug) {
               Mekanism.logger
                  .info(
                     "{} {} transmitters in chunk: {}, {}",
                     new Object[]{loaded ? "Loaded" : "Unloaded", chunkTransmitters.size(), chunk.f_45578_, chunk.f_45579_}
                  );
            }
         }
      }
   }

   private void removeInvalidTransmitters() {
      if (!this.invalidTransmitters.isEmpty()) {
         Set<Transmitter<?, ?, ?>> toInvalidate = this.invalidTransmitters;
         this.invalidTransmitters = new ObjectOpenHashSet();
         if (MekanismAPI.debug) {
            Mekanism.logger.info("Dealing with {} invalid Transmitters", toInvalidate.size());
         }

         for (Transmitter<?, ?, ?> invalid : toInvalidate) {
            this.removeInvalidTransmitter(invalid);
         }
      }
   }

   private <NETWORK extends DynamicNetwork<?, NETWORK, TRANSMITTER>, TRANSMITTER extends Transmitter<?, NETWORK, TRANSMITTER>> void removeInvalidTransmitter(
      Transmitter<?, NETWORK, TRANSMITTER> invalid
   ) {
      if (!invalid.isOrphan() || !invalid.isValid()) {
         NETWORK n = invalid.getTransmitterNetwork();
         if (n != null) {
            n.invalidate((TRANSMITTER)invalid);
            if (!invalid.isValid()) {
               invalid.setTransmitterNetwork(null, false);
            }
         }
      }
   }

   private void assignOrphans() {
      if (!this.newOrphanTransmitters.isEmpty()) {
         Map<Coord4D, Transmitter<?, ?, ?>> orphanTransmitters = this.newOrphanTransmitters;
         this.newOrphanTransmitters = new Object2ObjectOpenHashMap();
         if (MekanismAPI.debug) {
            Mekanism.logger.info("Dealing with {} orphan Transmitters", orphanTransmitters.size());
         }

         for (Transmitter<?, ?, ?> orphanTransmitter : orphanTransmitters.values()) {
            if (orphanTransmitter.isValid() && orphanTransmitter.isOrphan()) {
               TransmitterNetworkRegistry.OrphanPathFinder<?, ?, ?> finder = new TransmitterNetworkRegistry.OrphanPathFinder<>(orphanTransmitter);
               this.networksToChange.add(finder.getNetworkFromOrphan(orphanTransmitters));
            }
         }
      }
   }

   private void commitChanges() {
      if (!this.networksToChange.isEmpty()) {
         Set<DynamicNetwork<?, ?, ?>> networks = this.networksToChange;
         this.networksToChange = new ObjectOpenHashSet();

         for (DynamicNetwork<?, ?, ?> network : networks) {
            network.commit();
         }
      }
   }

   @Override
   public String toString() {
      return "Network Registry:\n" + this.networks;
   }

   public Component[] toComponents() {
      Component[] components = new Component[this.networks.size()];
      int i = 0;

      for (DynamicNetwork<?, ?, ?> network : this.networks) {
         components[i++] = network.getTextComponent();
      }

      return components;
   }

   public static class OrphanPathFinder<ACCEPTOR, NETWORK extends DynamicNetwork<ACCEPTOR, NETWORK, TRANSMITTER>, TRANSMITTER extends Transmitter<ACCEPTOR, NETWORK, TRANSMITTER>> {
      private final CompatibleTransmitterValidator<ACCEPTOR, NETWORK, TRANSMITTER> transmitterValidator;
      private final Set<TRANSMITTER> connectedTransmitters = new ObjectOpenHashSet();
      private final Long2ObjectMap<ChunkAccess> chunkMap = new Long2ObjectOpenHashMap();
      private final Set<NETWORK> networksFound = new ObjectOpenHashSet();
      private final Set<BlockPos> iterated = new ObjectOpenHashSet();
      private final Deque<BlockPos> queue = new LinkedList<>();
      private final TRANSMITTER startPoint;
      private final Level world;

      OrphanPathFinder(Transmitter<ACCEPTOR, NETWORK, TRANSMITTER> start) {
         this.startPoint = (TRANSMITTER)start;
         this.world = this.startPoint.getTileWorld();
         this.transmitterValidator = this.startPoint.getNewOrphanValidator();
      }

      NETWORK getNetworkFromOrphan(Map<Coord4D, Transmitter<?, ?, ?>> orphanTransmitters) {
         if (this.queue.peek() != null) {
            Mekanism.logger.error("OrphanPathFinder queue was not empty?!");
            this.queue.clear();
         }

         this.queue.push(this.startPoint.getTilePos());

         while (this.queue.peek() != null) {
            this.iterate(orphanTransmitters, this.queue.removeFirst());
         }

         NETWORK network;
         if (this.networksFound.size() == 1) {
            if (MekanismAPI.debug) {
               Mekanism.logger.info("Adding {} transmitters to single found network", this.connectedTransmitters.size());
            }

            network = this.networksFound.iterator().next();
         } else {
            if (MekanismAPI.debug) {
               if (this.networksFound.isEmpty()) {
                  Mekanism.logger.info("No networks found. Creating new network for {} transmitters", this.connectedTransmitters.size());
               } else {
                  Mekanism.logger.info("Merging {} networks with {} new transmitters", this.networksFound.size(), this.connectedTransmitters.size());
               }
            }

            network = this.startPoint.createNetworkByMerging(this.networksFound);
         }

         network.addNewTransmitters(this.connectedTransmitters, this.transmitterValidator);
         return network;
      }

      private void iterate(Map<Coord4D, Transmitter<?, ?, ?>> orphanTransmitters, BlockPos from) {
         if (this.iterated.add(from)) {
            Coord4D fromCoord = new Coord4D(from, this.world);
            Transmitter<?, ?, ?> transmitter = orphanTransmitters.get(fromCoord);
            if (transmitter != null) {
               if (transmitter.isValid()
                  && transmitter.isOrphan()
                  && this.startPoint.supportsTransmissionType(transmitter)
                  && this.transmitterValidator.isTransmitterCompatible(transmitter)) {
                  this.connectedTransmitters.add((TRANSMITTER)transmitter);
                  transmitter.setOrphan(false);

                  for (Direction direction : EnumUtils.DIRECTIONS) {
                     BlockPos directionPos = from.m_121945_(direction);
                     if (!this.iterated.contains(directionPos)) {
                        TileEntityTransmitter tile = WorldUtils.getTileEntity(TileEntityTransmitter.class, this.world, this.chunkMap, directionPos);
                        if (tile != null && transmitter.isValidTransmitterBasic(tile, direction)) {
                           this.queue.addLast(directionPos);
                        }
                     }
                  }
               }
            } else {
               TileEntityTransmitter tile = WorldUtils.getTileEntity(TileEntityTransmitter.class, this.world, this.chunkMap, from);
               if (tile != null && this.startPoint.supportsTransmissionType(tile)) {
                  NETWORK net = (NETWORK)tile.getTransmitter().getTransmitterNetwork();
                  if (net != null && this.transmitterValidator.isNetworkCompatible(net)) {
                     this.networksFound.add(net);
                  }
               }
            }
         }
      }
   }
}
