package mekanism.common.content.network.transmitter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.math.MathUtils;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.content.network.FluidNetwork;
import mekanism.common.lib.transmitter.CompatibleTransmitterValidator;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.lib.transmitter.acceptor.AcceptorCache;
import mekanism.common.tier.PipeTier;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.upgrade.transmitter.MechanicalPipeUpgradeData;
import mekanism.common.upgrade.transmitter.TransmitterUpgradeData;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MechanicalPipe
   extends BufferedTransmitter<IFluidHandler, FluidNetwork, FluidStack, MechanicalPipe>
   implements IMekanismFluidHandler,
   IUpgradeableTransmitter<MechanicalPipeUpgradeData> {
   public final PipeTier tier;
   @NotNull
   public FluidStack saveShare = FluidStack.EMPTY;
   private final List<IExtendedFluidTank> tanks;
   public final BasicFluidTank buffer;

   public MechanicalPipe(IBlockProvider blockProvider, TileEntityTransmitter tile) {
      super(tile, TransmissionType.FLUID);
      this.tier = Attribute.getTier(blockProvider, PipeTier.class);
      this.buffer = BasicFluidTank.create(MathUtils.clampToInt(this.getCapacity()), BasicFluidTank.alwaysFalse, BasicFluidTank.alwaysTrue, this);
      this.tanks = Collections.singletonList(this.buffer);
   }

   public AcceptorCache<IFluidHandler> getAcceptorCache() {
      return (AcceptorCache<IFluidHandler>)super.getAcceptorCache();
   }

   public PipeTier getTier() {
      return this.tier;
   }

   @Override
   public void pullFromAcceptors() {
      Set<Direction> connections = this.getConnections(ConnectionType.PULL);
      if (!connections.isEmpty()) {
         for (IFluidHandler connectedAcceptor : this.getAcceptorCache().getConnectedAcceptors(connections)) {
            FluidStack bufferWithFallback = this.getBufferWithFallback();
            FluidStack received;
            if (bufferWithFallback.isEmpty()) {
               received = connectedAcceptor.drain(this.getAvailablePull(), FluidAction.SIMULATE);
            } else {
               received = connectedAcceptor.drain(new FluidStack(bufferWithFallback, this.getAvailablePull()), FluidAction.SIMULATE);
            }

            if (!received.isEmpty() && this.takeFluid(received, Action.SIMULATE).isEmpty()) {
               this.takeFluid(connectedAcceptor.drain(received.copy(), FluidAction.EXECUTE), Action.EXECUTE);
            }
         }
      }
   }

   private int getAvailablePull() {
      return this.hasTransmitterNetwork()
         ? Math.min(this.tier.getPipePullAmount(), this.getTransmitterNetwork().fluidTank.getNeeded())
         : Math.min(this.tier.getPipePullAmount(), this.buffer.getNeeded());
   }

   @Nullable
   public MechanicalPipeUpgradeData getUpgradeData() {
      return new MechanicalPipeUpgradeData(this.redstoneReactive, this.getConnectionTypesRaw(), this.getShare());
   }

   @Override
   public boolean dataTypeMatches(@NotNull TransmitterUpgradeData data) {
      return data instanceof MechanicalPipeUpgradeData;
   }

   public void parseUpgradeData(@NotNull MechanicalPipeUpgradeData data) {
      this.redstoneReactive = data.redstoneReactive;
      this.setConnectionTypesRaw(data.connectionTypes);
      this.takeFluid(data.contents, Action.EXECUTE);
   }

   @Override
   public void read(@NotNull CompoundTag nbtTags) {
      super.read(nbtTags);
      if (nbtTags.m_128425_("fluid", 10)) {
         this.saveShare = FluidStack.loadFluidStackFromNBT(nbtTags.m_128469_("fluid"));
      } else {
         this.saveShare = FluidStack.EMPTY;
      }

      this.buffer.setStack(this.saveShare);
   }

   @NotNull
   @Override
   public CompoundTag write(@NotNull CompoundTag nbtTags) {
      super.write(nbtTags);
      if (this.hasTransmitterNetwork()) {
         this.getTransmitterNetwork().validateSaveShares(this);
      }

      if (this.saveShare.isEmpty()) {
         nbtTags.m_128473_("fluid");
      } else {
         nbtTags.m_128365_("fluid", this.saveShare.writeToNBT(new CompoundTag()));
      }

      return nbtTags;
   }

   @Override
   public boolean isValidAcceptor(BlockEntity tile, Direction side) {
      return super.isValidAcceptor(tile, side) && this.getAcceptorCache().isAcceptorAndListen(tile, side, ForgeCapabilities.FLUID_HANDLER);
   }

   @Override
   public CompatibleTransmitterValidator<IFluidHandler, FluidNetwork, MechanicalPipe> getNewOrphanValidator() {
      return new CompatibleTransmitterValidator.CompatibleFluidTransmitterValidator(this);
   }

   @Override
   public boolean isValidTransmitter(TileEntityTransmitter transmitter, Direction side) {
      if (super.isValidTransmitter(transmitter, side) && transmitter.getTransmitter() instanceof MechanicalPipe other) {
         FluidStack buffer = this.getBufferWithFallback();
         if (buffer.isEmpty() && this.hasTransmitterNetwork() && this.getTransmitterNetwork().getPrevTransferAmount() > 0) {
            buffer = this.getTransmitterNetwork().lastFluid;
         }

         FluidStack otherBuffer = other.getBufferWithFallback();
         if (otherBuffer.isEmpty() && other.hasTransmitterNetwork() && other.getTransmitterNetwork().getPrevTransferAmount() > 0) {
            otherBuffer = other.getTransmitterNetwork().lastFluid;
         }

         return buffer.isEmpty() || otherBuffer.isEmpty() || buffer.isFluidEqual(otherBuffer);
      } else {
         return false;
      }
   }

   public FluidNetwork createEmptyNetworkWithID(UUID networkID) {
      return new FluidNetwork(networkID);
   }

   public FluidNetwork createNetworkByMerging(Collection<FluidNetwork> networks) {
      return new FluidNetwork(networks);
   }

   @Override
   protected boolean canHaveIncompatibleNetworks() {
      return true;
   }

   @Override
   public long getCapacity() {
      return this.tier.getPipeCapacity();
   }

   @NotNull
   public FluidStack releaseShare() {
      FluidStack ret = this.buffer.getFluid();
      this.buffer.setEmpty();
      return ret;
   }

   @Override
   public boolean noBufferOrFallback() {
      return this.getBufferWithFallback().isEmpty();
   }

   @NotNull
   public FluidStack getBufferWithFallback() {
      FluidStack buffer = this.getShare();
      return buffer.isEmpty() && this.hasTransmitterNetwork() ? this.getTransmitterNetwork().getBuffer() : buffer;
   }

   @NotNull
   public FluidStack getShare() {
      return this.buffer.getFluid();
   }

   @Override
   public void takeShare() {
      if (this.hasTransmitterNetwork()) {
         FluidNetwork network = this.getTransmitterNetwork();
         if (!network.fluidTank.isEmpty() && !this.saveShare.isEmpty()) {
            int amount = this.saveShare.getAmount();
            MekanismUtils.logMismatchedStackSize(network.fluidTank.shrinkStack(amount, Action.EXECUTE), amount);
            this.buffer.setStack(this.saveShare);
         }
      }
   }

   @NotNull
   @Override
   public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
      return this.hasTransmitterNetwork() ? this.getTransmitterNetwork().getFluidTanks(side) : this.tanks;
   }

   @Override
   public void onContentsChanged() {
      this.getTransmitterTile().m_6596_();
   }

   @NotNull
   public FluidStack takeFluid(@NotNull FluidStack fluid, Action action) {
      return this.hasTransmitterNetwork()
         ? this.getTransmitterNetwork().fluidTank.insert(fluid, action, AutomationType.INTERNAL)
         : this.buffer.insert(fluid, action, AutomationType.INTERNAL);
   }

   protected void handleContentsUpdateTag(@NotNull FluidNetwork network, @NotNull CompoundTag tag) {
      super.handleContentsUpdateTag(network, tag);
      NBTUtils.setFluidStackIfPresent(tag, "fluid", network::setLastFluid);
      NBTUtils.setFloatIfPresent(tag, "scale", scale -> network.currentScale = scale);
   }
}
