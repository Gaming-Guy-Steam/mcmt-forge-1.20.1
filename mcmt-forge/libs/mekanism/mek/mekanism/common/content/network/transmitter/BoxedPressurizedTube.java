package mekanism.common.content.network.transmitter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.merged.BoxedChemical;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.chemical.merged.MergedChemicalTank;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.capabilities.chemical.BoxedChemicalHandler;
import mekanism.common.capabilities.chemical.dynamic.IGasTracker;
import mekanism.common.capabilities.chemical.dynamic.IInfusionTracker;
import mekanism.common.capabilities.chemical.dynamic.IPigmentTracker;
import mekanism.common.capabilities.chemical.dynamic.ISlurryTracker;
import mekanism.common.content.network.BoxedChemicalNetwork;
import mekanism.common.lib.transmitter.CompatibleTransmitterValidator;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.lib.transmitter.acceptor.BoxedChemicalAcceptorCache;
import mekanism.common.tier.TubeTier;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.upgrade.transmitter.PressurizedTubeUpgradeData;
import mekanism.common.upgrade.transmitter.TransmitterUpgradeData;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BoxedPressurizedTube
   extends BufferedTransmitter<BoxedChemicalHandler, BoxedChemicalNetwork, BoxedChemicalStack, BoxedPressurizedTube>
   implements IGasTracker,
   IInfusionTracker,
   IPigmentTracker,
   ISlurryTracker,
   IUpgradeableTransmitter<PressurizedTubeUpgradeData> {
   public final TubeTier tier;
   public final MergedChemicalTank chemicalTank;
   private final List<IGasTank> gasTanks;
   private final List<IInfusionTank> infusionTanks;
   private final List<IPigmentTank> pigmentTanks;
   private final List<ISlurryTank> slurryTanks;
   @NotNull
   public BoxedChemicalStack saveShare = BoxedChemicalStack.EMPTY;

   public BoxedPressurizedTube(IBlockProvider blockProvider, TileEntityTransmitter tile) {
      super(tile, TransmissionType.GAS, TransmissionType.INFUSION, TransmissionType.PIGMENT, TransmissionType.SLURRY);
      this.tier = Attribute.getTier(blockProvider, TubeTier.class);
      this.chemicalTank = MergedChemicalTank.create(
         (IGasTank)ChemicalTankBuilder.GAS.createAllValid(this.getCapacity(), this),
         (IInfusionTank)ChemicalTankBuilder.INFUSION.createAllValid(this.getCapacity(), this),
         (IPigmentTank)ChemicalTankBuilder.PIGMENT.createAllValid(this.getCapacity(), this),
         (ISlurryTank)ChemicalTankBuilder.SLURRY.createAllValid(this.getCapacity(), this)
      );
      this.gasTanks = Collections.singletonList(this.chemicalTank.getGasTank());
      this.infusionTanks = Collections.singletonList(this.chemicalTank.getInfusionTank());
      this.pigmentTanks = Collections.singletonList(this.chemicalTank.getPigmentTank());
      this.slurryTanks = Collections.singletonList(this.chemicalTank.getSlurryTank());
   }

   protected BoxedChemicalAcceptorCache createAcceptorCache() {
      return new BoxedChemicalAcceptorCache(this, this.getTransmitterTile());
   }

   public BoxedChemicalAcceptorCache getAcceptorCache() {
      return (BoxedChemicalAcceptorCache)super.getAcceptorCache();
   }

   public TubeTier getTier() {
      return this.tier;
   }

   @Override
   public void pullFromAcceptors() {
      Set<Direction> connections = this.getConnections(ConnectionType.PULL);
      if (!connections.isEmpty()) {
         for (BoxedChemicalHandler connectedAcceptor : this.getAcceptorCache().getConnectedAcceptors(connections)) {
            BoxedChemicalStack bufferWithFallback = this.getBufferWithFallback();
            if (bufferWithFallback.isEmpty()) {
               for (ChemicalType chemicalType : EnumUtils.CHEMICAL_TYPES) {
                  if (this.pullFromAcceptor(connectedAcceptor, chemicalType, bufferWithFallback, true)) {
                     break;
                  }
               }
            } else {
               this.pullFromAcceptor(connectedAcceptor, bufferWithFallback.getChemicalType(), bufferWithFallback, false);
            }
         }
      }
   }

   private boolean pullFromAcceptor(BoxedChemicalHandler acceptor, ChemicalType chemicalType, BoxedChemicalStack bufferWithFallback, boolean bufferIsEmpty) {
      IChemicalHandler<?, ?> handler = acceptor.getHandlerFor(chemicalType);
      return handler != null ? this.pullFromAcceptor(handler, bufferWithFallback, chemicalType, bufferIsEmpty) : false;
   }

   private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, STACK>> boolean pullFromAcceptor(
      HANDLER connectedAcceptor, BoxedChemicalStack bufferWithFallback, ChemicalType chemicalType, boolean bufferIsEmpty
   ) {
      long availablePull = this.getAvailablePull(chemicalType);
      STACK received;
      if (bufferIsEmpty) {
         received = connectedAcceptor.extractChemical(availablePull, Action.SIMULATE);
      } else {
         received = connectedAcceptor.extractChemical(ChemicalUtil.copyWithAmount((STACK)bufferWithFallback.getChemicalStack(), availablePull), Action.SIMULATE);
      }

      if (!received.isEmpty() && this.takeChemical(chemicalType, received, Action.SIMULATE).isEmpty()) {
         this.takeChemical(chemicalType, connectedAcceptor.extractChemical(received, Action.EXECUTE), Action.EXECUTE);
         return true;
      } else {
         return false;
      }
   }

   private long getAvailablePull(ChemicalType chemicalType) {
      return this.hasTransmitterNetwork()
         ? Math.min(this.tier.getTubePullAmount(), this.getTransmitterNetwork().chemicalTank.getTankForType(chemicalType).getNeeded())
         : Math.min(this.tier.getTubePullAmount(), this.chemicalTank.getTankForType(chemicalType).getNeeded());
   }

   @Nullable
   public PressurizedTubeUpgradeData getUpgradeData() {
      return new PressurizedTubeUpgradeData(this.redstoneReactive, this.getConnectionTypesRaw(), this.getShare());
   }

   @Override
   public boolean dataTypeMatches(@NotNull TransmitterUpgradeData data) {
      return data instanceof PressurizedTubeUpgradeData;
   }

   public void parseUpgradeData(@NotNull PressurizedTubeUpgradeData data) {
      this.redstoneReactive = data.redstoneReactive;
      this.setConnectionTypesRaw(data.connectionTypes);
      this.takeChemical(data.contents, Action.EXECUTE);
   }

   @Override
   public void read(@NotNull CompoundTag nbtTags) {
      super.read(nbtTags);
      if (nbtTags.m_128425_("boxedChemical", 10)) {
         this.saveShare = BoxedChemicalStack.read(nbtTags.m_128469_("boxedChemical"));
      } else {
         this.saveShare = BoxedChemicalStack.EMPTY;
      }

      this.setStackClearOthers(this.saveShare.getChemicalStack(), this.chemicalTank.getTankForType(this.saveShare.getChemicalType()));
   }

   private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> void setStackClearOthers(STACK stack, IChemicalTank<?, ?> tank) {
      ((IChemicalTank<?, STACK>)tank).setStack(stack);

      for (IChemicalTank<?, ?> tankToClear : this.chemicalTank.getAllTanks()) {
         if (tank != tankToClear) {
            tankToClear.setEmpty();
         }
      }
   }

   @NotNull
   @Override
   public CompoundTag write(@NotNull CompoundTag nbtTags) {
      super.write(nbtTags);
      if (this.hasTransmitterNetwork()) {
         this.getTransmitterNetwork().validateSaveShares(this);
      }

      if (this.saveShare.isEmpty()) {
         nbtTags.m_128473_("boxedChemical");
      } else {
         nbtTags.m_128365_("boxedChemical", this.saveShare.write(new CompoundTag()));
      }

      return nbtTags;
   }

   @Override
   public boolean isValidAcceptor(BlockEntity tile, Direction side) {
      return super.isValidAcceptor(tile, side) && this.getAcceptorCache().isChemicalAcceptorAndListen(tile, side);
   }

   public BoxedChemicalNetwork createEmptyNetworkWithID(UUID networkID) {
      return new BoxedChemicalNetwork(networkID);
   }

   public BoxedChemicalNetwork createNetworkByMerging(Collection<BoxedChemicalNetwork> toMerge) {
      return new BoxedChemicalNetwork(toMerge);
   }

   @Override
   public CompatibleTransmitterValidator<BoxedChemicalHandler, BoxedChemicalNetwork, BoxedPressurizedTube> getNewOrphanValidator() {
      return new CompatibleTransmitterValidator.CompatibleChemicalTransmitterValidator(this);
   }

   @Override
   public boolean isValidTransmitter(TileEntityTransmitter transmitter, Direction side) {
      if (super.isValidTransmitter(transmitter, side) && transmitter.getTransmitter() instanceof BoxedPressurizedTube other) {
         BoxedChemical buffer = this.getBufferWithFallback().getType();
         if (buffer.isEmpty() && this.hasTransmitterNetwork() && this.getTransmitterNetwork().getPrevTransferAmount() > 0L) {
            buffer = this.getTransmitterNetwork().lastChemical;
         }

         BoxedChemical otherBuffer = other.getBufferWithFallback().getType();
         if (otherBuffer.isEmpty() && other.hasTransmitterNetwork() && other.getTransmitterNetwork().getPrevTransferAmount() > 0L) {
            otherBuffer = other.getTransmitterNetwork().lastChemical;
         }

         return buffer.isEmpty() || otherBuffer.isEmpty() || buffer.equals(otherBuffer);
      } else {
         return false;
      }
   }

   @Override
   protected boolean canHaveIncompatibleNetworks() {
      return true;
   }

   @Override
   public long getCapacity() {
      return this.tier.getTubeCapacity();
   }

   @NotNull
   public BoxedChemicalStack releaseShare() {
      MergedChemicalTank.Current current = this.chemicalTank.getCurrent();
      BoxedChemicalStack ret;
      if (current == MergedChemicalTank.Current.EMPTY) {
         ret = BoxedChemicalStack.EMPTY;
      } else {
         IChemicalTank<?, ?> tank = this.chemicalTank.getTankFromCurrent(current);
         ret = BoxedChemicalStack.box(tank.getStack());
         tank.setEmpty();
      }

      return ret;
   }

   @NotNull
   public BoxedChemicalStack getShare() {
      MergedChemicalTank.Current current = this.chemicalTank.getCurrent();
      return current == MergedChemicalTank.Current.EMPTY
         ? BoxedChemicalStack.EMPTY
         : BoxedChemicalStack.box(this.chemicalTank.getTankFromCurrent(current).getStack());
   }

   @Override
   public boolean noBufferOrFallback() {
      return this.getBufferWithFallback().isEmpty();
   }

   @NotNull
   public BoxedChemicalStack getBufferWithFallback() {
      BoxedChemicalStack buffer = this.getShare();
      return buffer.isEmpty() && this.hasTransmitterNetwork() ? this.getTransmitterNetwork().getBuffer() : buffer;
   }

   @Override
   public void takeShare() {
      if (this.hasTransmitterNetwork()) {
         BoxedChemicalNetwork transmitterNetwork = this.getTransmitterNetwork();
         MergedChemicalTank.Current networkCurrent = transmitterNetwork.chemicalTank.getCurrent();
         if (networkCurrent != MergedChemicalTank.Current.EMPTY && !this.saveShare.isEmpty()) {
            ChemicalStack<?> chemicalStack = this.saveShare.getChemicalStack();
            long amount = chemicalStack.getAmount();
            MekanismUtils.logMismatchedStackSize(transmitterNetwork.chemicalTank.getTankFromCurrent(networkCurrent).shrinkStack(amount, Action.EXECUTE), amount);
            this.setStackClearOthers(chemicalStack, this.chemicalTank.getTankFromCurrent(networkCurrent));
         }
      }
   }

   public void takeChemical(BoxedChemicalStack stack, Action action) {
      this.takeChemical(stack.getChemicalType(), stack.getChemicalStack(), action);
   }

   @NotNull
   private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> STACK takeChemical(ChemicalType type, STACK stack, Action action) {
      IChemicalTank<CHEMICAL, STACK> tank;
      if (this.hasTransmitterNetwork()) {
         tank = (IChemicalTank<CHEMICAL, STACK>)this.getTransmitterNetwork().chemicalTank.getTankForType(type);
      } else {
         tank = (IChemicalTank<CHEMICAL, STACK>)this.chemicalTank.getTankForType(type);
      }

      return tank.insert(stack, action, AutomationType.INTERNAL);
   }

   @NotNull
   @Override
   public List<IGasTank> getGasTanks(@Nullable Direction side) {
      return this.hasTransmitterNetwork() ? this.getTransmitterNetwork().getGasTanks(side) : this.gasTanks;
   }

   @NotNull
   @Override
   public List<IInfusionTank> getInfusionTanks(@Nullable Direction side) {
      return this.hasTransmitterNetwork() ? this.getTransmitterNetwork().getInfusionTanks(side) : this.infusionTanks;
   }

   @NotNull
   @Override
   public List<IPigmentTank> getPigmentTanks(@Nullable Direction side) {
      return this.hasTransmitterNetwork() ? this.getTransmitterNetwork().getPigmentTanks(side) : this.pigmentTanks;
   }

   @NotNull
   @Override
   public List<ISlurryTank> getSlurryTanks(@Nullable Direction side) {
      return this.hasTransmitterNetwork() ? this.getTransmitterNetwork().getSlurryTanks(side) : this.slurryTanks;
   }

   @Override
   public void onContentsChanged() {
      this.getTransmitterTile().m_6596_();
   }

   protected void handleContentsUpdateTag(@NotNull BoxedChemicalNetwork network, @NotNull CompoundTag tag) {
      super.handleContentsUpdateTag(network, tag);
      NBTUtils.setFloatIfPresent(tag, "scale", scale -> network.currentScale = scale);
      NBTUtils.setBoxedChemicalIfPresent(tag, "boxedChemical", network::setLastChemical);
   }

   public IGasTank getGasTank() {
      return this.chemicalTank.getGasTank();
   }

   public IInfusionTank getInfusionTank() {
      return this.chemicalTank.getInfusionTank();
   }

   public IPigmentTank getPigmentTank() {
      return this.chemicalTank.getPigmentTank();
   }

   public ISlurryTank getSlurryTank() {
      return this.chemicalTank.getSlurryTank();
   }
}
