package mekanism.common.lib.transmitter;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import mekanism.api.text.IHasTextComponent;
import mekanism.common.content.network.transmitter.Transmitter;
import mekanism.common.lib.transmitter.acceptor.NetworkAcceptorCache;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class DynamicNetwork<ACCEPTOR, NETWORK extends DynamicNetwork<ACCEPTOR, NETWORK, TRANSMITTER>, TRANSMITTER extends Transmitter<ACCEPTOR, NETWORK, TRANSMITTER>>
   implements INetworkDataHandler,
   IHasTextComponent {
   protected final Set<TRANSMITTER> transmitters = new ObjectOpenHashSet();
   protected final Set<TRANSMITTER> transmittersToAdd = new ObjectOpenHashSet();
   protected final NetworkAcceptorCache<ACCEPTOR> acceptorCache = new NetworkAcceptorCache<>();
   @Nullable
   protected Level world;
   private final UUID uuid;
   @Nullable
   private CompatibleTransmitterValidator<ACCEPTOR, NETWORK, TRANSMITTER> transmitterValidator;

   protected DynamicNetwork(UUID networkID) {
      this.uuid = networkID;
   }

   public UUID getUUID() {
      return this.uuid;
   }

   protected NETWORK getNetwork() {
      return (NETWORK)this;
   }

   public void commit() {
      if (!this.transmittersToAdd.isEmpty()) {
         boolean addedValidTransmitters = false;
         List<TRANSMITTER> transmittersToUpdate = new ArrayList<>();

         for (TRANSMITTER transmitter : this.transmittersToAdd) {
            if (transmitter != null && transmitter.isValid()) {
               addedValidTransmitters = true;
               if (this.world == null) {
                  this.world = transmitter.getTileWorld();
               }

               for (Direction side : EnumUtils.DIRECTIONS) {
                  this.acceptorCache.updateTransmitterOnSide(transmitter, side);
               }

               if (transmitter.setTransmitterNetwork(this.getNetwork(), false)) {
                  transmittersToUpdate.add(transmitter);
               }

               this.addTransmitterFromCommit(transmitter);
            }
         }

         this.transmittersToAdd.clear();
         if (addedValidTransmitters) {
            this.validTransmittersAdded();
            transmittersToUpdate.forEach(Transmitter::requestsUpdate);
         }
      }

      this.acceptorCache.commit();
      this.transmitterValidator = null;
   }

   @Nullable
   public CompatibleTransmitterValidator<ACCEPTOR, NETWORK, TRANSMITTER> getTransmitterValidator() {
      return this.transmitterValidator;
   }

   public void addNewTransmitters(Collection<TRANSMITTER> newTransmitters, CompatibleTransmitterValidator<ACCEPTOR, NETWORK, TRANSMITTER> transmitterValidator) {
      this.transmittersToAdd.addAll(newTransmitters);
      this.transmitterValidator = transmitterValidator;
   }

   protected void addTransmitterFromCommit(TRANSMITTER transmitter) {
      this.transmitters.add(transmitter);
   }

   protected void validTransmittersAdded() {
   }

   public boolean isRemote() {
      return this.world == null ? EffectiveSide.get().isClient() : this.world.f_46443_;
   }

   public void invalidate(@Nullable TRANSMITTER triggerTransmitter) {
      if (this.transmitters.size() == 1 && triggerTransmitter != null && !triggerTransmitter.isValid()) {
         this.onLastTransmitterRemoved(triggerTransmitter);
      }

      this.removeInvalid(triggerTransmitter);
      if (!this.isRemote()) {
         for (TRANSMITTER transmitter : this.transmitters) {
            if (transmitter.isValid()) {
               transmitter.takeShare();
               transmitter.setTransmitterNetwork(null);
               TransmitterNetworkRegistry.registerOrphanTransmitter(transmitter);
            }
         }
      }

      this.deregister();
   }

   protected void onLastTransmitterRemoved(@NotNull TRANSMITTER triggerTransmitter) {
   }

   protected void removeInvalid(@Nullable TRANSMITTER triggerTransmitter) {
      this.transmitters.removeIf(transmitter -> !transmitter.isValid());
   }

   public void acceptorChanged(TRANSMITTER transmitter, Direction side) {
      this.acceptorCache.acceptorChanged(transmitter, side);
   }

   public List<TRANSMITTER> adoptTransmittersAndAcceptorsFrom(NETWORK net) {
      List<TRANSMITTER> transmittersToUpdate = new ArrayList<>();

      for (TRANSMITTER transmitter : net.transmitters) {
         this.transmitters.add(transmitter);
         if (transmitter.setTransmitterNetwork(this.getNetwork(), false)) {
            transmittersToUpdate.add(transmitter);
         }
      }

      this.transmittersToAdd.addAll(net.transmittersToAdd);
      this.acceptorCache.adoptAcceptors(net.acceptorCache);
      return transmittersToUpdate;
   }

   protected void adoptAllAndRegister(Collection<NETWORK> networks) {
      List<TRANSMITTER> transmittersToUpdate = new ArrayList<>();

      for (NETWORK net : networks) {
         if (net != null) {
            transmittersToUpdate.addAll(this.adoptTransmittersAndAcceptorsFrom(net));
            net.deregister();
         }
      }

      this.register();
      transmittersToUpdate.forEach(Transmitter::requestsUpdate);
   }

   public void register() {
      if (this.isRemote()) {
         TransmitterNetworkRegistry.getInstance().addClientNetwork(this.getUUID(), this);
      } else {
         TransmitterNetworkRegistry.getInstance().registerNetwork(this);
      }
   }

   public void deregister() {
      this.transmitters.clear();
      this.transmittersToAdd.clear();
      this.acceptorCache.deregister();
      this.transmitterValidator = null;
      if (this.isRemote()) {
         TransmitterNetworkRegistry.getInstance().removeClientNetwork(this);
      } else {
         TransmitterNetworkRegistry.getInstance().removeNetwork(this);
      }
   }

   public boolean isEmpty() {
      return this.transmitters.isEmpty();
   }

   public int getAcceptorCount() {
      return this.acceptorCache.getAcceptorCount();
   }

   @Nullable
   public Level getWorld() {
      return this.world;
   }

   public void onUpdate() {
   }

   public Set<TRANSMITTER> getTransmitters() {
      return this.transmitters;
   }

   public void addTransmitter(TRANSMITTER transmitter) {
      this.transmitters.add(transmitter);
   }

   public void removeTransmitter(TRANSMITTER transmitter) {
      this.transmitters.remove(transmitter);
      if (this.transmitters.isEmpty()) {
         this.deregister();
      }
   }

   public int transmittersSize() {
      return this.transmitters.size();
   }

   public boolean hasAcceptor(BlockPos acceptorPos) {
      return this.acceptorCache.hasAcceptor(acceptorPos);
   }

   public Set<Direction> getAcceptorDirections(BlockPos pos) {
      return this.acceptorCache.getAcceptorDirections(pos);
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else {
         return o instanceof DynamicNetwork<?, ?, ?> other ? this.uuid.equals(other.uuid) : false;
      }
   }

   @Override
   public int hashCode() {
      return this.uuid.hashCode();
   }
}
