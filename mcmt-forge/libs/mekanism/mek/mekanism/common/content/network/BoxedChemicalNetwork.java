package mekanism.common.content.network;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import mekanism.api.Action;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.merged.BoxedChemical;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.chemical.merged.MergedChemicalTank;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.radiation.IRadiationManager;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.chemical.BoxedChemicalHandler;
import mekanism.common.capabilities.chemical.dynamic.IGasTracker;
import mekanism.common.capabilities.chemical.dynamic.IInfusionTracker;
import mekanism.common.capabilities.chemical.dynamic.IPigmentTracker;
import mekanism.common.capabilities.chemical.dynamic.ISlurryTracker;
import mekanism.common.capabilities.chemical.variable.VariableCapacityChemicalTankBuilder;
import mekanism.common.content.network.distribution.BoxedChemicalTransmitterSaveTarget;
import mekanism.common.content.network.distribution.ChemicalHandlerTarget;
import mekanism.common.content.network.transmitter.BoxedPressurizedTube;
import mekanism.common.lib.transmitter.DynamicBufferedNetwork;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.EmitUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BoxedChemicalNetwork
   extends DynamicBufferedNetwork<BoxedChemicalHandler, BoxedChemicalNetwork, BoxedChemicalStack, BoxedPressurizedTube>
   implements IGasTracker,
   IInfusionTracker,
   IPigmentTracker,
   ISlurryTracker {
   public final MergedChemicalTank chemicalTank;
   private final List<IGasTank> gasTanks;
   private final List<IInfusionTank> infusionTanks;
   private final List<IPigmentTank> pigmentTanks;
   private final List<ISlurryTank> slurryTanks;
   @NotNull
   public BoxedChemical lastChemical = BoxedChemical.EMPTY;
   private long prevTransferAmount;

   public BoxedChemicalNetwork(UUID networkID) {
      super(networkID);
      this.chemicalTank = MergedChemicalTank.create(
         (IGasTank)VariableCapacityChemicalTankBuilder.GAS.createAllValid(this::getCapacity, this),
         (IInfusionTank)VariableCapacityChemicalTankBuilder.INFUSION.createAllValid(this::getCapacity, this),
         (IPigmentTank)VariableCapacityChemicalTankBuilder.PIGMENT.createAllValid(this::getCapacity, this),
         (ISlurryTank)VariableCapacityChemicalTankBuilder.SLURRY.createAllValid(this::getCapacity, this)
      );
      this.gasTanks = Collections.singletonList(this.chemicalTank.getGasTank());
      this.infusionTanks = Collections.singletonList(this.chemicalTank.getInfusionTank());
      this.pigmentTanks = Collections.singletonList(this.chemicalTank.getPigmentTank());
      this.slurryTanks = Collections.singletonList(this.chemicalTank.getSlurryTank());
   }

   public BoxedChemicalNetwork(Collection<BoxedChemicalNetwork> networks) {
      this(UUID.randomUUID());
      this.adoptAllAndRegister(networks);
   }

   public boolean isTankEmpty() {
      return this.chemicalTank.getCurrent() == MergedChemicalTank.Current.EMPTY;
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

   private IChemicalTank<?, ?> getCurrentTankWithFallback() {
      MergedChemicalTank.Current current = this.chemicalTank.getCurrent();
      return (IChemicalTank<?, ?>)(current == MergedChemicalTank.Current.EMPTY ? this.getGasTank() : this.chemicalTank.getTankFromCurrent(current));
   }

   @Override
   protected void forceScaleUpdate() {
      if (!this.isTankEmpty() && this.getCapacity() > 0L) {
         this.currentScale = (float)Math.min(1.0, (double)this.getCurrentTankWithFallback().getStored() / this.getCapacity());
      } else {
         this.currentScale = 0.0F;
      }
   }

   public List<BoxedPressurizedTube> adoptTransmittersAndAcceptorsFrom(BoxedChemicalNetwork net) {
      float oldScale = this.currentScale;
      long oldCapacity = this.getCapacity();
      List<BoxedPressurizedTube> transmittersToUpdate = super.adoptTransmittersAndAcceptorsFrom(net);
      long capacity = this.getCapacity();
      this.currentScale = Math.min(
         1.0F, capacity == 0L ? 0.0F : (this.currentScale * (float)oldCapacity + net.currentScale * (float)net.capacity) / (float)capacity
      );
      if (this.isRemote()) {
         if (this.isTankEmpty()) {
            this.adoptBuffer(net);
         }
      } else {
         if (!net.isTankEmpty()) {
            if (this.isTankEmpty()) {
               this.adoptBuffer(net);
            } else {
               MergedChemicalTank.Current current = this.chemicalTank.getCurrent();
               MergedChemicalTank.Current netCurrent = net.chemicalTank.getCurrent();
               if (current == netCurrent) {
                  IChemicalTank<?, ?> tank = this.chemicalTank.getTankFromCurrent(current);
                  IChemicalTank<?, ?> netTank = net.chemicalTank.getTankFromCurrent(current);
                  if (tank.getType() == netTank.getType()) {
                     long amount = netTank.getStored();
                     MekanismUtils.logMismatchedStackSize(tank.growStack(amount, Action.EXECUTE), amount);
                  }

                  netTank.setEmpty();
               } else {
                  Mekanism.logger.error("Incompatible chemical networks merged: {}, {}.", current, netCurrent);
               }
            }
         }

         if (oldScale != this.currentScale) {
            this.needsUpdate = true;
         }
      }

      return transmittersToUpdate;
   }

   private void adoptBuffer(BoxedChemicalNetwork net) {
      switch (net.chemicalTank.getCurrent()) {
         case GAS:
            this.moveBuffer(this.getGasTank(), net.getGasTank());
            break;
         case INFUSION:
            this.moveBuffer(this.getInfusionTank(), net.getInfusionTank());
            break;
         case PIGMENT:
            this.moveBuffer(this.getPigmentTank(), net.getPigmentTank());
            break;
         case SLURRY:
            this.moveBuffer(this.getSlurryTank(), net.getSlurryTank());
      }
   }

   private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>> void moveBuffer(
      TANK tank, TANK other
   ) {
      tank.setStack(ChemicalUtil.copy(other.getStack()));
      other.setEmpty();
   }

   @NotNull
   public BoxedChemicalStack getBuffer() {
      MergedChemicalTank.Current current = this.chemicalTank.getCurrent();
      return current == MergedChemicalTank.Current.EMPTY
         ? BoxedChemicalStack.EMPTY
         : BoxedChemicalStack.box(this.chemicalTank.getTankFromCurrent(current).getStack().copy());
   }

   public void absorbBuffer(BoxedPressurizedTube transmitter) {
      BoxedChemicalStack chemical = transmitter.releaseShare();
      if (!chemical.isEmpty()) {
         MergedChemicalTank.Current current = this.chemicalTank.getCurrent();
         ChemicalStack<?> chemicalStack = chemical.getChemicalStack();
         if (current == MergedChemicalTank.Current.EMPTY) {
            this.setStack(chemicalStack.copy(), this.chemicalTank.getTankForType(chemical.getChemicalType()));
         } else if (ChemicalUtil.compareTypes(chemical.getChemicalType(), current)) {
            IChemicalTank<?, ?> tank = this.chemicalTank.getTankFromCurrent(current);
            if (chemicalStack.getType() == tank.getType()) {
               long amount = chemicalStack.getAmount();
               MekanismUtils.logMismatchedStackSize(tank.growStack(amount, Action.EXECUTE), amount);
            }
         }
      }
   }

   @Override
   public void clampBuffer() {
      MergedChemicalTank.Current current = this.chemicalTank.getCurrent();
      if (current != MergedChemicalTank.Current.EMPTY) {
         long capacity = this.getCapacity();
         IChemicalTank<?, ?> tank = this.chemicalTank.getTankFromCurrent(current);
         if (tank.getStored() > capacity) {
            MekanismUtils.logMismatchedStackSize(tank.setStackSize(capacity, Action.EXECUTE), capacity);
         }
      }
   }

   protected void updateSaveShares(@Nullable BoxedPressurizedTube triggerTransmitter) {
      super.updateSaveShares(triggerTransmitter);
      if (!this.isEmpty()) {
         this.updateSaveShares(triggerTransmitter, this.getCurrentTankWithFallback().getStack());
      }
   }

   private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> void updateSaveShares(
      @Nullable BoxedPressurizedTube triggerTransmitter, STACK chemical
   ) {
      STACK empty = ChemicalUtil.getEmptyStack(chemical);
      BoxedChemicalTransmitterSaveTarget<CHEMICAL, STACK> saveTarget = new BoxedChemicalTransmitterSaveTarget<>(empty, chemical, this.transmitters);
      long sent = EmitUtils.sendToAcceptors(saveTarget, chemical.getAmount(), chemical);
      if (triggerTransmitter != null && sent < chemical.getAmount()) {
         this.disperse(triggerTransmitter, ChemicalUtil.copyWithAmount(chemical, chemical.getAmount() - sent));
      }

      saveTarget.saveShare();
   }

   protected void onLastTransmitterRemoved(@NotNull BoxedPressurizedTube triggerTransmitter) {
      MergedChemicalTank.Current current = this.chemicalTank.getCurrent();
      if (current != MergedChemicalTank.Current.EMPTY) {
         this.disperse(triggerTransmitter, this.chemicalTank.getTankFromCurrent(current).getStack());
      }
   }

   protected <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> void disperse(
      @NotNull BoxedPressurizedTube triggerTransmitter, STACK chemical
   ) {
      if (chemical instanceof GasStack stack) {
         IRadiationManager.INSTANCE.dumpRadiation(triggerTransmitter.getTileCoord(), stack);
      }
   }

   private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> long tickEmit(@NotNull STACK stack) {
      ChemicalType chemicalType = ChemicalType.getTypeFor(stack);
      Collection<Map<Direction, LazyOptional<BoxedChemicalHandler>>> acceptorValues = this.acceptorCache.getAcceptorValues();
      ChemicalHandlerTarget<CHEMICAL, STACK, IChemicalHandler<CHEMICAL, STACK>> target = new ChemicalHandlerTarget<>(stack, acceptorValues.size() * 2);

      for (Map<Direction, LazyOptional<BoxedChemicalHandler>> acceptors : acceptorValues) {
         for (LazyOptional<BoxedChemicalHandler> lazyAcceptor : acceptors.values()) {
            lazyAcceptor.ifPresent(acceptor -> {
               IChemicalHandler<CHEMICAL, STACK> handler = acceptor.getHandlerFor(chemicalType);
               if (handler != null && ChemicalUtil.canInsert(handler, stack)) {
                  target.addHandler(handler);
               }
            });
         }
      }

      return EmitUtils.sendToAcceptors(target, stack.getAmount(), stack);
   }

   @Override
   public void onUpdate() {
      super.onUpdate();
      if (this.needsUpdate) {
         MinecraftForge.EVENT_BUS.post(new BoxedChemicalNetwork.ChemicalTransferEvent(this, this.lastChemical));
         this.needsUpdate = false;
      }

      MergedChemicalTank.Current current = this.chemicalTank.getCurrent();
      if (current == MergedChemicalTank.Current.EMPTY) {
         this.prevTransferAmount = 0L;
      } else {
         IChemicalTank<?, ?> tank = this.chemicalTank.getTankFromCurrent(current);
         this.prevTransferAmount = this.tickEmit(tank.getStack());
         MekanismUtils.logMismatchedStackSize(tank.shrinkStack(this.prevTransferAmount, Action.EXECUTE), this.prevTransferAmount);
      }
   }

   @Override
   protected float computeContentScale() {
      float scale = (float)((double)this.getCurrentTankWithFallback().getStored() / this.getCapacity());
      float ret = Math.max(this.currentScale, scale);
      if (this.prevTransferAmount > 0L && ret < 1.0F) {
         ret = Math.min(1.0F, ret + 0.02F);
      } else if (this.prevTransferAmount <= 0L && ret > 0.0F) {
         ret = Math.max(scale, ret - 0.02F);
      }

      return ret;
   }

   public long getPrevTransferAmount() {
      return this.prevTransferAmount;
   }

   @Override
   public Component getNeededInfo() {
      return TextComponentUtil.build(this.getCurrentTankWithFallback().getNeeded());
   }

   @Override
   public Component getStoredInfo() {
      if (this.isTankEmpty()) {
         return MekanismLang.NONE.translate(new Object[0]);
      } else {
         IChemicalTank<?, ?> tank = this.getCurrentTankWithFallback();
         return MekanismLang.NETWORK_MB_STORED.translate(new Object[]{tank.getStack(), tank.getStored()});
      }
   }

   @Override
   public Component getFlowInfo() {
      return MekanismLang.NETWORK_MB_PER_TICK.translate(new Object[]{this.prevTransferAmount});
   }

   public boolean isCompatibleWith(BoxedChemicalNetwork other) {
      if (super.isCompatibleWith(other)) {
         MergedChemicalTank.Current current = this.chemicalTank.getCurrent();
         if (current == MergedChemicalTank.Current.EMPTY) {
            return true;
         } else {
            MergedChemicalTank.Current otherCurrent = other.chemicalTank.getCurrent();
            return otherCurrent == MergedChemicalTank.Current.EMPTY
               || current == otherCurrent
                  && this.chemicalTank.getTankFromCurrent(current).getType() == other.chemicalTank.getTankFromCurrent(otherCurrent).getType();
         }
      } else {
         return false;
      }
   }

   @NotNull
   @Override
   public Component getTextComponent() {
      return MekanismLang.NETWORK_DESCRIPTION.translate(new Object[]{MekanismLang.CHEMICAL_NETWORK, this.transmittersSize(), this.getAcceptorCount()});
   }

   @Override
   public String toString() {
      return "[ChemicalNetwork] " + this.transmittersSize() + " transmitters, " + this.getAcceptorCount() + " acceptors.";
   }

   @Override
   public void onContentsChanged() {
      this.markDirty();
      MergedChemicalTank.Current current = this.chemicalTank.getCurrent();
      BoxedChemical type = current == MergedChemicalTank.Current.EMPTY
         ? BoxedChemical.EMPTY
         : BoxedChemical.box(this.chemicalTank.getTankFromCurrent(current).getType());
      if (!this.lastChemical.equals(type)) {
         if (!type.isEmpty()) {
            this.lastChemical = type;
         }

         this.needsUpdate = true;
      }
   }

   public void setLastChemical(@NotNull BoxedChemical chemical) {
      if (chemical.isEmpty()) {
         MergedChemicalTank.Current current = this.chemicalTank.getCurrent();
         if (current != MergedChemicalTank.Current.EMPTY) {
            this.chemicalTank.getTankFromCurrent(current).setEmpty();
         }
      } else {
         this.lastChemical = chemical;
         this.setStackClearOthers(this.lastChemical.getChemical().getStack(1L), this.chemicalTank.getTankForType(this.lastChemical.getChemicalType()));
      }
   }

   @NotNull
   @Override
   public List<IGasTank> getGasTanks(@Nullable Direction side) {
      return this.gasTanks;
   }

   @NotNull
   @Override
   public List<IInfusionTank> getInfusionTanks(@Nullable Direction side) {
      return this.infusionTanks;
   }

   @NotNull
   @Override
   public List<IPigmentTank> getPigmentTanks(@Nullable Direction side) {
      return this.pigmentTanks;
   }

   @NotNull
   @Override
   public List<ISlurryTank> getSlurryTanks(@Nullable Direction side) {
      return this.slurryTanks;
   }

   private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> void setStack(STACK stack, IChemicalTank<?, ?> tank) {
      ((IChemicalTank<?, STACK>)tank).setStack(stack);
   }

   private void setStackClearOthers(ChemicalStack<?> stack, IChemicalTank<?, ?> tank) {
      this.setStack(stack, tank);

      for (IChemicalTank<?, ?> tankToClear : this.chemicalTank.getAllTanks()) {
         if (tank != tankToClear) {
            tankToClear.setEmpty();
         }
      }
   }

   public static class ChemicalTransferEvent extends DynamicBufferedNetwork.TransferEvent<BoxedChemicalNetwork> {
      public final BoxedChemical transferType;

      public ChemicalTransferEvent(BoxedChemicalNetwork network, BoxedChemical type) {
         super(network);
         this.transferType = type;
      }
   }
}
