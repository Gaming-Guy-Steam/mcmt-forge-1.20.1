package mekanism.common.tile.machine;

import mekanism.api.IContentsListener;
import mekanism.api.heat.HeatAPI;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.capabilities.heat.CachedAmbientTemperature;
import mekanism.common.capabilities.holder.heat.HeatCapacitorHelper;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.FuelInventorySlot;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.NotNull;

public class TileEntityFuelwoodHeater extends TileEntityMekanism {
   public int burnTime;
   public int maxBurnTime;
   private double lastEnvironmentLoss;
   private double lastTransferLoss;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getFuelItem"},
      docPlaceholder = "fuel slot"
   )
   FuelInventorySlot fuelSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerHeatCapacitorWrapper.class,
      methodNames = {"getTemperature"},
      docPlaceholder = "heater"
   )
   BasicHeatCapacitor heatCapacitor;

   public TileEntityFuelwoodHeater(BlockPos pos, BlockState state) {
      super(MekanismBlocks.FUELWOOD_HEATER, pos, state);
   }

   @NotNull
   @Override
   protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
      InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
      builder.addSlot(this.fuelSlot = FuelInventorySlot.forFuel(stack -> ForgeHooks.getBurnTime(stack, null), listener, 15, 29));
      return builder.build();
   }

   @NotNull
   @Override
   protected IHeatCapacitorHolder getInitialHeatCapacitors(IContentsListener listener, CachedAmbientTemperature ambientTemperature) {
      HeatCapacitorHelper builder = HeatCapacitorHelper.forSide(this::getDirection);
      builder.addCapacitor(this.heatCapacitor = BasicHeatCapacitor.create(100.0, 5.0, 10.0, ambientTemperature, listener));
      return builder.build();
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      if (this.burnTime == 0) {
         this.maxBurnTime = this.burnTime = this.fuelSlot.burn();
      }

      if (this.burnTime > 0) {
         int ticks = Math.min(this.burnTime, MekanismConfig.general.fuelwoodTickMultiplier.get());
         this.burnTime -= ticks;
         this.heatCapacitor.handleHeat(MekanismConfig.general.heatPerFuelTick.get() * ticks);
         this.setActive(true);
      } else {
         this.setActive(false);
      }

      HeatAPI.HeatTransfer loss = this.simulate();
      this.lastEnvironmentLoss = loss.environmentTransfer();
      this.lastTransferLoss = loss.adjacentTransfer();
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

   @Override
   public void m_142466_(@NotNull CompoundTag nbt) {
      super.m_142466_(nbt);
      this.burnTime = nbt.m_128451_("burnTime");
      this.maxBurnTime = nbt.m_128451_("maxBurnTime");
   }

   @Override
   public void m_183515_(@NotNull CompoundTag nbtTags) {
      super.m_183515_(nbtTags);
      nbtTags.m_128405_("burnTime", this.burnTime);
      nbtTags.m_128405_("maxBurnTime", this.maxBurnTime);
   }

   @Override
   public void addContainerTrackers(MekanismContainer container) {
      super.addContainerTrackers(container);
      container.track(SyncableInt.create(() -> this.burnTime, value -> this.burnTime = value));
      container.track(SyncableInt.create(() -> this.maxBurnTime, value -> this.maxBurnTime = value));
      container.track(SyncableDouble.create(this::getLastTransferLoss, value -> this.lastTransferLoss = value));
      container.track(SyncableDouble.create(this::getLastEnvironmentLoss, value -> this.lastEnvironmentLoss = value));
   }
}
