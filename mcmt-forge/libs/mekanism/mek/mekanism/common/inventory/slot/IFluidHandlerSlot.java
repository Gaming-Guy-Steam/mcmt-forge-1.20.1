package mekanism.common.inventory.slot;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.tile.interfaces.IFluidContainerManager;
import mekanism.common.util.MekanismUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.ItemHandlerHelper;

public interface IFluidHandlerSlot extends IInventorySlot {
   IExtendedFluidTank getFluidTank();

   boolean isDraining();

   boolean isFilling();

   void setDraining(boolean draining);

   void setFilling(boolean filling);

   default void handleTank(IInventorySlot outputSlot, IFluidContainerManager.ContainerEditMode editMode) {
      if (!this.isEmpty()) {
         if (editMode == IFluidContainerManager.ContainerEditMode.FILL) {
            this.drainTank(outputSlot);
         } else if (editMode == IFluidContainerManager.ContainerEditMode.EMPTY) {
            this.fillTank(outputSlot);
         } else if (editMode == IFluidContainerManager.ContainerEditMode.BOTH) {
            ItemStack stack = this.getStack();
            Optional<IFluidHandlerItem> cap = FluidUtil.getFluidHandler(stack.m_41613_() > 1 ? stack.m_255036_(1) : stack).resolve();
            if (cap.isPresent()) {
               IFluidHandlerItem fluidHandlerItem = cap.get();
               boolean hasEmpty = false;

               for (int tank = 0; tank < fluidHandlerItem.getTanks(); tank++) {
                  FluidStack fluidInTank = fluidHandlerItem.getFluidInTank(tank);
                  if (fluidInTank.isEmpty()) {
                     hasEmpty = true;
                  } else if (!this.isDraining()
                     && this.getFluidTank().insert(fluidInTank, Action.SIMULATE, AutomationType.INTERNAL).getAmount() < fluidInTank.getAmount()) {
                     this.fillTank(outputSlot);
                     return;
                  }
               }

               if (this.isFilling()) {
                  if (this.moveItem(outputSlot, stack)) {
                     this.setFilling(false);
                  }
               } else if (this.getFluidTank().isEmpty() && hasEmpty
                  || this.isDraining()
                  || fluidHandlerItem.fill(this.getFluidTank().getFluid().copy(), FluidAction.SIMULATE) > 0) {
                  this.drainTank(outputSlot);
               }
            }
         }
      }
   }

   default void fillTank(IInventorySlot outputSlot) {
      if (!this.isEmpty()) {
         Optional<IFluidHandlerItem> capability = FluidUtil.getFluidHandler(this.getStack()).resolve();
         if (capability.isPresent()) {
            IFluidHandlerItem itemFluidHandler = capability.get();
            int itemTanks = itemFluidHandler.getTanks();
            if (itemTanks == 1) {
               FluidStack fluidInItem = itemFluidHandler.getFluidInTank(0);
               if (!fluidInItem.isEmpty() && this.getFluidTank().isFluidValid(fluidInItem)) {
                  this.drainItemAndMove(outputSlot, fluidInItem);
               }
            } else if (itemTanks > 1) {
               for (FluidStack knownFluid : this.gatherKnownFluids(itemFluidHandler, itemTanks)) {
                  if (this.drainItemAndMove(outputSlot, knownFluid) && this.isEmpty()) {
                     break;
                  }
               }
            }
         }
      }
   }

   default void drainTank(IInventorySlot outputSlot) {
      if (!this.isEmpty() && FluidUtil.getFluidHandler(this.getStack()).isPresent()) {
         FluidStack fluidInTank = this.getFluidTank().getFluid();
         if (!fluidInTank.isEmpty()) {
            FluidStack simulatedDrain = this.getFluidTank().extract(fluidInTank.getAmount(), Action.SIMULATE, AutomationType.INTERNAL);
            if (simulatedDrain.isEmpty()) {
               return;
            }

            ItemStack inputCopy = this.getStack().m_255036_(1);
            Optional<IFluidHandlerItem> cap = FluidUtil.getFluidHandler(inputCopy).resolve();
            if (cap.isPresent()) {
               IFluidHandlerItem fluidHandlerItem = cap.get();
               int toDrain = fluidHandlerItem.fill(fluidInTank.copy(), FluidAction.EXECUTE);
               if (this.getCount() == 1) {
                  Optional<IFluidHandlerItem> containerCap = FluidUtil.getFluidHandler(fluidHandlerItem.getContainer()).resolve();
                  if (containerCap.isPresent() && containerCap.get().fill(fluidInTank.copy(), FluidAction.SIMULATE) > 0) {
                     this.setStack(fluidHandlerItem.getContainer());
                     this.setDraining(true);
                     MekanismUtils.logMismatchedStackSize(this.getFluidTank().shrinkStack(toDrain, Action.EXECUTE), toDrain);
                     return;
                  }
               }

               if (this.moveItem(outputSlot, fluidHandlerItem.getContainer())) {
                  MekanismUtils.logMismatchedStackSize(this.getFluidTank().shrinkStack(toDrain, Action.EXECUTE), toDrain);
                  this.setDraining(false);
               }
            }
         }
      }
   }

