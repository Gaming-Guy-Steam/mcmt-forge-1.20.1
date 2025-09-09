package mekanism.common.tile;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IHeatHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.heat.CachedAmbientTemperature;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.chemical.QuantumEntangloporterChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.energy.QuantumEntangloporterEnergyContainerHolder;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.fluid.QuantumEntangloporterFluidTankHolder;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.capabilities.holder.heat.QuantumEntangloporterHeatCapacitorHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.QuantumEntangloporterInventorySlotHolder;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.lib.chunkloading.IChunkLoader;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.component.TileComponentChunkLoader;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.IProxiedSlotInfo;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityQuantumEntangloporter extends TileEntityConfigurableMachine implements IChunkLoader {
   private final TileComponentChunkLoader<TileEntityQuantumEntangloporter> chunkLoaderComponent;
   private double lastTransferLoss;
   private double lastEnvironmentLoss;

   public TileEntityQuantumEntangloporter(BlockPos pos, BlockState state) {
      super(MekanismBlocks.QUANTUM_ENTANGLOPORTER, pos, state);
      this.configComponent = new TileComponentConfig(
         this,
         TransmissionType.ITEM,
         TransmissionType.FLUID,
         TransmissionType.GAS,
         TransmissionType.INFUSION,
         TransmissionType.PIGMENT,
         TransmissionType.SLURRY,
         TransmissionType.ENERGY,
         TransmissionType.HEAT
      );
      this.setupConfig(
         TransmissionType.ITEM,
         IProxiedSlotInfo.InventoryProxy::new,
         () -> this.hasFrequency() ? this.getFreq().getInventorySlots(null) : Collections.emptyList()
      );
      this.setupConfig(
         TransmissionType.FLUID, IProxiedSlotInfo.FluidProxy::new, () -> this.hasFrequency() ? this.getFreq().getFluidTanks(null) : Collections.emptyList()
      );
      this.setupConfig(
         TransmissionType.GAS, IProxiedSlotInfo.GasProxy::new, () -> this.hasFrequency() ? this.getFreq().getGasTanks(null) : Collections.emptyList()
      );
      this.setupConfig(
         TransmissionType.INFUSION,
         IProxiedSlotInfo.InfusionProxy::new,
         () -> this.hasFrequency() ? this.getFreq().getInfusionTanks(null) : Collections.emptyList()
      );
      this.setupConfig(
         TransmissionType.PIGMENT,
         IProxiedSlotInfo.PigmentProxy::new,
         () -> this.hasFrequency() ? this.getFreq().getPigmentTanks(null) : Collections.emptyList()
      );
      this.setupConfig(
         TransmissionType.SLURRY, IProxiedSlotInfo.SlurryProxy::new, () -> this.hasFrequency() ? this.getFreq().getSlurryTanks(null) : Collections.emptyList()
      );
      this.setupConfig(
         TransmissionType.ENERGY,
         IProxiedSlotInfo.EnergyProxy::new,
         () -> this.hasFrequency() ? this.getFreq().getEnergyContainers(null) : Collections.emptyList()
      );
      ConfigInfo heatConfig = this.configComponent.getConfig(TransmissionType.HEAT);
      if (heatConfig != null) {
         Supplier<List<IHeatCapacitor>> capacitorSupplier = () -> this.hasFrequency() ? this.getFreq().getHeatCapacitors(null) : Collections.emptyList();
         heatConfig.addSlotInfo(DataType.INPUT_OUTPUT, new IProxiedSlotInfo.HeatProxy(true, false, capacitorSupplier));
         heatConfig.fill(DataType.INPUT_OUTPUT);
         heatConfig.setCanEject(false);
      }

      this.ejectorComponent = new TileComponentEjector(this);
      this.ejectorComponent
         .setOutputData(this.configComponent, TransmissionType.ITEM)
         .setCanEject(type -> this.hasFrequency() && MekanismUtils.canFunction(this));
      this.chunkLoaderComponent = new TileComponentChunkLoader<>(this);
      this.frequencyComponent.track(FrequencyType.INVENTORY, true, true, true);
      this.cacheCoord();
   }

   private <T> void setupConfig(TransmissionType type, IProxiedSlotInfo.ProxySlotInfoCreator<T> proxyCreator, Supplier<List<T>> supplier) {
      ConfigInfo config = this.configComponent.getConfig(type);
      if (config != null) {
         config.addSlotInfo(DataType.INPUT, proxyCreator.create(true, false, supplier));
         config.addSlotInfo(DataType.OUTPUT, proxyCreator.create(false, true, supplier));
         config.addSlotInfo(DataType.INPUT_OUTPUT, proxyCreator.create(true, true, supplier));
         config.fill(DataType.INPUT);
         config.setDataType(DataType.OUTPUT, RelativeSide.FRONT);
      }
   }

   @NotNull
   @Override
   public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks(IContentsListener listener) {
      return new QuantumEntangloporterChemicalTankHolder<>(this, TransmissionType.GAS, InventoryFrequency::getGasTanks);
   }

   @NotNull
   @Override
   public IChemicalTankHolder<InfuseType, InfusionStack, IInfusionTank> getInitialInfusionTanks(IContentsListener listener) {
      return new QuantumEntangloporterChemicalTankHolder<>(this, TransmissionType.INFUSION, InventoryFrequency::getInfusionTanks);
   }

   @NotNull
   @Override
   public IChemicalTankHolder<Pigment, PigmentStack, IPigmentTank> getInitialPigmentTanks(IContentsListener listener) {
      return new QuantumEntangloporterChemicalTankHolder<>(this, TransmissionType.PIGMENT, InventoryFrequency::getPigmentTanks);
   }

   @NotNull
   @Override
   public IChemicalTankHolder<Slurry, SlurryStack, ISlurryTank> getInitialSlurryTanks(IContentsListener listener) {
      return new QuantumEntangloporterChemicalTankHolder<>(this, TransmissionType.SLURRY, InventoryFrequency::getSlurryTanks);
   }

   @NotNull
   @Override
   protected IFluidTankHolder getInitialFluidTanks(IContentsListener listener) {
      return new QuantumEntangloporterFluidTankHolder(this);
   }

   @NotNull
   @Override
   protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
      return new QuantumEntangloporterEnergyContainerHolder(this);
   }

   @NotNull
   @Override
   protected IHeatCapacitorHolder getInitialHeatCapacitors(IContentsListener listener, CachedAmbientTemperature ambientTemperature) {
      return new QuantumEntangloporterHeatCapacitorHolder(this);
   }

   @NotNull
   @Override
   protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
      return new QuantumEntangloporterInventorySlotHolder(this);
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      InventoryFrequency freq = this.getFreq();
      if (freq != null && freq.isValid() && !freq.isRemoved()) {
         freq.handleEject(this.f_58857_.m_46467_());
         this.updateHeatCapacitors(null);
         HeatAPI.HeatTransfer loss = this.simulate();
         this.lastTransferLoss = loss.adjacentTransfer();
         this.lastEnvironmentLoss = loss.environmentTransfer();
      } else {
         this.lastTransferLoss = 0.0;
         this.lastEnvironmentLoss = 0.0;
      }
   }

   @ComputerMethod
   public boolean hasFrequency() {
      Frequency freq = this.getFreq();
      return freq != null && freq.isValid() && !freq.isRemoved();
   }

   @Override
   public boolean persistInventory() {
      return false;
   }

   @Override
   public boolean persists(SubstanceType type) {
      return false;
   }

   @Override
   public boolean shouldDumpRadiation() {
      return false;
   }

   @Nullable
   @Override
   public IHeatHandler getAdjacent(@NotNull Direction side) {
      if (this.hasFrequency()) {
         ISlotInfo slotInfo = this.configComponent.getSlotInfo(TransmissionType.HEAT, side);
         if (slotInfo != null && slotInfo.canInput()) {
            BlockEntity adj = WorldUtils.getTileEntity(this.m_58904_(), this.m_58899_().m_121945_(side));
            return (IHeatHandler)CapabilityUtils.getCapability(adj, Capabilities.HEAT_HANDLER, side.m_122424_()).resolve().orElse(null);
         }
      }

      return null;
   }

   @Override
   public TileComponentChunkLoader<TileEntityQuantumEntangloporter> getChunkLoader() {
      return this.chunkLoaderComponent;
   }

   @Override
   public Set<ChunkPos> getChunkSet() {
      return Collections.singleton(new ChunkPos(this.m_58899_()));
   }

   public InventoryFrequency getFreq() {
      return this.getFrequency(FrequencyType.INVENTORY);
   }

   @ComputerMethod(
      nameOverride = "getTransferLoss",
      methodDescription = "May not be accurate if there is no frequency"
   )
   public double getLastTransferLoss() {
      return this.lastTransferLoss;
   }

   @ComputerMethod(
      nameOverride = "getEnvironmentalLoss",
      methodDescription = "May not be accurate if there is no frequency"
   )
   public double getLastEnvironmentLoss() {
      return this.lastEnvironmentLoss;
   }

   @Override
   public void addContainerTrackers(MekanismContainer container) {
      super.addContainerTrackers(container);
      container.track(SyncableDouble.create(this::getLastTransferLoss, value -> this.lastTransferLoss = value));
      container.track(SyncableDouble.create(this::getLastEnvironmentLoss, value -> this.lastEnvironmentLoss = value));
   }

   @ComputerMethod(
      methodDescription = "Lists public frequencies"
   )
   Collection<InventoryFrequency> getFrequencies() {
      return FrequencyType.INVENTORY.getManagerWrapper().getPublicManager().getFrequencies();
   }

   @ComputerMethod(
      methodDescription = "Requires a frequency to be selected"
   )
   InventoryFrequency getFrequency() throws ComputerException {
      InventoryFrequency frequency = this.getFreq();
      if (frequency != null && frequency.isValid() && !frequency.isRemoved()) {
         return frequency;
      } else {
         throw new ComputerException("No frequency is currently selected.");
      }
   }

   @ComputerMethod(
      requiresPublicSecurity = true,
      methodDescription = "Requires a public frequency to exist"
   )
   void setFrequency(String name) throws ComputerException {
      this.validateSecurityIsPublic();
      InventoryFrequency frequency = FrequencyType.INVENTORY.getManagerWrapper().getPublicManager().getFrequency(name);
      if (frequency == null) {
         throw new ComputerException("No public inventory frequency with name '%s' found.", name);
      } else {
         this.setFrequency(FrequencyType.INVENTORY, frequency.getIdentity(), this.getOwnerUUID());
      }
   }

   @ComputerMethod(
      requiresPublicSecurity = true,
      methodDescription = "Requires frequency to not already exist and for it to be public so that it can make it as the player who owns the block. Also sets the frequency after creation"
   )
   void createFrequency(String name) throws ComputerException {
      this.validateSecurityIsPublic();
      InventoryFrequency frequency = FrequencyType.INVENTORY.getManagerWrapper().getPublicManager().getFrequency(name);
      if (frequency != null) {
         throw new ComputerException("Unable to create public inventory frequency with name '%s' as one already exists.", name);
      } else {
         this.setFrequency(FrequencyType.INVENTORY, new Frequency.FrequencyIdentity(name, true), this.getOwnerUUID());
      }
   }

   @ComputerMethod
   ItemStack getBufferItem() throws ComputerException {
      return this.getFrequency().getInventorySlots(null).get(0).getStack();
   }

   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerFluidTankWrapper.class,
      methodNames = {"getBufferFluid", "getBufferFluidCapacity", "getBufferFluidNeeded", "getBufferFluidFilledPercentage"},
      docPlaceholder = "fluid buffer"
   )
   IExtendedFluidTank getBufferFluidTank() throws ComputerException {
      return this.getFrequency().getFluidTanks(null).get(0);
   }

   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.class,
      methodNames = {"getBufferGas", "getBufferGasCapacity", "getBufferGasNeeded", "getBufferGasFilledPercentage"},
      docPlaceholder = "gas buffer"
   )
   IGasTank getBufferGasTank() throws ComputerException {
      return this.getFrequency().getGasTanks(null).get(0);
   }

   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.class,
      methodNames = {"getBufferInfuseType", "getBufferInfuseTypeCapacity", "getBufferInfuseTypeNeeded", "getBufferInfuseTypeFilledPercentage"},
      docPlaceholder = "infusion buffer"
   )
   IInfusionTank getBufferInfuseTypeTank() throws ComputerException {
      return this.getFrequency().getInfusionTanks(null).get(0);
   }

   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.class,
      methodNames = {"getBufferPigment", "getBufferPigmentCapacity", "getBufferPigmentNeeded", "getBufferPigmentFilledPercentage"},
      docPlaceholder = "pigment buffer"
   )
   IPigmentTank getBufferPigmentTank() throws ComputerException {
      return this.getFrequency().getPigmentTanks(null).get(0);
   }

   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.class,
      methodNames = {"getBufferSlurry", "getBufferSlurryCapacity", "getBufferSlurryNeeded", "getBufferSlurryFilledPercentage"},
      docPlaceholder = "slurry buffer"
   )
   ISlurryTank getBufferSlurryTank() throws ComputerException {
      return this.getFrequency().getSlurryTanks(null).get(0);
   }

   @ComputerMethod(
      methodDescription = "Requires a frequency to be selected"
   )
   double getTemperature() throws ComputerException {
      return this.getFrequency().getTotalTemperature();
   }
}
