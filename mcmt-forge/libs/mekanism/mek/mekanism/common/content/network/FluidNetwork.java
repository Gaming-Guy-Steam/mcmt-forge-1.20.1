package mekanism.common.content.network;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import mekanism.api.Action;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.math.MathUtils;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.content.network.distribution.FluidHandlerTarget;
import mekanism.common.content.network.distribution.FluidTransmitterSaveTarget;
import mekanism.common.content.network.transmitter.MechanicalPipe;
import mekanism.common.lib.transmitter.DynamicBufferedNetwork;
import mekanism.common.util.EmitUtils;
import mekanism.common.util.FluidUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FluidNetwork extends DynamicBufferedNetwork<IFluidHandler, FluidNetwork, FluidStack, MechanicalPipe> implements IMekanismFluidHandler {
   private final List<IExtendedFluidTank> fluidTanks;
   public final VariableCapacityFluidTank fluidTank;
   @NotNull
   public FluidStack lastFluid = FluidStack.EMPTY;
   private int prevTransferAmount;
   private int intCapacity;

   public FluidNetwork(UUID networkID) {
      super(networkID);
      this.fluidTank = VariableCapacityFluidTank.create(
         this::getCapacityAsInt, BasicFluidTank.alwaysTrueBi, BasicFluidTank.alwaysTrueBi, BasicFluidTank.alwaysTrue, this
      );
      this.fluidTanks = Collections.singletonList(this.fluidTank);
   }

   public FluidNetwork(Collection<FluidNetwork> networks) {
      this(UUID.randomUUID());
      this.adoptAllAndRegister(networks);
   }

   @Override
   protected void forceScaleUpdate() {
      if (!this.fluidTank.isEmpty() && this.fluidTank.getCapacity() > 0) {
         this.currentScale = Math.min(1.0F, (float)this.fluidTank.getFluidAmount() / this.fluidTank.getCapacity());
      } else {
         this.currentScale = 0.0F;
      }
   }

   public List<MechanicalPipe> adoptTransmittersAndAcceptorsFrom(FluidNetwork net) {
      float oldScale = this.currentScale;
      long oldCapacity = this.getCapacity();
      List<MechanicalPipe> transmittersToUpdate = super.adoptTransmittersAndAcceptorsFrom(net);
      long capacity = this.getCapacity();
      this.currentScale = Math.min(
         1.0F, capacity == 0L ? 0.0F : (this.currentScale * (float)oldCapacity + net.currentScale * (float)net.capacity) / (float)capacity
      );
      if (this.isRemote()) {
         if (this.fluidTank.isEmpty() && !net.fluidTank.isEmpty()) {
            this.fluidTank.setStack(net.getBuffer());
            net.fluidTank.setEmpty();
         }
      } else {
         if (!net.fluidTank.isEmpty()) {
            if (this.fluidTank.isEmpty()) {
               this.fluidTank.setStack(net.getBuffer());
            } else if (this.fluidTank.isFluidEqual(net.fluidTank.getFluid())) {
               int amount = net.fluidTank.getFluidAmount();
               MekanismUtils.logMismatchedStackSize(this.fluidTank.growStack(amount, Action.EXECUTE), amount);
            } else {
               Mekanism.logger.error("Incompatible fluid networks merged.");
            }

            net.fluidTank.setEmpty();
         }

         if (oldScale != this.currentScale) {
            this.needsUpdate = true;
         }
      }

      return transmittersToUpdate;
   }

   @NotNull
   public FluidStack getBuffer() {
      return this.fluidTank.getFluid().copy();
   }

   public void absorbBuffer(MechanicalPipe transmitter) {
      FluidStack fluid = transmitter.releaseShare();
      if (!fluid.isEmpty()) {
         if (this.fluidTank.isEmpty()) {
            this.fluidTank.setStack(fluid.copy());
         } else if (this.fluidTank.isFluidEqual(fluid)) {
            int amount = fluid.getAmount();
            MekanismUtils.logMismatchedStackSize(this.fluidTank.growStack(amount, Action.EXECUTE), amount);
         }
      }
   }

   @Override
   public void clampBuffer() {
      if (!this.fluidTank.isEmpty()) {
         int capacity = this.getCapacityAsInt();
         if (this.fluidTank.getFluidAmount() > capacity) {
            MekanismUtils.logMismatchedStackSize(this.fluidTank.setStackSize(capacity, Action.EXECUTE), capacity);
         }
      }
   }

   protected synchronized void updateCapacity(MechanicalPipe transmitter) {
      super.updateCapacity(transmitter);
      this.intCapacity = MathUtils.clampToInt(this.getCapacity());
   }

   @Override
   public synchronized void updateCapacity() {
      super.updateCapacity();
      this.intCapacity = MathUtils.clampToInt(this.getCapacity());
   }

   public int getCapacityAsInt() {
      return this.intCapacity;
   }

   protected void updateSaveShares(@Nullable MechanicalPipe triggerTransmitter) {
      super.updateSaveShares(triggerTransmitter);
      if (!this.isEmpty()) {
         FluidStack fluidType = this.fluidTank.getFluid();
         FluidTransmitterSaveTarget saveTarget = new FluidTransmitterSaveTarget(fluidType, this.transmitters);
         EmitUtils.sendToAcceptors(saveTarget, fluidType.getAmount(), fluidType);
         saveTarget.saveShare();
      }
   }

   private int tickEmit(@NotNull FluidStack fluidToSend) {
      Collection<Map<Direction, LazyOptional<IFluidHandler>>> acceptorValues = this.acceptorCache.getAcceptorValues();
      FluidHandlerTarget target = new FluidHandlerTarget(fluidToSend, acceptorValues.size() * 2);

      for (Map<Direction, LazyOptional<IFluidHandler>> acceptors : acceptorValues) {
         for (LazyOptional<IFluidHandler> lazyAcceptor : acceptors.values()) {
            lazyAcceptor.ifPresent(acceptor -> {
               if (FluidUtils.canFill(acceptor, fluidToSend)) {
                  target.addHandler(acceptor);
               }
            });
         }
      }

      return EmitUtils.sendToAcceptors(target, fluidToSend.getAmount(), fluidToSend);
   }

   @Override
   public void onUpdate() {
      super.onUpdate();
      if (this.needsUpdate) {
         MinecraftForge.EVENT_BUS.post(new FluidNetwork.FluidTransferEvent(this, this.lastFluid));
         this.needsUpdate = false;
      }

      if (this.fluidTank.isEmpty()) {
         this.prevTransferAmount = 0;
      } else {
         this.prevTransferAmount = this.tickEmit(this.fluidTank.getFluid());
         MekanismUtils.logMismatchedStackSize(this.fluidTank.shrinkStack(this.prevTransferAmount, Action.EXECUTE), this.prevTransferAmount);
      }
   }

   @Override
   protected float computeContentScale() {
      float scale = (float)this.fluidTank.getFluidAmount() / this.fluidTank.getCapacity();
      float ret = Math.max(this.currentScale, scale);
      if (this.prevTransferAmount > 0 && ret < 1.0F) {
         ret = Math.min(1.0F, ret + 0.02F);
      } else if (this.prevTransferAmount <= 0 && ret > 0.0F) {
         ret = Math.max(scale, ret - 0.02F);
      }

      return ret;
   }

   public int getPrevTransferAmount() {
      return this.prevTransferAmount;
   }

   @Override
   public String toString() {
      return "[FluidNetwork] " + this.transmittersSize() + " transmitters, " + this.getAcceptorCount() + " acceptors.";
   }

   @Override
   public Component getNeededInfo() {
      return MekanismLang.FLUID_NETWORK_NEEDED.translate(new Object[]{this.fluidTank.getNeeded() / 1000.0F});
   }

   @Override
   public Component getStoredInfo() {
      return this.fluidTank.isEmpty()
         ? MekanismLang.NONE.translate(new Object[0])
         : MekanismLang.NETWORK_MB_STORED.translate(new Object[]{this.fluidTank.getFluid(), this.fluidTank.getFluidAmount()});
   }

   @Override
   public Component getFlowInfo() {
      return MekanismLang.NETWORK_MB_PER_TICK.translate(new Object[]{this.prevTransferAmount});
   }

   public boolean isCompatibleWith(FluidNetwork other) {
      return super.isCompatibleWith(other)
         && (this.fluidTank.isEmpty() || other.fluidTank.isEmpty() || this.fluidTank.isFluidEqual(other.fluidTank.getFluid()));
   }

   @NotNull
   @Override
   public Component getTextComponent() {
      return MekanismLang.NETWORK_DESCRIPTION.translate(new Object[]{MekanismLang.FLUID_NETWORK, this.transmittersSize(), this.getAcceptorCount()});
   }

   @NotNull
   @Override
   public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
      return this.fluidTanks;
   }

   @Override
   public void onContentsChanged() {
      this.markDirty();
      FluidStack type = this.fluidTank.getFluid();
      if (!this.lastFluid.isFluidEqual(type)) {
         if (!type.isEmpty()) {
            this.lastFluid = new FluidStack(type, 1);
         }

         this.needsUpdate = true;
      }
   }

   public void setLastFluid(@NotNull FluidStack fluid) {
      if (fluid.isEmpty()) {
         this.fluidTank.setEmpty();
      } else {
         this.lastFluid = fluid;
         this.fluidTank.setStack(new FluidStack(fluid, 1));
      }
   }

   public static class FluidTransferEvent extends DynamicBufferedNetwork.TransferEvent<FluidNetwork> {
      public final FluidStack fluidType;

      public FluidTransferEvent(FluidNetwork network, @NotNull FluidStack type) {
         super(network);
         this.fluidType = type;
      }
   }
}
