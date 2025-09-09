package mekanism.common.content.entangloporter;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.Coord4D;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.chemical.dynamic.IGasTracker;
import mekanism.common.capabilities.chemical.dynamic.IInfusionTracker;
import mekanism.common.capabilities.chemical.dynamic.IPigmentTracker;
import mekanism.common.capabilities.chemical.dynamic.ISlurryTracker;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.capabilities.heat.ITileHeatHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.network.distribution.ChemicalHandlerTarget;
import mekanism.common.content.network.distribution.EnergyAcceptorTarget;
import mekanism.common.content.network.distribution.FluidHandlerTarget;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.inventory.slot.EntangloporterInventorySlot;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.EmitUtils;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.FluidUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InventoryFrequency
   extends Frequency
   implements IMekanismInventory,
   IMekanismFluidHandler,
   IMekanismStrictEnergyHandler,
   ITileHeatHandler,
   IGasTracker,
   IInfusionTracker,
   IPigmentTracker,
   ISlurryTracker {
   private final Map<Coord4D, TileEntityQuantumEntangloporter> activeQEs = new Object2ObjectOpenHashMap();
   private long lastEject = -1L;
   private BasicFluidTank storedFluid;
   private IGasTank storedGas;
   private IInfusionTank storedInfusion;
   private IPigmentTank storedPigment;
   private ISlurryTank storedSlurry;
   private IInventorySlot storedItem;
   public IEnergyContainer storedEnergy;
   private BasicHeatCapacitor storedHeat;
   private List<IInventorySlot> inventorySlots;
   private List<IGasTank> gasTanks;
   private List<IInfusionTank> infusionTanks;
   private List<IPigmentTank> pigmentTanks;
   private List<ISlurryTank> slurryTanks;
   private List<IExtendedFluidTank> fluidTanks;
   private List<IEnergyContainer> energyContainers;
   private List<IHeatCapacitor> heatCapacitors;

   public InventoryFrequency(String n, @Nullable UUID uuid) {
      super(FrequencyType.INVENTORY, n, uuid);
      this.presetVariables();
   }

   public InventoryFrequency() {
      super(FrequencyType.INVENTORY);
      this.presetVariables();
   }

   private void presetVariables() {
      this.fluidTanks = Collections.singletonList(this.storedFluid = BasicFluidTank.create(MekanismConfig.general.entangloporterFluidBuffer.get(), this));
      this.gasTanks = Collections.singletonList(
         this.storedGas = (IGasTank)ChemicalTankBuilder.GAS.create(MekanismConfig.general.entangloporterChemicalBuffer.get(), this)
      );
      this.infusionTanks = Collections.singletonList(
         this.storedInfusion = (IInfusionTank)ChemicalTankBuilder.INFUSION.create(MekanismConfig.general.entangloporterChemicalBuffer.get(), this)
      );
      this.pigmentTanks = Collections.singletonList(
         this.storedPigment = (IPigmentTank)ChemicalTankBuilder.PIGMENT.create(MekanismConfig.general.entangloporterChemicalBuffer.get(), this)
      );
      this.slurryTanks = Collections.singletonList(
         this.storedSlurry = (ISlurryTank)ChemicalTankBuilder.SLURRY.create(MekanismConfig.general.entangloporterChemicalBuffer.get(), this)
      );
      this.inventorySlots = Collections.singletonList(this.storedItem = EntangloporterInventorySlot.create(this));
      this.energyContainers = Collections.singletonList(
         this.storedEnergy = BasicEnergyContainer.create(MekanismConfig.general.entangloporterEnergyBuffer.get(), this)
      );
      this.heatCapacitors = Collections.singletonList(this.storedHeat = BasicHeatCapacitor.create(1.0, 1.0, 1000.0, null, this));
   }

   @Override
   public void write(CompoundTag nbtTags) {
      super.write(nbtTags);
      nbtTags.m_128365_("energy", this.storedEnergy.serializeNBT());
      nbtTags.m_128365_("fluid", this.storedFluid.serializeNBT());
      nbtTags.m_128365_("gas", this.storedGas.serializeNBT());
      nbtTags.m_128365_("infuseType", this.storedInfusion.serializeNBT());
      nbtTags.m_128365_("pigment", this.storedPigment.serializeNBT());
      nbtTags.m_128365_("slurry", this.storedSlurry.serializeNBT());
      nbtTags.m_128365_("Item", this.storedItem.serializeNBT());
      nbtTags.m_128365_("heat", this.storedHeat.serializeNBT());
   }

   @Override
   protected void read(CompoundTag nbtTags) {
      super.read(nbtTags);
      this.storedEnergy.deserializeNBT(nbtTags.m_128469_("energy"));
      this.storedFluid.deserializeNBT(nbtTags.m_128469_("fluid"));
      this.storedGas.deserializeNBT(nbtTags.m_128469_("gas"));
      this.storedInfusion.deserializeNBT(nbtTags.m_128469_("infuseType"));
      this.storedPigment.deserializeNBT(nbtTags.m_128469_("pigment"));
      this.storedSlurry.deserializeNBT(nbtTags.m_128469_("slurry"));
      this.storedItem.deserializeNBT(nbtTags.m_128469_("Item"));
      this.storedHeat.deserializeNBT(nbtTags.m_128469_("heat"));
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      super.write(buffer);
      this.storedEnergy.getEnergy().writeToBuffer(buffer);
      buffer.writeFluidStack(this.storedFluid.getFluid());
      ChemicalUtils.writeChemicalStack(buffer, this.storedGas.getStack());
      ChemicalUtils.writeChemicalStack(buffer, this.storedInfusion.getStack());
      ChemicalUtils.writeChemicalStack(buffer, this.storedPigment.getStack());
      ChemicalUtils.writeChemicalStack(buffer, this.storedSlurry.getStack());
      buffer.m_130079_((CompoundTag)this.storedItem.serializeNBT());
      buffer.writeDouble(this.storedHeat.getHeat());
   }

   @Override
   protected void read(FriendlyByteBuf dataStream) {
      super.read(dataStream);
      this.presetVariables();
      this.storedEnergy.setEnergy(FloatingLong.readFromBuffer(dataStream));
      this.storedFluid.setStack(dataStream.readFluidStack());
      this.storedGas.setStack(ChemicalUtils.readGasStack(dataStream));
      this.storedInfusion.setStack(ChemicalUtils.readInfusionStack(dataStream));
      this.storedPigment.setStack(ChemicalUtils.readPigmentStack(dataStream));
      this.storedSlurry.setStack(ChemicalUtils.readSlurryStack(dataStream));
      this.storedItem.deserializeNBT(dataStream.m_130260_());
      this.storedHeat.setHeat(dataStream.readDouble());
   }

   @NotNull
   @Override
   public List<IInventorySlot> getInventorySlots(@Nullable Direction side) {
      return this.inventorySlots;
   }

   @NotNull
   @Override
   public List<IGasTank> getGasTanks(@Nullable Direction side) {
      return this.gasTanks;
   }

   @NotNull
   @Override
   public List<IInfusionTank> getInfusionTanks(@Nullable Direction side) {
      return this.infusionTanks;
   }

   @NotNull
   @Override
   public List<IPigmentTank> getPigmentTanks(@Nullable Direction side) {
      return this.pigmentTanks;
   }

   @NotNull
   @Override
   public List<ISlurryTank> getSlurryTanks(@Nullable Direction side) {
      return this.slurryTanks;
   }

   @NotNull
   @Override
   public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
      return this.fluidTanks;
   }

   @NotNull
   @Override
   public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
      return this.energyContainers;
   }

   @NotNull
   @Override
   public List<IHeatCapacitor> getHeatCapacitors(@Nullable Direction side) {
      return this.heatCapacitors;
   }

   @Override
   public void onContentsChanged() {
      this.dirty = true;
   }

   @Override
   public boolean update(BlockEntity tile) {
      boolean changedData = super.update(tile);
      if (tile instanceof TileEntityQuantumEntangloporter entangloporter) {
         this.activeQEs.put(entangloporter.getTileCoord(), entangloporter);
      } else {
         this.activeQEs.remove(new Coord4D(tile));
      }

      return changedData;
   }

   @Override
   public boolean onDeactivate(BlockEntity tile) {
      boolean changedData = super.onDeactivate(tile);
      this.activeQEs.remove(new Coord4D(tile));
      return changedData;
   }

   public void handleEject(long gameTime) {
      if (this.isValid() && !this.activeQEs.isEmpty() && this.lastEject != gameTime) {
         this.lastEject = gameTime;
         Map<TransmissionType, BiConsumer<BlockEntity, Direction>> typesToEject = new EnumMap<>(TransmissionType.class);
         List<Runnable> transferHandlers = new ArrayList<>(EnumUtils.TRANSMISSION_TYPES.length - 2);
         int expected = 6 * this.activeQEs.size();
         this.addEnergyTransferHandler(typesToEject, transferHandlers, expected);
         this.addFluidTransferHandler(typesToEject, transferHandlers, expected);
         this.addChemicalTransferHandler(TransmissionType.GAS, this.storedGas, typesToEject, transferHandlers, expected);
         this.addChemicalTransferHandler(TransmissionType.INFUSION, this.storedInfusion, typesToEject, transferHandlers, expected);
         this.addChemicalTransferHandler(TransmissionType.PIGMENT, this.storedPigment, typesToEject, transferHandlers, expected);
         this.addChemicalTransferHandler(TransmissionType.SLURRY, this.storedSlurry, typesToEject, transferHandlers, expected);
         if (!typesToEject.isEmpty()) {
            for (TileEntityQuantumEntangloporter qe : this.activeQEs.values()) {
               if (MekanismUtils.canFunction(qe)) {
                  Map<Direction, BlockEntity> adjacentTiles = null;

                  for (Entry<TransmissionType, BiConsumer<BlockEntity, Direction>> entry : typesToEject.entrySet()) {
                     TransmissionType transmissionType = entry.getKey();
                     ConfigInfo config = qe.getConfig().getConfig(transmissionType);
                     if (config != null && qe.getEjector().isEjecting(config, transmissionType)) {
                        Set<Direction> outputSides = config.getAllOutputtingSides();
                        if (!outputSides.isEmpty()) {
                           if (adjacentTiles == null) {
                              adjacentTiles = new EnumMap<>(Direction.class);
                           }

                           for (Direction side : outputSides) {
                              BlockEntity tile;
                              if (adjacentTiles.containsKey(side)) {
                                 tile = adjacentTiles.get(side);
                              } else {
                                 tile = WorldUtils.getTileEntity(qe.m_58904_(), qe.m_58899_().m_121945_(side));
                                 adjacentTiles.put(side, tile);
                              }

                              if (tile != null) {
                                 entry.getValue().accept(tile, side);
                              }
                           }
                        }
                     }
                  }
               }
            }

            for (Runnable transferHandler : transferHandlers) {
               transferHandler.run();
            }
         }
      }
   }

   private void addEnergyTransferHandler(Map<TransmissionType, BiConsumer<BlockEntity, Direction>> typesToEject, List<Runnable> transferHandlers, int expected) {
      FloatingLong toSend = this.storedEnergy.extract(this.storedEnergy.getMaxEnergy(), Action.SIMULATE, AutomationType.INTERNAL);
      if (!toSend.isZero()) {
         EnergyAcceptorTarget target = new EnergyAcceptorTarget(expected);
         typesToEject.put(
            TransmissionType.ENERGY, (tile, side) -> EnergyCompatUtils.getLazyStrictEnergyHandler(tile, side.m_122424_()).ifPresent(target::addHandler)
         );
         transferHandlers.add(() -> {
            if (target.getHandlerCount() > 0) {
               this.storedEnergy.extract(EmitUtils.sendToAcceptors(target, toSend), Action.EXECUTE, AutomationType.INTERNAL);
            }
         });
      }
   }

   private void addFluidTransferHandler(Map<TransmissionType, BiConsumer<BlockEntity, Direction>> typesToEject, List<Runnable> transferHandlers, int expected) {
      FluidStack fluidToSend = this.storedFluid.extract(this.storedFluid.getCapacity(), Action.SIMULATE, AutomationType.INTERNAL);
      if (!fluidToSend.isEmpty()) {
         FluidHandlerTarget target = new FluidHandlerTarget(fluidToSend, expected);
         typesToEject.put(
            TransmissionType.FLUID,
            (tile, side) -> CapabilityUtils.getCapability(tile, ForgeCapabilities.FLUID_HANDLER, side.m_122424_()).ifPresent(handler -> {
               if (FluidUtils.canFill(handler, fluidToSend)) {
                  target.addHandler(handler);
               }
            })
         );
         transferHandlers.add(() -> {
            if (target.getHandlerCount() > 0) {
               this.storedFluid.extract(EmitUtils.sendToAcceptors(target, fluidToSend.getAmount(), fluidToSend), Action.EXECUTE, AutomationType.INTERNAL);
            }
         });
      }
   }

   private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> void addChemicalTransferHandler(
      TransmissionType chemicalType,
      IChemicalTank<CHEMICAL, STACK> tank,
      Map<TransmissionType, BiConsumer<BlockEntity, Direction>> typesToEject,
      List<Runnable> transferHandlers,
      int expected
   ) {
      STACK toSend = tank.extract(tank.getCapacity(), Action.SIMULATE, AutomationType.INTERNAL);
      if (!toSend.isEmpty()) {
         Capability<IChemicalHandler<CHEMICAL, STACK>> capability = ChemicalUtil.getCapabilityForChemical(toSend);
         ChemicalHandlerTarget<CHEMICAL, STACK, IChemicalHandler<CHEMICAL, STACK>> target = new ChemicalHandlerTarget<>(toSend, expected);
         typesToEject.put(chemicalType, (tile, side) -> CapabilityUtils.getCapability(tile, capability, side.m_122424_()).ifPresent(handler -> {
            if (ChemicalUtil.canInsert(handler, toSend)) {
               target.addHandler(handler);
            }
         }));
         transferHandlers.add(() -> {
            if (target.getHandlerCount() > 0) {
               tank.extract(EmitUtils.sendToAcceptors(target, toSend.getAmount(), toSend), Action.EXECUTE, AutomationType.INTERNAL);
            }
         });
      }
   }
}
