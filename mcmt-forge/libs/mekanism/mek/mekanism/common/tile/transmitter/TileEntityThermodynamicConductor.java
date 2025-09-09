package mekanism.common.tile.transmitter;

import java.util.Collections;
import java.util.List;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IMekanismHeatHandler;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.tier.BaseTier;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.resolver.manager.HeatHandlerManager;
import mekanism.common.content.network.transmitter.ThermodynamicConductor;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityThermodynamicConductor extends TileEntityTransmitter {
   private final HeatHandlerManager heatHandlerManager;

   public TileEntityThermodynamicConductor(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
      super(blockProvider, pos, state);
      this.addCapabilityResolver(
         this.heatHandlerManager = new HeatHandlerManager(
            direction -> {
               ThermodynamicConductor conductor = this.getTransmitter();
               return (direction == null || conductor.getConnectionTypeRaw(direction) != ConnectionType.NONE) && !conductor.isRedstoneActivated()
                  ? conductor.getHeatCapacitors(direction)
                  : Collections.emptyList();
            },
            new IMekanismHeatHandler() {
               @NotNull
               @Override
               public List<IHeatCapacitor> getHeatCapacitors(@Nullable Direction side) {
                  return TileEntityThermodynamicConductor.this.heatHandlerManager.getContainers(side);
               }

               @Override
               public void onContentsChanged() {
               }
            }
         )
      );
   }

   protected ThermodynamicConductor createTransmitter(IBlockProvider blockProvider) {
      return new ThermodynamicConductor(blockProvider, this);
   }

   public ThermodynamicConductor getTransmitter() {
      return (ThermodynamicConductor)super.getTransmitter();
   }

   @Override
   public TransmitterType getTransmitterType() {
      return TransmitterType.THERMODYNAMIC_CONDUCTOR;
   }

   @NotNull
   @Override
   protected BlockState upgradeResult(@NotNull BlockState current, @NotNull BaseTier tier) {
      return BlockStateHelper.copyStateData(current, switch (tier) {
         case BASIC -> MekanismBlocks.BASIC_THERMODYNAMIC_CONDUCTOR;
         case ADVANCED -> MekanismBlocks.ADVANCED_THERMODYNAMIC_CONDUCTOR;
         case ELITE -> MekanismBlocks.ELITE_THERMODYNAMIC_CONDUCTOR;
         case ULTIMATE -> MekanismBlocks.ULTIMATE_THERMODYNAMIC_CONDUCTOR;
         default -> null;
      });
   }

   @Override
   public void sideChanged(@NotNull Direction side, @NotNull ConnectionType old, @NotNull ConnectionType type) {
      super.sideChanged(side, old, type);
      if (type == ConnectionType.NONE) {
         this.invalidateCapability(Capabilities.HEAT_HANDLER, side);
         WorldUtils.notifyNeighborOfChange(this.f_58857_, side, this.f_58858_);
      } else if (old == ConnectionType.NONE) {
         WorldUtils.notifyNeighborOfChange(this.f_58857_, side, this.f_58858_);
      }
   }

   @Override
   public void redstoneChanged(boolean powered) {
      super.redstoneChanged(powered);
      if (powered) {
         this.invalidateCapability(Capabilities.HEAT_HANDLER, EnumUtils.DIRECTIONS);
      }
   }
}
