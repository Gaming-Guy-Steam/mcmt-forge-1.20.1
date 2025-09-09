package mekanism.common.content.network.transmitter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.math.FloatingLong;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.content.network.EnergyNetwork;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.lib.transmitter.acceptor.EnergyAcceptorCache;
import mekanism.common.tier.CableTier;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.upgrade.transmitter.TransmitterUpgradeData;
import mekanism.common.upgrade.transmitter.UniversalCableUpgradeData;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UniversalCable
   extends BufferedTransmitter<IStrictEnergyHandler, EnergyNetwork, FloatingLong, UniversalCable>
   implements IMekanismStrictEnergyHandler,
   IUpgradeableTransmitter<UniversalCableUpgradeData> {
   public final CableTier tier;
   private final List<IEnergyContainer> energyContainers;
   public final BasicEnergyContainer buffer;
   public FloatingLong lastWrite = FloatingLong.ZERO;

   public UniversalCable(IBlockProvider blockProvider, TileEntityTransmitter tile) {
      super(tile, TransmissionType.ENERGY);
      this.tier = Attribute.getTier(blockProvider, CableTier.class);
      this.buffer = BasicEnergyContainer.create(this.getCapacityAsFloatingLong(), BasicEnergyContainer.alwaysFalse, BasicEnergyContainer.alwaysTrue, this);
      this.energyContainers = Collections.singletonList(this.buffer);
   }

   protected EnergyAcceptorCache createAcceptorCache() {
      return new EnergyAcceptorCache(this, this.getTransmitterTile());
   }

   public EnergyAcceptorCache getAcceptorCache() {
      return (EnergyAcceptorCache)super.getAcceptorCache();
   }

   public CableTier getTier() {
      return this.tier;
   }

   @Override
   public void pullFromAcceptors() {
      Set<Direction> connections = this.getConnections(ConnectionType.PULL);
      if (!connections.isEmpty()) {
         for (IStrictEnergyHandler connectedAcceptor : this.getAcceptorCache().getConnectedAcceptors(connections)) {
            FloatingLong received = connectedAcceptor.extractEnergy(this.getAvailablePull(), Action.SIMULATE);
            if (!received.isZero() && this.takeEnergy(received, Action.SIMULATE).isZero()) {
               FloatingLong remainder = this.takeEnergy(received, Action.EXECUTE);
               connectedAcceptor.extractEnergy(received.subtract(remainder), Action.EXECUTE);
            }
         }
      }
   }

   private FloatingLong getAvailablePull() {
      return this.hasTransmitterNetwork()
         ? this.getCapacityAsFloatingLong().min(this.getTransmitterNetwork().energyContainer.getNeeded())
         : this.getCapacityAsFloatingLong().min(this.buffer.getNeeded());
   }

   @NotNull
   @Override
   public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
      return this.hasTransmitterNetwork() ? this.getTransmitterNetwork().getEnergyContainers(side) : this.energyContainers;
   }

   @Override
   public void onContentsChanged() {
      this.getTransmitterTile().m_6596_();
   }

   @Nullable
   public UniversalCableUpgradeData getUpgradeData() {
      return new UniversalCableUpgradeData(this.redstoneReactive, this.getConnectionTypesRaw(), this.buffer);
   }

   @Override
   public boolean dataTypeMatches(@NotNull TransmitterUpgradeData data) {
      return data instanceof UniversalCableUpgradeData;
   }

   public void parseUpgradeData(@NotNull UniversalCableUpgradeData data) {
      this.redstoneReactive = data.redstoneReactive;
      this.setConnectionTypesRaw(data.connectionTypes);
      this.buffer.setEnergy(data.buffer.getEnergy());
   }

   @Override
   public void read(@NotNull CompoundTag nbtTags) {
      super.read(nbtTags);
      if (nbtTags.m_128425_("energy", 8)) {
         try {
            this.lastWrite = FloatingLong.parseFloatingLong(nbtTags.m_128461_("energy"));
         } catch (NumberFormatException var3) {
            this.lastWrite = FloatingLong.ZERO;
         }
      } else {
         this.lastWrite = FloatingLong.ZERO;
      }

      this.buffer.setEnergy(this.lastWrite);
   }

   @NotNull
   @Override
   public CompoundTag write(@NotNull CompoundTag nbtTags) {
      super.write(nbtTags);
      if (this.hasTransmitterNetwork()) {
         this.getTransmitterNetwork().validateSaveShares(this);
      }

      if (this.lastWrite.isZero()) {
         nbtTags.m_128473_("energy");
      } else {
         nbtTags.m_128359_("energy", this.lastWrite.toString());
      }

      return nbtTags;
   }

   public EnergyNetwork createNetworkByMerging(Collection<EnergyNetwork> networks) {
      return new EnergyNetwork(networks);
   }

   @Override
   public boolean isValidAcceptor(BlockEntity tile, Direction side) {
      return super.isValidAcceptor(tile, side) && this.getAcceptorCache().hasStrictEnergyHandlerAndListen(tile, side);
   }

   public EnergyNetwork createEmptyNetworkWithID(UUID networkID) {
      return new EnergyNetwork(networkID);
   }

   @NotNull
   public FloatingLong releaseShare() {
      FloatingLong energy = this.buffer.getEnergy();
      this.buffer.setEmpty();
      return energy;
   }

   @NotNull
   public FloatingLong getShare() {
      return this.buffer.getEnergy();
   }

   @Override
   public boolean noBufferOrFallback() {
      return this.getBufferWithFallback().isZero();
   }

   @NotNull
   public FloatingLong getBufferWithFallback() {
      FloatingLong buffer = this.getShare();
      return buffer.isZero() && this.hasTransmitterNetwork() ? this.getTransmitterNetwork().getBuffer() : buffer;
   }

   @Override
   public void takeShare() {
      if (this.hasTransmitterNetwork()) {
         EnergyNetwork transmitterNetwork = this.getTransmitterNetwork();
         if (!transmitterNetwork.energyContainer.isEmpty() && !this.lastWrite.isZero()) {
            transmitterNetwork.energyContainer.setEnergy(transmitterNetwork.energyContainer.getEnergy().subtract(this.lastWrite));
            this.buffer.setEnergy(this.lastWrite);
         }
      }
   }

   @NotNull
   public FloatingLong getCapacityAsFloatingLong() {
      return this.tier.getCableCapacity();
   }

   @Override
   public long getCapacity() {
      return this.getCapacityAsFloatingLong().longValue();
   }

   private FloatingLong takeEnergy(FloatingLong amount, Action action) {
      return this.hasTransmitterNetwork()
         ? this.getTransmitterNetwork().energyContainer.insert(amount, action, AutomationType.INTERNAL)
         : this.buffer.insert(amount, action, AutomationType.INTERNAL);
   }

   protected void handleContentsUpdateTag(@NotNull EnergyNetwork network, @NotNull CompoundTag tag) {
      super.handleContentsUpdateTag(network, tag);
      NBTUtils.setFloatingLongIfPresent(tag, "energy", network.energyContainer::setEnergy);
      NBTUtils.setFloatIfPresent(tag, "scale", scale -> network.currentScale = scale);
   }
}
