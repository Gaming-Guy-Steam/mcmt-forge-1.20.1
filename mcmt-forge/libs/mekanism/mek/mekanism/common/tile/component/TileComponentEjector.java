package mekanism.common.tile.component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.RelativeSide;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.api.text.EnumColor;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.ISyncableData;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.lib.inventory.TileTransitRequest;
import mekanism.common.lib.inventory.TransitRequest;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.ChemicalSlotInfo;
import mekanism.common.tile.component.config.slot.EnergySlotInfo;
import mekanism.common.tile.component.config.slot.FluidSlotInfo;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporterBase;
import mekanism.common.util.CableUtils;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.FluidUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileComponentEjector implements ITileComponent, MekanismContainer.ISpecificContainerTracker {
   private final TileEntityMekanism tile;
   private final Map<TransmissionType, ConfigInfo> configInfo = new EnumMap<>(TransmissionType.class);
   private final EnumColor[] inputColors = new EnumColor[6];
   private final LongSupplier chemicalEjectRate;
   private final IntSupplier fluidEjectRate;
   @Nullable
   private final FloatingLongSupplier energyEjectRate;
   @Nullable
   private Predicate<TransmissionType> canEject;
   @Nullable
   private Predicate<IChemicalTank<?, ?>> canTankEject;
   private boolean strictInput;
   private EnumColor outputColor;
   private int tickDelay = 0;

   public TileComponentEjector(TileEntityMekanism tile) {
      this(tile, MekanismConfig.general.chemicalAutoEjectRate);
   }

   public TileComponentEjector(TileEntityMekanism tile, LongSupplier chemicalEjectRate) {
      this(tile, chemicalEjectRate, MekanismConfig.general.fluidAutoEjectRate);
   }

   public TileComponentEjector(TileEntityMekanism tile, LongSupplier chemicalEjectRate, IntSupplier fluidEjectRate) {
      this(tile, chemicalEjectRate, fluidEjectRate, null);
   }

   public TileComponentEjector(TileEntityMekanism tile, FloatingLongSupplier energyEjectRate) {
      this(tile, MekanismConfig.general.chemicalAutoEjectRate, MekanismConfig.general.fluidAutoEjectRate, energyEjectRate);
   }

   public TileComponentEjector(
      TileEntityMekanism tile, LongSupplier chemicalEjectRate, IntSupplier fluidEjectRate, @Nullable FloatingLongSupplier energyEjectRate
   ) {
      this.tile = tile;
      this.chemicalEjectRate = chemicalEjectRate;
      this.fluidEjectRate = fluidEjectRate;
      this.energyEjectRate = energyEjectRate;
      tile.addComponent(this);
   }

   public TileComponentEjector setOutputData(TileComponentConfig config, TransmissionType... types) {
      for (TransmissionType type : types) {
         ConfigInfo info = config.getConfig(type);
         if (info != null) {
            this.configInfo.put(type, info);
         }
      }

      return this;
   }

   public TileComponentEjector setCanEject(Predicate<TransmissionType> canEject) {
      this.canEject = canEject;
      return this;
   }

   public TileComponentEjector setCanTankEject(Predicate<IChemicalTank<?, ?>> canTankEject) {
      this.canTankEject = canTankEject;
      return this;
   }

   public boolean isEjecting(ConfigInfo info, TransmissionType type) {
      return info.isEjecting() && (this.canEject == null || this.canEject.test(type));
   }

   public void tickServer() {
      for (Entry<TransmissionType, ConfigInfo> entry : this.configInfo.entrySet()) {
         TransmissionType type = entry.getKey();
         ConfigInfo info = entry.getValue();
         if (this.isEjecting(info, type)) {
            if (type == TransmissionType.ITEM) {
               if (this.tickDelay == 0) {
                  this.outputItems(info);
               } else {
                  this.tickDelay--;
               }
            } else if (type != TransmissionType.HEAT) {
               this.eject(type, info);
            }
         }
      }
   }

   private void eject(TransmissionType type, ConfigInfo info) {
      Map<Object, Set<Direction>> outputData = null;

      for (DataType dataType : info.getSupportedDataTypes()) {
         if (dataType.canOutput()) {
            ISlotInfo slotInfo = info.getSlotInfo(dataType);
            if (slotInfo != null) {
               Set<Direction> outputSides = info.getSidesForData(dataType);
               if (!outputSides.isEmpty()) {
                  if (outputData == null) {
                     outputData = new HashMap<>();
                  }

                  if (type.isChemical() && slotInfo instanceof ChemicalSlotInfo<?, ?, ?> chemicalSlotInfo) {
                     for (IChemicalTank<?, ?> tank : chemicalSlotInfo.getTanks()) {
                        if (!tank.isEmpty() && (this.canTankEject == null || this.canTankEject.test(tank))) {
                           outputData.computeIfAbsent(tank, t -> EnumSet.noneOf(Direction.class)).addAll(outputSides);
                        }
                     }
                  } else if (type == TransmissionType.FLUID && slotInfo instanceof FluidSlotInfo fluidSlotInfo) {
                     for (IExtendedFluidTank tankx : fluidSlotInfo.getTanks()) {
                        if (!tankx.isEmpty()) {
                           outputData.computeIfAbsent(tankx, t -> EnumSet.noneOf(Direction.class)).addAll(outputSides);
                        }
                     }
                  } else if (type == TransmissionType.ENERGY && slotInfo instanceof EnergySlotInfo energySlotInfo) {
                     for (IEnergyContainer container : energySlotInfo.getContainers()) {
                        if (!container.isEmpty()) {
                           outputData.computeIfAbsent(container, t -> EnumSet.noneOf(Direction.class)).addAll(outputSides);
                        }
                     }
                  }
               }
            }
         }
      }

      if (outputData != null && !outputData.isEmpty()) {
         for (Entry<Object, Set<Direction>> entry : outputData.entrySet()) {
            if (type.isChemical()) {
               ChemicalUtil.emit(entry.getValue(), (IChemicalTank<?, ?>)entry.getKey(), this.tile, this.chemicalEjectRate.getAsLong());
            } else if (type == TransmissionType.FLUID) {
               FluidUtils.emit(entry.getValue(), (IExtendedFluidTank)entry.getKey(), this.tile, this.fluidEjectRate.getAsInt());
            } else if (type == TransmissionType.ENERGY) {
               IEnergyContainer containerx = (IEnergyContainer)entry.getKey();
               CableUtils.emit(entry.getValue(), containerx, this.tile, this.energyEjectRate == null ? containerx.getMaxEnergy() : this.energyEjectRate.get());
            }
         }
      }
   }

   private void outputItems(ConfigInfo info) {
      for (DataType dataType : info.getSupportedDataTypes()) {
         if (dataType.canOutput() && info.getSlotInfo(dataType) instanceof InventorySlotInfo inventorySlotInfo) {
            Set<Direction> outputs = info.getSidesForData(dataType);
            if (!outputs.isEmpty()) {
               TileComponentEjector.EjectTransitRequest ejectMap = InventoryUtils.getEjectItemMap(
                  new TileComponentEjector.EjectTransitRequest(this.tile, outputs.iterator().next()), inventorySlotInfo.getSlots()
               );
               if (!ejectMap.isEmpty()) {
                  for (Direction side : outputs) {
                     BlockEntity target = WorldUtils.getTileEntity(this.tile.m_58904_(), this.tile.m_58899_().m_121945_(side));
                     if (target != null) {
                        ejectMap.side = side;
                        TransitRequest.TransitResponse response;
                        if (target instanceof TileEntityLogisticalTransporterBase transporter) {
                           response = transporter.getTransmitter().insert(this.tile, ejectMap, this.outputColor, true, 0);
                        } else {
                           response = ejectMap.addToInventory(target, side, 0, false);
                        }

                        if (!response.isEmpty()) {
                           response.useAll();
                           if (ejectMap.isEmpty()) {
                              break;
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      this.tickDelay = 10;
   }

   @ComputerMethod
   public boolean hasStrictInput() {
      return this.strictInput;
   }

   public void setStrictInput(boolean strict) {
      if (this.strictInput != strict) {
         this.strictInput = strict;
         this.tile.markForSave();
      }
   }

   @ComputerMethod
   public EnumColor getOutputColor() {
      return this.outputColor;
   }

   public void setOutputColor(EnumColor color) {
      if (this.outputColor != color) {
         this.outputColor = color;
         this.tile.markForSave();
      }
   }

   public boolean isInputSideEnabled(@NotNull RelativeSide side) {
      ConfigInfo info = this.configInfo.get(TransmissionType.ITEM);
      return info == null || info.isSideEnabled(side);
   }

   public void setInputColor(RelativeSide side, EnumColor color) {
      if (this.isInputSideEnabled(side)) {
         int ordinal = side.ordinal();
         if (this.inputColors[ordinal] != color) {
            this.inputColors[ordinal] = color;
            this.tile.markForSave();
         }
      }
   }

   @ComputerMethod
   public EnumColor getInputColor(RelativeSide side) {
      return this.inputColors[side.ordinal()];
   }

   @Override
   public void read(CompoundTag nbtTags) {
      NBTUtils.setCompoundIfPresent(nbtTags, "componentEjector", ejectorNBT -> {
         this.strictInput = ejectorNBT.m_128471_("strictInput");
         NBTUtils.setEnumIfPresent(ejectorNBT, "color", TransporterUtils::readColor, color -> this.outputColor = color);

         for (int i = 0; i < EnumUtils.DIRECTIONS.length; i++) {
            int index = i;
            NBTUtils.setEnumIfPresent(ejectorNBT, "color" + index, TransporterUtils::readColor, color -> this.inputColors[index] = color);
         }
      });
   }

   @Override
   public void write(CompoundTag nbtTags) {
      CompoundTag ejectorNBT = new CompoundTag();
      ejectorNBT.m_128379_("strictInput", this.strictInput);
      if (this.outputColor != null) {
         ejectorNBT.m_128405_("color", TransporterUtils.getColorIndex(this.outputColor));
      }

      for (int i = 0; i < EnumUtils.DIRECTIONS.length; i++) {
         ejectorNBT.m_128405_("color" + i, TransporterUtils.getColorIndex(this.inputColors[i]));
      }

      nbtTags.m_128365_("componentEjector", ejectorNBT);
   }

   @Override
   public List<ISyncableData> getSpecificSyncableData() {
      List<ISyncableData> list = new ArrayList<>();
      list.add(SyncableBoolean.create(this::hasStrictInput, input -> this.strictInput = input));
      list.add(SyncableInt.create(() -> TransporterUtils.getColorIndex(this.outputColor), index -> this.outputColor = TransporterUtils.readColor(index)));

      for (int i = 0; i < EnumUtils.DIRECTIONS.length; i++) {
         int idx = i;
         list.add(
            SyncableInt.create(() -> TransporterUtils.getColorIndex(this.inputColors[idx]), index -> this.inputColors[idx] = TransporterUtils.readColor(index))
         );
      }

      return list;
   }

   @ComputerMethod(
      nameOverride = "setStrictInput",
      requiresPublicSecurity = true
   )
   void computerSetStrictInput(boolean strict) throws ComputerException {
      this.tile.validateSecurityIsPublic();
      this.setStrictInput(strict);
   }

   private void validateInputSide(RelativeSide side) throws ComputerException {
      if (!this.isInputSideEnabled(side)) {
         throw new ComputerException("Side '%s' is disabled and can't be configured.", side);
      }
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   void clearInputColor(RelativeSide side) throws ComputerException {
      this.tile.validateSecurityIsPublic();
      this.validateInputSide(side);
      this.setInputColor(side, null);
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   void incrementInputColor(RelativeSide side) throws ComputerException {
      this.tile.validateSecurityIsPublic();
      this.validateInputSide(side);
      int ordinal = side.ordinal();
      this.inputColors[ordinal] = TransporterUtils.increment(this.inputColors[ordinal]);
      this.tile.markForSave();
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   void decrementInputColor(RelativeSide side) throws ComputerException {
      this.tile.validateSecurityIsPublic();
      this.validateInputSide(side);
      int ordinal = side.ordinal();
      this.inputColors[ordinal] = TransporterUtils.decrement(this.inputColors[ordinal]);
      this.tile.markForSave();
   }

   @ComputerMethod(
      nameOverride = "setInputColor",
      requiresPublicSecurity = true
   )
   void computerSetInputColor(RelativeSide side, EnumColor color) throws ComputerException {
      this.tile.validateSecurityIsPublic();
      this.validateInputSide(side);
      if (!TransporterUtils.colors.contains(color)) {
         throw new ComputerException("Color '%s' is not a supported transporter color.", color);
      } else {
         this.setInputColor(side, color);
      }
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   void clearOutputColor() throws ComputerException {
      this.tile.validateSecurityIsPublic();
      this.setOutputColor(null);
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   void incrementOutputColor() throws ComputerException {
      this.tile.validateSecurityIsPublic();
      this.outputColor = TransporterUtils.increment(this.outputColor);
      this.tile.markForSave();
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   void decrementOutputColor() throws ComputerException {
      this.tile.validateSecurityIsPublic();
      this.outputColor = TransporterUtils.decrement(this.outputColor);
      this.tile.markForSave();
   }

   @ComputerMethod(
      nameOverride = "setOutputColor",
      requiresPublicSecurity = true
   )
   void computerSetOutputColor(EnumColor color) throws ComputerException {
      this.tile.validateSecurityIsPublic();
      if (!TransporterUtils.colors.contains(color)) {
         throw new ComputerException("Color '%s' is not a supported transporter color.", color);
      } else {
         this.setOutputColor(color);
      }
   }

   private static class EjectTransitRequest extends TileTransitRequest {
      public Direction side;

      public EjectTransitRequest(BlockEntity tile, Direction side) {
         super(tile, side);
         this.side = side;
      }

      @Override
      public Direction getSide() {
         return this.side;
      }
   }
}
