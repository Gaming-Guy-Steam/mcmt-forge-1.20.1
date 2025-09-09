package mekanism.common.content.network;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.math.FloatingLong;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.VariableCapacityEnergyContainer;
import mekanism.common.content.network.distribution.EnergyAcceptorTarget;
import mekanism.common.content.network.distribution.EnergyTransmitterSaveTarget;
import mekanism.common.content.network.transmitter.UniversalCable;
import mekanism.common.lib.transmitter.DynamicBufferedNetwork;
import mekanism.common.util.EmitUtils;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EnergyNetwork
   extends DynamicBufferedNetwork<IStrictEnergyHandler, EnergyNetwork, FloatingLong, UniversalCable>
   implements IMekanismStrictEnergyHandler {
   private final List<IEnergyContainer> energyContainers;
   public final VariableCapacityEnergyContainer energyContainer;
   private FloatingLong prevTransferAmount = FloatingLong.ZERO;
   private FloatingLong floatingLongCapacity = FloatingLong.ZERO;

   public EnergyNetwork(UUID networkID) {
      super(networkID);
      this.energyContainer = VariableCapacityEnergyContainer.create(
         this::getCapacityAsFloatingLong, BasicEnergyContainer.alwaysTrue, BasicEnergyContainer.alwaysTrue, this
      );
      this.energyContainers = Collections.singletonList(this.energyContainer);
   }

   public EnergyNetwork(Collection<EnergyNetwork> networks) {
      this(UUID.randomUUID());
      this.adoptAllAndRegister(networks);
   }

   @Override
   protected void forceScaleUpdate() {
      if (!this.energyContainer.isEmpty() && !this.energyContainer.getMaxEnergy().isZero()) {
         this.currentScale = Math.min(1.0F, this.energyContainer.getEnergy().divide(this.energyContainer.getMaxEnergy()).floatValue());
      } else {
         this.currentScale = 0.0F;
      }
   }

   public List<UniversalCable> adoptTransmittersAndAcceptorsFrom(EnergyNetwork net) {
      FloatingLong oldCapacity = this.getCapacityAsFloatingLong();
      List<UniversalCable> transmittersToUpdate = super.adoptTransmittersAndAcceptorsFrom(net);
      FloatingLong ourScale = this.currentScale == 0.0F ? FloatingLong.ZERO : oldCapacity.multiply((double)this.currentScale);
      FloatingLong theirScale = net.currentScale == 0.0F ? FloatingLong.ZERO : net.getCapacityAsFloatingLong().multiply((double)net.currentScale);
      FloatingLong capacity = this.getCapacityAsFloatingLong();
      this.currentScale = Math.min(1.0F, capacity.isZero() ? 0.0F : ourScale.add(theirScale).divide(this.getCapacityAsFloatingLong()).floatValue());
      if (!this.isRemote() && !net.energyContainer.isEmpty()) {
         this.energyContainer.setEnergy(this.energyContainer.getEnergy().add(net.getBuffer()));
         net.energyContainer.setEmpty();
      }

      return transmittersToUpdate;
   }

   @NotNull
   public FloatingLong getBuffer() {
      return this.energyContainer.getEnergy();
   }

   public void absorbBuffer(UniversalCable transmitter) {
      FloatingLong energy = transmitter.releaseShare();
      if (!energy.isZero()) {
         this.energyContainer.setEnergy(this.energyContainer.getEnergy().add(energy));
      }
   }

   @Override
   public void clampBuffer() {
      if (!this.energyContainer.isEmpty()) {
         FloatingLong capacity = this.getCapacityAsFloatingLong();
         if (this.energyContainer.getEnergy().greaterThan(capacity)) {
            this.energyContainer.setEnergy(capacity);
         }
      }
   }

   protected synchronized void updateCapacity(UniversalCable transmitter) {
      this.floatingLongCapacity = this.floatingLongCapacity.plusEqual(transmitter.getCapacityAsFloatingLong());
      this.capacity = this.floatingLongCapacity.longValue();
   }

   @Override
   public synchronized void updateCapacity() {
      FloatingLong sum = FloatingLong.ZERO;

      for (UniversalCable transmitter : this.transmitters) {
         sum = sum.plusEqual(transmitter.getCapacityAsFloatingLong());
      }

      if (!this.floatingLongCapacity.equals(sum)) {
         this.floatingLongCapacity = sum;
         this.capacity = this.floatingLongCapacity.longValue();
      }
   }

   @NotNull
   public FloatingLong getCapacityAsFloatingLong() {
      return this.floatingLongCapacity;
   }

   protected void updateSaveShares(@Nullable UniversalCable triggerTransmitter) {
      super.updateSaveShares(triggerTransmitter);
      if (!this.isEmpty()) {
         EnergyTransmitterSaveTarget saveTarget = new EnergyTransmitterSaveTarget(this.transmitters);
         EmitUtils.sendToAcceptors(saveTarget, this.energyContainer.getEnergy().copy());
         saveTarget.saveShare();
      }
   }

   private FloatingLong tickEmit(FloatingLong energyToSend) {
      Collection<Map<Direction, LazyOptional<IStrictEnergyHandler>>> acceptorValues = this.acceptorCache.getAcceptorValues();
      EnergyAcceptorTarget target = new EnergyAcceptorTarget(acceptorValues.size() * 2);

      for (Map<Direction, LazyOptional<IStrictEnergyHandler>> acceptors : acceptorValues) {
         for (LazyOptional<IStrictEnergyHandler> lazyAcceptor : acceptors.values()) {
            lazyAcceptor.ifPresent(acceptor -> {
               if (acceptor.insertEnergy(energyToSend, Action.SIMULATE).smallerThan(energyToSend)) {
                  target.addHandler(acceptor);
               }
            });
         }
      }

      return EmitUtils.sendToAcceptors(target, energyToSend.copy());
   }

   @Override
   public String toString() {
      return "[EnergyNetwork] " + this.transmittersSize() + " transmitters, " + this.getAcceptorCount() + " acceptors.";
   }

   @Override
   public void onUpdate() {
      super.onUpdate();
      if (this.needsUpdate) {
         MinecraftForge.EVENT_BUS.post(new EnergyNetwork.EnergyTransferEvent(this));
         this.needsUpdate = false;
      }

      if (this.energyContainer.isEmpty()) {
         this.prevTransferAmount = FloatingLong.ZERO;
      } else {
         this.prevTransferAmount = this.tickEmit(this.energyContainer.getEnergy());
         this.energyContainer.extract(this.prevTransferAmount, Action.EXECUTE, AutomationType.INTERNAL);
      }
   }

   @Override
   protected float computeContentScale() {
      float scale = (float)this.energyContainer.getEnergy().divideToLevel(this.energyContainer.getMaxEnergy());
      float ret = Math.max(this.currentScale, scale);
      if (!this.prevTransferAmount.isZero() && ret < 1.0F) {
         ret = Math.min(1.0F, ret + 0.02F);
      } else if (this.prevTransferAmount.isZero() && ret > 0.0F) {
         ret = Math.max(scale, ret - 0.02F);
      }

      return ret;
   }

   @Override
   public Component getNeededInfo() {
      return EnergyDisplay.of(this.energyContainer.getNeeded()).getTextComponent();
   }

   @Override
   public Component getStoredInfo() {
      return EnergyDisplay.of(this.energyContainer.getEnergy()).getTextComponent();
   }

   @Override
   public Component getFlowInfo() {
      return MekanismLang.GENERIC_PER_TICK.translate(new Object[]{EnergyDisplay.of(this.prevTransferAmount)});
   }

   @Override
   public Object getNetworkReaderCapacity() {
      return this.getCapacityAsFloatingLong();
   }

   @NotNull
   @Override
   public Component getTextComponent() {
      return MekanismLang.NETWORK_DESCRIPTION.translate(new Object[]{MekanismLang.ENERGY_NETWORK, this.transmittersSize(), this.getAcceptorCount()});
   }

   @NotNull
   @Override
   public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
      return this.energyContainers;
   }

   @Override
   public void onContentsChanged() {
      this.markDirty();
   }

   public static class EnergyTransferEvent extends DynamicBufferedNetwork.TransferEvent<EnergyNetwork> {
      public EnergyTransferEvent(EnergyNetwork network) {
         super(network);
      }
   }
}