   private boolean drainItemAndMove(IInventorySlot outputSlot, FluidStack fluidToTransfer) {
      FluidStack simulatedRemainder = this.getFluidTank().insert(fluidToTransfer, Action.SIMULATE, AutomationType.INTERNAL);
      int remainder = simulatedRemainder.getAmount();
      int toTransfer = fluidToTransfer.getAmount();
      if (remainder == toTransfer) {
         return false;
      } else {
         ItemStack input = this.getStack().m_255036_(1);
         Optional<IFluidHandlerItem> cap = FluidUtil.getFluidHandler(input).resolve();
         if (cap.isEmpty()) {
            return false;
         } else {
            IFluidHandlerItem fluidHandlerItem = cap.get();
            FluidStack drained = fluidHandlerItem.drain(new FluidStack(fluidToTransfer, toTransfer - remainder), FluidAction.EXECUTE);
            if (drained.isEmpty()) {
               return false;
            } else {
               if (this.getCount() == 1) {
                  Optional<IFluidHandlerItem> containerCap = FluidUtil.getFluidHandler(fluidHandlerItem.getContainer()).resolve();
                  if (containerCap.isPresent() && !containerCap.get().drain(Integer.MAX_VALUE, FluidAction.SIMULATE).isEmpty()) {
                     this.setStack(fluidHandlerItem.getContainer());
                     this.getFluidTank().insert(drained, Action.EXECUTE, AutomationType.INTERNAL);
                     this.setFilling(true);
                     return true;
                  }
               }

               if (this.moveItem(outputSlot, fluidHandlerItem.getContainer())) {
                  this.getFluidTank().insert(drained, Action.EXECUTE, AutomationType.INTERNAL);
                  return true;
               } else {
                  return false;
               }
            }
         }
      }
   }

   private boolean moveItem(IInventorySlot outputSlot, ItemStack stackToMove) {
      if (outputSlot.isEmpty()) {
         outputSlot.setStack(stackToMove);
      } else {
         ItemStack outputStack = outputSlot.getStack();
         if (!ItemHandlerHelper.canItemStacksStack(outputStack, stackToMove) || outputStack.m_41613_() >= outputSlot.getLimit(outputStack)) {
            return false;
         }

         MekanismUtils.logMismatchedStackSize(outputSlot.growStack(1, Action.EXECUTE), 1L);
      }

      MekanismUtils.logMismatchedStackSize(this.shrinkStack(1, Action.EXECUTE), 1L);
      return true;
   }

   default boolean fillTank() {
      if (this.getCount() == 1) {
         Optional<IFluidHandlerItem> capability = FluidUtil.getFluidHandler(this.getStack()).resolve();
         if (capability.isPresent()) {
            IFluidHandlerItem itemFluidHandler = capability.get();
            int tanks = itemFluidHandler.getTanks();
            if (tanks == 1) {
               FluidStack fluidInItem = itemFluidHandler.getFluidInTank(0);
               if (!fluidInItem.isEmpty()
                  && this.getFluidTank().isFluidValid(fluidInItem)
                  && this.fillHandlerFromOther(this.getFluidTank(), itemFluidHandler, fluidInItem)) {
                  this.setStack(itemFluidHandler.getContainer());
                  return true;
               }
            } else if (tanks > 1) {
               Set<FluidStack> knownFluids = this.gatherKnownFluids(itemFluidHandler, tanks);
               if (!knownFluids.isEmpty()) {
                  boolean changed = false;

                  for (FluidStack knownFluid : knownFluids) {
                     if (this.fillHandlerFromOther(this.getFluidTank(), itemFluidHandler, knownFluid)) {
                        changed = true;
                     }
                  }

                  if (changed) {
                     this.setStack(itemFluidHandler.getContainer());
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   private Set<FluidStack> gatherKnownFluids(IFluidHandlerItem itemFluidHandler, int tanks) {
      Map<FluidStack, FluidStack> knownFluids = new HashMap<>();

      for (int tank = 0; tank < tanks; tank++) {
         FluidStack fluidInItem = itemFluidHandler.getFluidInTank(tank);
         if (!fluidInItem.isEmpty()) {
            FluidStack knownFluid = knownFluids.get(fluidInItem);
            if (knownFluid == null) {
               if (!itemFluidHandler.drain(fluidInItem.copy(), FluidAction.SIMULATE).isEmpty() && this.getFluidTank().isFluidValid(fluidInItem)) {
                  FluidStack copy = fluidInItem.copy();
                  knownFluids.put(copy, copy);
               }
            } else {
               knownFluid.grow(fluidInItem.getAmount());
            }
         }
      }

      return knownFluids.keySet();
   }

   private boolean fillHandlerFromOther(IExtendedFluidTank handlerToFill, IFluidHandler handlerToDrain, FluidStack fluid) {
      FluidStack simulatedDrain = handlerToDrain.drain(fluid.copy(), FluidAction.SIMULATE);
      if (!simulatedDrain.isEmpty()) {
         FluidStack simulatedRemainder = this.getFluidTank().insert(simulatedDrain, Action.SIMULATE, AutomationType.INTERNAL);
         int remainder = simulatedRemainder.getAmount();
         int drained = simulatedDrain.getAmount();
         if (remainder < drained) {
            handlerToFill.insert(handlerToDrain.drain(new FluidStack(fluid, drained - remainder), FluidAction.EXECUTE), Action.EXECUTE, AutomationType.INTERNAL);
            return true;
         }
      }

      return false;
   }
}
