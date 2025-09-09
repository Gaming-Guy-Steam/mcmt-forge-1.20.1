package mekanism.common.tile.machine;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.heat.HeatAPI;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.energy.ResistiveHeaterEnergyContainer;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.capabilities.heat.CachedAmbientTemperature;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.heat.HeatCapacitorHelper;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.container.sync.SyncableFloatingLong;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TileEntityResistiveHeater extends TileEntityMekanism {
   private float soundScale = 1.0F;
   private double lastEnvironmentLoss;
   private double lastTransferLoss;
   private FloatingLong clientEnergyUsed = FloatingLong.ZERO;
   private ResistiveHeaterEnergyContainer energyContainer;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerHeatCapacitorWrapper.class,
      methodNames = {"getTemperature"},
      docPlaceholder = "heater"
   )
   BasicHeatCapacitor heatCapacitor;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getEnergyItem"},
      docPlaceholder = "energy slot"
   )
   EnergyInventorySlot energySlot;

   public TileEntityResistiveHeater(BlockPos pos, BlockState state) {
      super(MekanismBlocks.RESISTIVE_HEATER, pos, state);
      this.addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIG_CARD, this));
   }

   @NotNull
   @Override
   protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
      EnergyContainerHelper builder = EnergyContainerHelper.forSide(this::getDirection);
      builder.addContainer(this.energyContainer = ResistiveHeaterEnergyContainer.input(this, listener), RelativeSide.LEFT, RelativeSide.RIGHT);
      return builder.build();
   }

   @NotNull
   @Override
   protected IHeatCapacitorHolder getInitialHeatCapacitors(IContentsListener listener, CachedAmbientTemperature ambientTemperature) {
      HeatCapacitorHelper builder = HeatCapacitorHelper.forSide(this::getDirection);
      builder.addCapacitor(this.heatCapacitor = BasicHeatCapacitor.create(100.0, 5.0, 100.0, ambientTemperature, listener));
      return builder.build();
   }

   @NotNull
   @Override
   protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
      InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
      builder.addSlot(this.energySlot = EnergyInventorySlot.fillOrConvert(this.energyContainer, this::m_58904_, listener, 15, 35));
      return builder.build();
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      this.energySlot.fillContainerOrConvert();
      FloatingLong toUse = FloatingLong.ZERO;
      if (MekanismUtils.canFunction(this)) {
         toUse = this.energyContainer.extract(this.energyContainer.getEnergyPerTick(), Action.SIMULATE, AutomationType.INTERNAL);
         if (!toUse.isZero()) {
            this.heatCapacitor.handleHeat(toUse.multiply(MekanismConfig.general.resistiveHeaterEfficiency.get()).doubleValue());
            this.energyContainer.extract(toUse, Action.EXECUTE, AutomationType.INTERNAL);
         }
      }

      this.setActive(!toUse.isZero());
      this.clientEnergyUsed = toUse;
      HeatAPI.HeatTransfer transfer = this.simulate();
      this.lastEnvironmentLoss = transfer.environmentTransfer();
      this.lastTransferLoss = transfer.adjacentTransfer();
      float newSoundScale = toUse.divide(100000L).floatValue();
      if (Math.abs(newSoundScale - this.soundScale) > 0.01) {
         this.soundScale = newSoundScale;
         this.sendUpdatePacket();
      }
   }

   @NotNull
   @ComputerMethod
   public FloatingLong getEnergyUsed() {
      return this.clientEnergyUsed;
   }

   @ComputerMethod(
      nameOverride = "getTransferLoss"
   )
   public double getLastTransferLoss() {
      return this.lastTransferLoss;
   }

   @ComputerMethod(
      nameOverride = "getEnvironmentalLoss"
   )
   public double getLastEnvironmentLoss() {
      return this.lastEnvironmentLoss;
   }

   public void setEnergyUsageFromPacket(FloatingLong floatingLong) {
      this.energyContainer.updateEnergyUsage(floatingLong);
      this.markForSave();
   }

   @Override
   public float getVolume() {
      return (float)Math.sqrt(this.soundScale);
   }

   public MachineEnergyContainer<TileEntityResistiveHeater> getEnergyContainer() {
      return this.energyContainer;
   }

   @Override
   public CompoundTag getConfigurationData(Player player) {
      CompoundTag data = super.getConfigurationData(player);
      data.m_128359_("energyUsage", this.energyContainer.getEnergyPerTick().toString());
      return data;
   }

   @Override
   public void setConfigurationData(Player player, CompoundTag data) {
      super.setConfigurationData(player, data);
      NBTUtils.setFloatingLongIfPresent(data, "energyUsage", this.energyContainer::updateEnergyUsage);
   }

   @Override
   public void addContainerTrackers(MekanismContainer container) {
      super.addContainerTrackers(container);
      container.track(SyncableDouble.create(this::getLastTransferLoss, value -> this.lastTransferLoss = value));
      container.track(SyncableDouble.create(this::getLastEnvironmentLoss, value -> this.lastEnvironmentLoss = value));
      container.track(SyncableFloatingLong.create(this::getEnergyUsed, value -> this.clientEnergyUsed = value));
   }

   @NotNull
   @Override
   public CompoundTag getReducedUpdateTag() {
      CompoundTag updateTag = super.getReducedUpdateTag();
      updateTag.m_128350_("soundScale", this.soundScale);
      return updateTag;
   }

   @Override
   public void handleUpdateTag(@NotNull CompoundTag tag) {
      super.handleUpdateTag(tag);
      NBTUtils.setFloatIfPresent(tag, "soundScale", value -> this.soundScale = value);
   }

   @ComputerMethod(
      methodDescription = "Get the energy used in the last tick by the machine"
   )
   FloatingLong getEnergyUsage() {
      return this.energyContainer.getEnergyPerTick();
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   void setEnergyUsage(FloatingLong usage) throws ComputerException {
      this.validateSecurityIsPublic();
      this.setEnergyUsageFromPacket(usage);
   }
}
