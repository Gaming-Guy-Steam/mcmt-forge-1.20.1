package mekanism.common.tile;

import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.capabilities.energy.EnergyCubeEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.component.ITileComponent;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import mekanism.common.upgrade.EnergyCubeUpgradeData;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityEnergyCube extends TileEntityConfigurableMachine {
   public static final ModelProperty<TileEntityEnergyCube.CubeSideState[]> SIDE_STATE_PROPERTY = new ModelProperty();
   private EnergyCubeTier tier;
   private float prevScale;
   private EnergyCubeEnergyContainer energyContainer;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getChargeItem"},
      docPlaceholder = "charge slot"
   )
   EnergyInventorySlot chargeSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getDischargeItem"},
      docPlaceholder = "discharge slot"
   )
   EnergyInventorySlot dischargeSlot;

   public TileEntityEnergyCube(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
      super(blockProvider, pos, state);
      this.configComponent = new TileComponentConfig(this, TransmissionType.ENERGY, TransmissionType.ITEM);
      this.configComponent.setupIOConfig(TransmissionType.ITEM, this.chargeSlot, this.dischargeSlot, RelativeSide.FRONT, true).setCanEject(false);
      this.configComponent.setupIOConfig(TransmissionType.ENERGY, this.energyContainer, RelativeSide.FRONT).setEjecting(true);
      this.ejectorComponent = new TileComponentEjector(this, () -> this.tier.getOutput());
      this.ejectorComponent.setOutputData(this.configComponent, TransmissionType.ENERGY).setCanEject(type -> MekanismUtils.canFunction(this));
   }

   @Override
   protected void presetVariables() {
      super.presetVariables();
      this.tier = Attribute.getTier(this.getBlockType(), EnergyCubeTier.class);
   }

   @NotNull
   @Override
   protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
      EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this::getDirection, this::getConfig);
      builder.addContainer(this.energyContainer = EnergyCubeEnergyContainer.create(this.tier, listener));
      return builder.build();
   }

   @NotNull
   @Override
   protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
      InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
      builder.addSlot(this.dischargeSlot = EnergyInventorySlot.fillOrConvert(this.energyContainer, this::m_58904_, listener, 17, 35));
      builder.addSlot(this.chargeSlot = EnergyInventorySlot.drain(this.energyContainer, listener, 143, 35));
      this.dischargeSlot.setSlotOverlay(SlotOverlay.MINUS);
      this.chargeSlot.setSlotOverlay(SlotOverlay.PLUS);
      return builder.build();
   }

   public EnergyCubeTier getTier() {
      return this.tier;
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      this.chargeSlot.drainContainer();
      this.dischargeSlot.fillContainerOrConvert();
      float newScale = MekanismUtils.getScale(this.prevScale, this.energyContainer);
      if (newScale != this.prevScale) {
         this.prevScale = newScale;
         this.sendUpdatePacket();
      }
   }

   @Override
   public int getRedstoneLevel() {
      return MekanismUtils.redstoneLevelFromContents(this.energyContainer.getEnergy(), this.energyContainer.getMaxEnergy());
   }

   @Override
   protected boolean makesComparatorDirty(@Nullable SubstanceType type) {
      return type == SubstanceType.ENERGY;
   }

   @Override
   public void parseUpgradeData(@NotNull IUpgradeData upgradeData) {
      if (upgradeData instanceof EnergyCubeUpgradeData data) {
         this.redstone = data.redstone;
         this.setControlType(data.controlType);
         this.getEnergyContainer().setEnergy(data.energyContainer.getEnergy());
         this.chargeSlot.setStack(data.chargeSlot.getStack());
         this.dischargeSlot.deserializeNBT(data.dischargeSlot.serializeNBT());

         for (ITileComponent component : this.getComponents()) {
            component.read(data.components);
         }
      } else {
         super.parseUpgradeData(upgradeData);
      }
   }

   public EnergyCubeEnergyContainer getEnergyContainer() {
      return this.energyContainer;
   }

   @NotNull
   public EnergyCubeUpgradeData getUpgradeData() {
      return new EnergyCubeUpgradeData(
         this.redstone, this.getControlType(), this.getEnergyContainer(), this.chargeSlot, this.dischargeSlot, this.getComponents()
      );
   }

   public float getEnergyScale() {
      return this.prevScale;
   }

   @NotNull
   @Override
   public CompoundTag getReducedUpdateTag() {
      CompoundTag updateTag = super.getReducedUpdateTag();
      updateTag.m_128350_("scale", this.prevScale);
      return updateTag;
   }

   @Override
   public void handleUpdateTag(@NotNull CompoundTag tag) {
      ConfigInfo config = this.getConfig().getConfig(TransmissionType.ENERGY);
      DataType[] currentConfig = new DataType[EnumUtils.SIDES.length];
      if (config != null) {
         for (RelativeSide side : EnumUtils.SIDES) {
            currentConfig[side.ordinal()] = config.getDataType(side);
         }
      }

      super.handleUpdateTag(tag);
      NBTUtils.setFloatIfPresent(tag, "scale", scale -> this.prevScale = scale);
      if (config != null) {
         for (RelativeSide side : EnumUtils.SIDES) {
            if (currentConfig[side.ordinal()] != config.getDataType(side)) {
               this.updateModelData();
               break;
            }
         }
      }
   }

   @NotNull
   public ModelData getModelData() {
      ConfigInfo config = this.getConfig().getConfig(TransmissionType.ENERGY);
      if (config == null) {
         return super.getModelData();
      } else {
         TileEntityEnergyCube.CubeSideState[] sideStates = new TileEntityEnergyCube.CubeSideState[EnumUtils.SIDES.length];

         for (RelativeSide side : EnumUtils.SIDES) {
            TileEntityEnergyCube.CubeSideState state = TileEntityEnergyCube.CubeSideState.INACTIVE;
            ISlotInfo slotInfo = config.getSlotInfo(side);
            if (slotInfo != null) {
               if (slotInfo.canOutput()) {
                  state = TileEntityEnergyCube.CubeSideState.ACTIVE_LIT;
               } else if (slotInfo.canInput()) {
                  state = TileEntityEnergyCube.CubeSideState.ACTIVE_UNLIT;
               }
            }

            sideStates[side.ordinal()] = state;
         }

         return ModelData.builder().with(SIDE_STATE_PROPERTY, sideStates).build();
      }
   }

   public static enum CubeSideState {
      ACTIVE_LIT,
      ACTIVE_UNLIT,
      INACTIVE;
   }
}
