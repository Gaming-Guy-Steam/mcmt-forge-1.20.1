package mekanism.common.tile.transmitter;

import java.util.Collections;
import java.util.List;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.tier.BaseTier;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.capabilities.fluid.DynamicFluidHandler;
import mekanism.common.capabilities.resolver.manager.FluidHandlerManager;
import mekanism.common.content.network.FluidNetwork;
import mekanism.common.content.network.transmitter.MechanicalPipe;
import mekanism.common.integration.computer.ComputerCapabilityHelper;
import mekanism.common.integration.computer.IComputerTile;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityMechanicalPipe extends TileEntityTransmitter implements IComputerTile {
   private final FluidHandlerManager fluidHandlerManager;

   public TileEntityMechanicalPipe(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
      super(blockProvider, pos, state);
      this.addCapabilityResolver(
         this.fluidHandlerManager = new FluidHandlerManager(
            direction -> {
               MechanicalPipe pipe = this.getTransmitter();
               return (direction == null || pipe.getConnectionTypeRaw(direction) != ConnectionType.NONE) && !pipe.isRedstoneActivated()
                  ? pipe.getFluidTanks(direction)
                  : Collections.emptyList();
            },
            new DynamicFluidHandler(this::getFluidTanks, this.getExtractPredicate(), this.getInsertPredicate(), null)
         )
      );
      ComputerCapabilityHelper.addComputerCapabilities(this, x$0 -> this.addCapabilityResolver(x$0));
   }

   protected MechanicalPipe createTransmitter(IBlockProvider blockProvider) {
      return new MechanicalPipe(blockProvider, this);
   }

   public MechanicalPipe getTransmitter() {
      return (MechanicalPipe)super.getTransmitter();
   }

   @Override
   protected void onUpdateServer() {
      this.getTransmitter().pullFromAcceptors();
      super.onUpdateServer();
   }

   @Override
   public TransmitterType getTransmitterType() {
      return TransmitterType.MECHANICAL_PIPE;
   }

   @NotNull
   @Override
   protected BlockState upgradeResult(@NotNull BlockState current, @NotNull BaseTier tier) {
      return BlockStateHelper.copyStateData(current, switch (tier) {
         case BASIC -> MekanismBlocks.BASIC_MECHANICAL_PIPE;
         case ADVANCED -> MekanismBlocks.ADVANCED_MECHANICAL_PIPE;
         case ELITE -> MekanismBlocks.ELITE_MECHANICAL_PIPE;
         case ULTIMATE -> MekanismBlocks.ULTIMATE_MECHANICAL_PIPE;
         default -> null;
      });
   }

   @NotNull
   @Override
   public CompoundTag m_5995_() {
      CompoundTag updateTag = super.m_5995_();
      if (this.getTransmitter().hasTransmitterNetwork()) {
         FluidNetwork network = this.getTransmitter().getTransmitterNetwork();
         updateTag.m_128365_("fluid", network.lastFluid.writeToNBT(new CompoundTag()));
         updateTag.m_128350_("scale", network.currentScale);
      }

      return updateTag;
   }

   private List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
      return this.fluidHandlerManager.getContainers(side);
   }

   @Override
   public void sideChanged(@NotNull Direction side, @NotNull ConnectionType old, @NotNull ConnectionType type) {
      super.sideChanged(side, old, type);
      if (type == ConnectionType.NONE) {
         this.invalidateCapability(ForgeCapabilities.FLUID_HANDLER, side);
         WorldUtils.notifyNeighborOfChange(this.f_58857_, side, this.f_58858_);
      } else if (old == ConnectionType.NONE) {
         WorldUtils.notifyNeighborOfChange(this.f_58857_, side, this.f_58858_);
      }
   }

   @Override
   public void redstoneChanged(boolean powered) {
      super.redstoneChanged(powered);
      if (powered) {
         this.invalidateCapability(ForgeCapabilities.FLUID_HANDLER, EnumUtils.DIRECTIONS);
      }
   }

   @Override
   public String getComputerName() {
      return this.getTransmitter().getTier().getBaseTier().getLowerName() + "MechanicalPipe";
   }

   @ComputerMethod
   FluidStack getBuffer() {
      return this.getTransmitter().getBufferWithFallback();
   }

   @ComputerMethod
   long getCapacity() {
      MechanicalPipe pipe = this.getTransmitter();
      return pipe.hasTransmitterNetwork() ? pipe.getTransmitterNetwork().getCapacity() : pipe.getCapacity();
   }

   @ComputerMethod
   long getNeeded() {
      return this.getCapacity() - this.getBuffer().getAmount();
   }

   @ComputerMethod
   double getFilledPercentage() {
      return (double)this.getBuffer().getAmount() / this.getCapacity();
   }
}
