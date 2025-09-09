package mekanism.common.tile.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;
import mekanism.api.RelativeSide;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.ISyncableData;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.BaseSlotInfo;
import mekanism.common.tile.component.config.slot.ChemicalSlotInfo;
import mekanism.common.tile.component.config.slot.EnergySlotInfo;
import mekanism.common.tile.component.config.slot.FluidSlotInfo;
import mekanism.common.tile.component.config.slot.HeatSlotInfo;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileComponentConfig implements ITileComponent, MekanismContainer.ISpecificContainerTracker {
   public final TileEntityMekanism tile;
   private final Map<TransmissionType, ConfigInfo> configInfo = new EnumMap<>(TransmissionType.class);
   private final Map<TransmissionType, List<Consumer<Direction>>> configChangeListeners = new EnumMap<>(TransmissionType.class);
   private final List<TransmissionType> transmissionTypes = new ArrayList<>();

   public TileComponentConfig(TileEntityMekanism tile, TransmissionType... types) {
      this.tile = tile;

      for (TransmissionType type : types) {
         this.addSupported(type);
      }

      tile.addComponent(this);
   }

   public void addConfigChangeListener(TransmissionType transmissionType, Consumer<Direction> listener) {
      this.configChangeListeners.computeIfAbsent(transmissionType, type -> new ArrayList<>(1)).add(listener);
   }

   public void sideChanged(TransmissionType transmissionType, RelativeSide side) {
      Direction direction = side.getDirection(this.tile.getDirection());
      this.sideChangedBasic(transmissionType, direction);
      this.tile.sendUpdatePacket();
      WorldUtils.notifyNeighborOfChange(this.tile.m_58904_(), direction, this.tile.m_58899_());
   }

   private void sideChangedBasic(TransmissionType transmissionType, Direction direction) {
      switch (transmissionType) {
         case ENERGY:
            this.tile.invalidateCapabilities(EnergyCompatUtils.getEnabledEnergyCapabilities(), direction);
            break;
         case FLUID:
            this.tile.invalidateCapability(ForgeCapabilities.FLUID_HANDLER, direction);
            break;
         case GAS:
            this.tile.invalidateCapability(Capabilities.GAS_HANDLER, direction);
            break;
         case INFUSION:
            this.tile.invalidateCapability(Capabilities.INFUSION_HANDLER, direction);
            break;
         case PIGMENT:
            this.tile.invalidateCapability(Capabilities.PIGMENT_HANDLER, direction);
            break;
         case SLURRY:
            this.tile.invalidateCapability(Capabilities.SLURRY_HANDLER, direction);
            break;
         case ITEM:
            this.tile.invalidateCapability(ForgeCapabilities.ITEM_HANDLER, direction);
            break;
         case HEAT:
            this.tile.invalidateCapability(Capabilities.HEAT_HANDLER, direction);
      }

      this.tile.markForSave();

      for (Consumer<Direction> listener : this.configChangeListeners.getOrDefault(transmissionType, Collections.emptyList())) {
         listener.accept(direction);
      }
   }

   private RelativeSide getSide(Direction direction) {
      return RelativeSide.fromDirections(this.tile.getDirection(), direction);
   }

   @ComputerMethod(
      nameOverride = "getConfigurableTypes"
   )
   public List<TransmissionType> getTransmissions() {
      return this.transmissionTypes;
   }

   public void addSupported(TransmissionType type) {
      if (!this.configInfo.containsKey(type)) {
         this.configInfo.put(type, new ConfigInfo(this.tile::getDirection));
         this.transmissionTypes.add(type);
      }
   }

   public boolean isCapabilityDisabled(@NotNull Capability<?> capability, Direction side) {
      TransmissionType type = null;
      if (capability == ForgeCapabilities.ITEM_HANDLER) {
         type = TransmissionType.ITEM;
      } else if (capability == Capabilities.GAS_HANDLER) {
         type = TransmissionType.GAS;
      } else if (capability == Capabilities.INFUSION_HANDLER) {
         type = TransmissionType.INFUSION;
      } else if (capability == Capabilities.PIGMENT_HANDLER) {
         type = TransmissionType.PIGMENT;
      } else if (capability == Capabilities.SLURRY_HANDLER) {
         type = TransmissionType.SLURRY;
      } else if (capability == Capabilities.HEAT_HANDLER) {
         type = TransmissionType.HEAT;
      } else if (capability == ForgeCapabilities.FLUID_HANDLER) {
         type = TransmissionType.FLUID;
      } else if (EnergyCompatUtils.isEnergyCapability(capability)) {
         type = TransmissionType.ENERGY;
      }

      if (type != null) {
         ConfigInfo info = this.getConfig(type);
         if (info != null && side != null) {
            ISlotInfo slotInfo = info.getSlotInfo(this.getSide(side));
            return slotInfo == null || !slotInfo.isEnabled();
         }
      }

      return false;
   }

   @Nullable
   public ConfigInfo getConfig(TransmissionType type) {
      return this.configInfo.get(type);
   }

   public void addDisabledSides(@NotNull RelativeSide... sides) {
      for (ConfigInfo config : this.configInfo.values()) {
         config.addDisabledSides(sides);
      }
   }

   public ConfigInfo setupInputConfig(TransmissionType type, Object container) {
      ConfigInfo config = this.getConfig(type);
      if (config != null) {
         config.addSlotInfo(DataType.INPUT, createInfo(type, true, false, container));
         config.fill(DataType.INPUT);
         config.setCanEject(false);
      }

      return config;
   }

   public ConfigInfo setupOutputConfig(TransmissionType type, Object container, RelativeSide... sides) {
      ConfigInfo config = this.getConfig(type);
      if (config != null) {
         config.addSlotInfo(DataType.OUTPUT, createInfo(type, false, true, container));
         config.setDataType(DataType.OUTPUT, sides);
         config.setEjecting(true);
      }

      return config;
   }

   public ConfigInfo setupIOConfig(TransmissionType type, Object inputInfo, Object outputInfo, RelativeSide outputSide) {
      return this.setupIOConfig(type, inputInfo, outputInfo, outputSide, false);
   }

   public ConfigInfo setupIOConfig(TransmissionType type, Object inputContainer, Object outputContainer, RelativeSide outputSide, boolean alwaysAllow) {
      return this.setupIOConfig(type, inputContainer, outputContainer, outputSide, alwaysAllow, alwaysAllow);
   }

   public ConfigInfo setupIOConfig(
      TransmissionType type, Object inputContainer, Object outputContainer, RelativeSide outputSide, boolean alwaysAllowInput, boolean alwaysAllowOutput
   ) {
      ConfigInfo config = this.getConfig(type);
      if (config != null) {
         config.addSlotInfo(DataType.INPUT, createInfo(type, true, alwaysAllowOutput, inputContainer));
         config.addSlotInfo(DataType.OUTPUT, createInfo(type, alwaysAllowInput, true, outputContainer));
         config.addSlotInfo(DataType.INPUT_OUTPUT, createInfo(type, true, true, List.of(inputContainer, outputContainer)));
         config.fill(DataType.INPUT);
         config.setDataType(DataType.OUTPUT, outputSide);
      }

      return config;
   }

   public ConfigInfo setupIOConfig(TransmissionType type, Object info, RelativeSide outputSide) {
      return this.setupIOConfig(type, info, outputSide, false);
   }

   public ConfigInfo setupIOConfig(TransmissionType type, Object info, RelativeSide outputSide, boolean alwaysAllow) {
      ConfigInfo config = this.getConfig(type);
      if (config != null) {
         config.addSlotInfo(DataType.INPUT, createInfo(type, true, alwaysAllow, info));
         config.addSlotInfo(DataType.OUTPUT, createInfo(type, alwaysAllow, true, info));
         config.addSlotInfo(DataType.INPUT_OUTPUT, createInfo(type, true, true, info));
         config.fill(DataType.INPUT);
         config.setDataType(DataType.OUTPUT, outputSide);
      }

      return config;
   }

   public ConfigInfo setupItemIOConfig(IInventorySlot inputSlot, IInventorySlot outputSlot, IInventorySlot energySlot) {
      return this.setupItemIOConfig(Collections.singletonList(inputSlot), Collections.singletonList(outputSlot), energySlot, false);
   }

   public ConfigInfo setupItemIOConfig(List<IInventorySlot> inputSlots, List<IInventorySlot> outputSlots, IInventorySlot energySlot, boolean alwaysAllow) {
      ConfigInfo itemConfig = this.getConfig(TransmissionType.ITEM);
      if (itemConfig != null) {
         itemConfig.addSlotInfo(DataType.INPUT, new InventorySlotInfo(true, alwaysAllow, inputSlots));
         itemConfig.addSlotInfo(DataType.OUTPUT, new InventorySlotInfo(alwaysAllow, true, outputSlots));
         List<IInventorySlot> ioSlots = new ArrayList<>(inputSlots);
         ioSlots.addAll(outputSlots);
         itemConfig.addSlotInfo(DataType.INPUT_OUTPUT, new InventorySlotInfo(true, true, ioSlots));
         itemConfig.addSlotInfo(DataType.ENERGY, new InventorySlotInfo(true, true, energySlot));
         itemConfig.setDefaults();
      }

      return itemConfig;
   }

   public ConfigInfo setupItemIOExtraConfig(IInventorySlot inputSlot, IInventorySlot outputSlot, IInventorySlot extraSlot, IInventorySlot energySlot) {
      ConfigInfo itemConfig = this.getConfig(TransmissionType.ITEM);
      if (itemConfig != null) {
         itemConfig.addSlotInfo(DataType.INPUT, new InventorySlotInfo(true, false, inputSlot));
         itemConfig.addSlotInfo(DataType.OUTPUT, new InventorySlotInfo(false, true, outputSlot));
         itemConfig.addSlotInfo(DataType.INPUT_OUTPUT, new InventorySlotInfo(true, true, inputSlot, outputSlot));
         itemConfig.addSlotInfo(DataType.EXTRA, new InventorySlotInfo(true, true, extraSlot));
         itemConfig.addSlotInfo(DataType.ENERGY, new InventorySlotInfo(true, true, energySlot));
         itemConfig.setDefaults();
      }

      return itemConfig;
   }

   @Nullable
   public DataType getDataType(TransmissionType type, RelativeSide side) {
      ConfigInfo info = this.getConfig(type);
      return info == null ? null : info.getDataType(side);
   }

   @Nullable
   public ISlotInfo getSlotInfo(TransmissionType type, Direction direction) {
      if (direction == null) {
         return null;
      } else {
         ConfigInfo info = this.getConfig(type);
         return info == null ? null : info.getSlotInfo(this.getSide(direction));
      }
   }

   public boolean supports(TransmissionType type) {
      return this.configInfo.containsKey(type);
   }

   @Override
   public void read(CompoundTag nbtTags) {
      NBTUtils.setCompoundIfPresent(nbtTags, "componentConfig", configNBT -> {
         Set<Direction> directionsToUpdate = EnumSet.noneOf(Direction.class);
         this.configInfo.forEach((type, info) -> {
            NBTUtils.setBooleanIfPresent(configNBT, "eject" + type.ordinal(), info::setEjecting);
            NBTUtils.setCompoundIfPresent(configNBT, "config" + type.ordinal(), sideConfig -> {
               for (RelativeSide side : EnumUtils.SIDES) {
                  NBTUtils.setEnumIfPresent(sideConfig, "side" + side.ordinal(), DataType::byIndexStatic, dataType -> {
                     if (info.getDataType(side) != dataType) {
                        info.setDataType(dataType, side);
                        if (this.tile.m_58898_()) {
                           Direction direction = side.getDirection(this.tile.getDirection());
                           this.sideChangedBasic(type, direction);
                           directionsToUpdate.add(direction);
                        }
                     }
                  });
               }
            });
         });
         WorldUtils.notifyNeighborsOfChange(this.tile.m_58904_(), this.tile.m_58899_(), directionsToUpdate);
      });
   }

   @Override
   public void write(CompoundTag nbtTags) {
      CompoundTag configNBT = new CompoundTag();

      for (Entry<TransmissionType, ConfigInfo> entry : this.configInfo.entrySet()) {
         TransmissionType type = entry.getKey();
         ConfigInfo info = entry.getValue();
         configNBT.m_128379_("eject" + type.ordinal(), info.isEjecting());
         CompoundTag sideConfig = new CompoundTag();

         for (RelativeSide side : EnumUtils.SIDES) {
            NBTUtils.writeEnum(sideConfig, "side" + side.ordinal(), info.getDataType(side));
         }

         configNBT.m_128365_("config" + type.ordinal(), sideConfig);
      }

      nbtTags.m_128365_("componentConfig", configNBT);
   }

   @Override
   public void addToUpdateTag(CompoundTag updateTag) {
      CompoundTag configNBT = new CompoundTag();

      for (Entry<TransmissionType, ConfigInfo> entry : this.configInfo.entrySet()) {
         TransmissionType type = entry.getKey();
         ConfigInfo info = entry.getValue();
         CompoundTag sideConfig = new CompoundTag();

         for (RelativeSide side : EnumUtils.SIDES) {
            NBTUtils.writeEnum(sideConfig, "side" + side.ordinal(), info.getDataType(side));
         }

         configNBT.m_128365_("config" + type.ordinal(), sideConfig);
      }

      updateTag.m_128365_("componentConfig", configNBT);
   }

   @Override
   public void readFromUpdateTag(CompoundTag updateTag) {
      NBTUtils.setCompoundIfPresent(updateTag, "componentConfig", configNBT -> {
         for (Entry<TransmissionType, ConfigInfo> entry : this.configInfo.entrySet()) {
            TransmissionType type = entry.getKey();
            ConfigInfo info = entry.getValue();
            NBTUtils.setCompoundIfPresent(configNBT, "config" + type.ordinal(), sideConfig -> {
               for (RelativeSide side : EnumUtils.SIDES) {
                  NBTUtils.setEnumIfPresent(sideConfig, "side" + side.ordinal(), DataType::byIndexStatic, dataType -> info.setDataType(dataType, side));
               }
            });
         }
      });
   }

   @Override
   public List<ISyncableData> getSpecificSyncableData() {
      List<ISyncableData> list = new ArrayList<>();

      for (TransmissionType transmission : this.getTransmissions()) {
         ConfigInfo info = this.configInfo.get(transmission);
         list.add(SyncableBoolean.create(info::isEjecting, info::setEjecting));
      }

      return list;
   }

   public static BaseSlotInfo createInfo(TransmissionType type, boolean input, boolean output, Object... containers) {
      return createInfo(type, input, output, List.of(containers));
   }

   public static BaseSlotInfo createInfo(TransmissionType type, boolean input, boolean output, List<?> containers) {
      return (BaseSlotInfo)(switch (type) {
         case ENERGY -> new EnergySlotInfo(input, output, (List<IEnergyContainer>)containers);
         case FLUID -> new FluidSlotInfo(input, output, (List<IExtendedFluidTank>)containers);
         case GAS -> new ChemicalSlotInfo.GasSlotInfo(input, output, (List<IGasTank>)containers);
         case INFUSION -> new ChemicalSlotInfo.InfusionSlotInfo(input, output, (List<IInfusionTank>)containers);
         case PIGMENT -> new ChemicalSlotInfo.PigmentSlotInfo(input, output, (List<IPigmentTank>)containers);
         case SLURRY -> new ChemicalSlotInfo.SlurrySlotInfo(input, output, (List<ISlurryTank>)containers);
         case ITEM -> new InventorySlotInfo(input, output, (List<IInventorySlot>)containers);
         case HEAT -> new HeatSlotInfo(input, output, (List<IHeatCapacitor>)containers);
      });
   }

   private void validateSupportedTransmissionType(TransmissionType type) throws ComputerException {
      if (!this.supports(type)) {
         throw new ComputerException("This machine does not support configuring transmission type '%s'.", type);
      }
   }

   @ComputerMethod
   boolean canEject(TransmissionType type) throws ComputerException {
      this.validateSupportedTransmissionType(type);
      return this.configInfo.get(type).canEject();
   }

   @ComputerMethod
   boolean isEjecting(TransmissionType type) throws ComputerException {
      this.validateSupportedTransmissionType(type);
      return this.configInfo.get(type).isEjecting();
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   void setEjecting(TransmissionType type, boolean ejecting) throws ComputerException {
      this.tile.validateSecurityIsPublic();
      this.validateSupportedTransmissionType(type);
      ConfigInfo config = this.configInfo.get(type);
      if (!config.canEject()) {
         throw new ComputerException("This machine does not support auto-ejecting for transmission type '%s'.", type);
      } else {
         if (config.isEjecting() != ejecting) {
            config.setEjecting(ejecting);
            this.tile.markForSave();
         }
      }
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   Set<DataType> getSupportedModes(TransmissionType type) throws ComputerException {
      this.validateSupportedTransmissionType(type);
      return this.configInfo.get(type).getSupportedDataTypes();
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   DataType getMode(TransmissionType type, RelativeSide side) throws ComputerException {
      this.validateSupportedTransmissionType(type);
      return this.configInfo.get(type).getDataType(side);
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   void setMode(TransmissionType type, RelativeSide side, DataType mode) throws ComputerException {
      this.tile.validateSecurityIsPublic();
      this.validateSupportedTransmissionType(type);
      ConfigInfo config = this.configInfo.get(type);
      if (!config.getSupportedDataTypes().contains(mode)) {
         throw new ComputerException("This machine does not support mode '%s' for transmission type '%s'.", mode, type);
      } else {
         DataType currentMode = config.getDataType(side);
         if (mode != currentMode) {
            config.setDataType(mode, side);
            this.sideChanged(type, side);
         }
      }
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   void incrementMode(TransmissionType type, RelativeSide side) throws ComputerException {
      this.tile.validateSecurityIsPublic();
      this.validateSupportedTransmissionType(type);
      ConfigInfo configInfo = this.configInfo.get(type);
      if (configInfo.getDataType(side) != configInfo.incrementDataType(side)) {
         this.sideChanged(type, side);
      }
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   void decrementMode(TransmissionType type, RelativeSide side) throws ComputerException {
      this.tile.validateSecurityIsPublic();
      this.validateSupportedTransmissionType(type);
      ConfigInfo configInfo = this.configInfo.get(type);
      if (configInfo.getDataType(side) != configInfo.decrementDataType(side)) {
         this.sideChanged(type, side);
      }
   }
}
