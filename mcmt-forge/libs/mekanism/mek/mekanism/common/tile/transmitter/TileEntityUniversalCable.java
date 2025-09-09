package mekanism.common.tile.transmitter;

import java.util.Collections;
import java.util.List;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.tier.BaseTier;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.capabilities.energy.DynamicStrictEnergyHandler;
import mekanism.common.capabilities.resolver.manager.EnergyHandlerManager;
import mekanism.common.content.network.EnergyNetwork;
import mekanism.common.content.network.transmitter.UniversalCable;
import mekanism.common.integration.computer.ComputerCapabilityHelper;
import mekanism.common.integration.computer.IComputerTile;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityUniversalCable extends TileEntityTransmitter implements IComputerTile {
   private final EnergyHandlerManager energyHandlerManager;

   public TileEntityUniversalCable(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
      super(blockProvider, pos, state);
      this.addCapabilityResolver(
         this.energyHandlerManager = new EnergyHandlerManager(
            direction -> {
               UniversalCable cable = this.getTransmitter();
               return (direction == null || cable.getConnectionTypeRaw(direction) != ConnectionType.NONE) && !cable.isRedstoneActivated()
                  ? cable.getEnergyContainers(direction)
                  : Collections.emptyList();
            },
            new DynamicStrictEnergyHandler(this::getEnergyContainers, this.getExtractPredicate(), this.getInsertPredicate(), null)
         )
      );
      ComputerCapabilityHelper.addComputerCapabilities(this, x$0 -> this.addCapabilityResolver(x$0));
   }

   protected UniversalCable createTransmitter(IBlockProvider blockProvider) {
      return new UniversalCable(blockProvider, this);
   }

   public UniversalCable getTransmitter() {
      return (UniversalCable)super.getTransmitter();
   }

   @Override
   protected void onUpdateServer() {
      this.getTransmitter().pullFromAcceptors();
      super.onUpdateServer();
   }

   @Override
   public TransmitterType getTransmitterType() {
      return TransmitterType.UNIVERSAL_CABLE;
   }

   @NotNull
   @Override
   protected BlockState upgradeResult(@NotNull BlockState current, @NotNull BaseTier tier) {
      return BlockStateHelper.copyStateData(current, switch (tier) {
         case BASIC -> MekanismBlocks.BASIC_UNIVERSAL_CABLE;
         case ADVANCED -> MekanismBlocks.ADVANCED_UNIVERSAL_CABLE;
         case ELITE -> MekanismBlocks.ELITE_UNIVERSAL_CABLE;
         case ULTIMATE -> MekanismBlocks.ULTIMATE_UNIVERSAL_CABLE;
         default -> null;
      });
   }

   @NotNull
   @Override
   public CompoundTag m_5995_() {
      CompoundTag updateTag = super.m_5995_();
      if (this.getTransmitter().hasTransmitterNetwork()) {
         EnergyNetwork network = this.getTransmitter().getTransmitterNetwork();
         updateTag.m_128359_("energy", network.energyContainer.getEnergy().toString());
         updateTag.m_128350_("scale", network.currentScale);
      }

      return updateTag;
   }

   private List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
      return this.energyHandlerManager.getContainers(side);
   }

   @Override
   public void sideChanged(@NotNull Direction side, @NotNull ConnectionType old, @NotNull ConnectionType type) {
      super.sideChanged(side, old, type);
      if (type == ConnectionType.NONE) {
         this.invalidateCapabilities(EnergyCompatUtils.getEnabledEnergyCapabilities(), side);
         WorldUtils.notifyNeighborOfChange(this.f_58857_, side, this.f_58858_);
      } else if (old == ConnectionType.NONE) {
         WorldUtils.notifyNeighborOfChange(this.f_58857_, side, this.f_58858_);
      }
   }

   @Override
   public void redstoneChanged(boolean powered) {
      super.redstoneChanged(powered);
      if (powered) {
         this.invalidateCapabilities(EnergyCompatUtils.getEnabledEnergyCapabilities(), EnumUtils.DIRECTIONS);
      }
   }

   @Override
   public String getComputerName() {
      return this.getTransmitter().getTier().getBaseTier().getLowerName() + "UniversalCable";
   }

   @ComputerMethod
   FloatingLong getBuffer() {
      return this.getTransmitter().getBufferWithFallback();
   }

   @ComputerMethod
   FloatingLong getCapacity() {
      UniversalCable cable = this.getTransmitter();
      return cable.hasTransmitterNetwork() ? cable.getTransmitterNetwork().getCapacityAsFloatingLong() : cable.getCapacityAsFloatingLong();
   }

   @ComputerMethod
   FloatingLong getNeeded() {
      return this.getCapacity().subtract(this.getBuffer());
   }

   @ComputerMethod
   double getFilledPercentage() {
      return this.getBuffer().divideToLevel(this.getCapacity());
   }
}
