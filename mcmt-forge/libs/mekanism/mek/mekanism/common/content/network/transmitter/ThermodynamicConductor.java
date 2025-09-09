package mekanism.common.content.network.transmitter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import mekanism.api.DataHandlerUtils;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IHeatHandler;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.heat.CachedAmbientTemperature;
import mekanism.common.capabilities.heat.ITileHeatHandler;
import mekanism.common.capabilities.heat.VariableHeatCapacitor;
import mekanism.common.content.network.HeatNetwork;
import mekanism.common.lib.Color;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.lib.transmitter.acceptor.AcceptorCache;
import mekanism.common.tier.ConductorTier;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.upgrade.transmitter.ThermodynamicConductorUpgradeData;
import mekanism.common.upgrade.transmitter.TransmitterUpgradeData;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ThermodynamicConductor
   extends Transmitter<IHeatHandler, HeatNetwork, ThermodynamicConductor>
   implements ITileHeatHandler,
   IUpgradeableTransmitter<ThermodynamicConductorUpgradeData> {
   private final CachedAmbientTemperature ambientTemperature = new CachedAmbientTemperature(this::getTileWorld, this::getTilePos);
   public final ConductorTier tier;
   private double clientTemperature = -1.0;
   private final List<IHeatCapacitor> capacitors;
   public final VariableHeatCapacitor buffer;

   public ThermodynamicConductor(IBlockProvider blockProvider, TileEntityTransmitter tile) {
      super(tile, TransmissionType.HEAT);
      this.tier = Attribute.getTier(blockProvider, ConductorTier.class);
      this.buffer = VariableHeatCapacitor.create(
         this.tier.getHeatCapacity(), this.tier::getInverseConduction, this.tier::getInverseConductionInsulation, this.ambientTemperature, this
      );
      this.capacitors = Collections.singletonList(this.buffer);
   }

   public AcceptorCache<IHeatHandler> getAcceptorCache() {
      return (AcceptorCache<IHeatHandler>)super.getAcceptorCache();
   }

   public ConductorTier getTier() {
      return this.tier;
   }

   public HeatNetwork createEmptyNetworkWithID(UUID networkID) {
      return new HeatNetwork(networkID);
   }

   public HeatNetwork createNetworkByMerging(Collection<HeatNetwork> networks) {
      return new HeatNetwork(networks);
   }

   @Override
   public void takeShare() {
   }

   @Override
   public boolean isValidAcceptor(BlockEntity tile, Direction side) {
      return this.getAcceptorCache().isAcceptorAndListen(tile, side, Capabilities.HEAT_HANDLER);
   }

   @Nullable
   public ThermodynamicConductorUpgradeData getUpgradeData() {
      return new ThermodynamicConductorUpgradeData(this.redstoneReactive, this.getConnectionTypesRaw(), this.buffer.getHeat());
   }

   @Override
   public boolean dataTypeMatches(@NotNull TransmitterUpgradeData data) {
      return data instanceof ThermodynamicConductorUpgradeData;
   }

   public void parseUpgradeData(@NotNull ThermodynamicConductorUpgradeData data) {
      this.redstoneReactive = data.redstoneReactive;
      this.setConnectionTypesRaw(data.connectionTypes);
      this.buffer.setHeat(data.heat);
   }

   @NotNull
   @Override
   public CompoundTag write(@NotNull CompoundTag tag) {
      super.write(tag);
      tag.m_128365_("HeatCapacitors", DataHandlerUtils.writeContainers(this.getHeatCapacitors(null)));
      return tag;
   }

   @Override
   public void read(@NotNull CompoundTag tag) {
      super.read(tag);
      DataHandlerUtils.readContainers(this.getHeatCapacitors(null), tag.m_128437_("HeatCapacitors", 10));
   }

   @NotNull
   @Override
   public CompoundTag getReducedUpdateTag(CompoundTag updateTag) {
      updateTag = super.getReducedUpdateTag(updateTag);
      updateTag.m_128347_("temperature", this.buffer.getHeat());
      return updateTag;
   }

   @Override
   public void handleUpdateTag(@NotNull CompoundTag tag) {
      super.handleUpdateTag(tag);
      NBTUtils.setDoubleIfPresent(tag, "temperature", this.buffer::setHeat);
   }

   public Color getBaseColor() {
      return this.tier.getBaseColor();
   }

   @NotNull
   @Override
   public List<IHeatCapacitor> getHeatCapacitors(Direction side) {
      return this.capacitors;
   }

   @Override
   public void onContentsChanged() {
      if (!this.isRemote()) {
         if (this.clientTemperature == -1.0) {
            this.clientTemperature = this.ambientTemperature.getAsDouble();
         }

         if (Math.abs(this.buffer.getTemperature() - this.clientTemperature) > this.buffer.getTemperature() / 20.0) {
            this.clientTemperature = this.buffer.getTemperature();
            this.getTransmitterTile().sendUpdatePacket();
         }
      }

      this.getTransmitterTile().m_6596_();
   }

   @Override
   public double getAmbientTemperature(@NotNull Direction side) {
      return this.ambientTemperature.getTemperature(side);
   }

   @Nullable
   @Override
   public IHeatHandler getAdjacent(@NotNull Direction side) {
      return connectionMapContainsSide(this.getAllCurrentConnections(), side)
         ? (IHeatHandler)this.getAcceptorCache().getConnectedAcceptor(side).resolve().orElse(null)
         : null;
   }

   @Override
   public double incrementAdjacentTransfer(double currentAdjacentTransfer, double tempToTransfer, @NotNull Direction side) {
      return tempToTransfer > 0.0
            && this.getAcceptorCache().getConnectedAcceptorTile(side) instanceof TileEntityTransmitter transmitter
            && TransmissionType.HEAT.checkTransmissionType(transmitter)
         ? currentAdjacentTransfer
         : ITileHeatHandler.super.incrementAdjacentTransfer(currentAdjacentTransfer, tempToTransfer, side);
   }
}
