package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.util.MekanismUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class FluidInventorySlot extends BasicInventorySlot implements IFluidHandlerSlot {
   protected final IExtendedFluidTank fluidTank;
   private boolean isDraining;
   private boolean isFilling;

   public static FluidInventorySlot input(IExtendedFluidTank fluidTank, @Nullable IContentsListener listener, int x, int y) {
      Objects.requireNonNull(fluidTank, "Fluid tank cannot be null");
      return new FluidInventorySlot(fluidTank, alwaysFalse, getInputPredicate(fluidTank), stack -> FluidUtil.getFluidHandler(stack).isPresent(), listener, x, y);
   }

   protected static Predicate<ItemStack> getInputPredicate(IExtendedFluidTank fluidTank) {
      return stack -> {
         Optional<IFluidHandlerItem> cap = FluidUtil.getFluidHandler(stack.m_41613_() > 1 ? stack.m_255036_(1) : stack).resolve();
         if (cap.isPresent()) {
            IFluidHandlerItem fluidHandlerItem = cap.get();
            boolean hasEmpty = false;

            for (int tank = 0; tank < fluidHandlerItem.getTanks(); tank++) {
               FluidStack fluidInTank = fluidHandlerItem.getFluidInTank(tank);
               if (fluidInTank.isEmpty()) {
                  hasEmpty = true;
               } else if (fluidTank.insert(fluidInTank, Action.SIMULATE, AutomationType.INTERNAL).getAmount() < fluidInTank.getAmount()) {
                  return true;
               }
            }

            return fluidTank.isEmpty() ? hasEmpty : fluidHandlerItem.fill(fluidTank.getFluid().copy(), FluidAction.SIMULATE) > 0;
         } else {
            return false;
         }
      };
   }

   public static FluidInventorySlot rotary(IExtendedFluidTank fluidTank, BooleanSupplier modeSupplier, @Nullable IContentsListener listener, int x, int y) {
      Objects.requireNonNull(fluidTank, "Fluid tank cannot be null");
      Objects.requireNonNull(modeSupplier, "Mode supplier cannot be null");
      return new FluidInventorySlot(fluidTank, alwaysFalse, stack -> {
         Optional<IFluidHandlerItem> cap = FluidUtil.getFluidHandler(stack).resolve();
         if (!cap.isPresent()) {
            return false;
         } else {
            boolean mode = modeSupplier.getAsBoolean();
            IFluidHandlerItem fluidHandlerItem = cap.get();
            boolean allEmpty = true;

            for (int tank = 0; tank < fluidHandlerItem.getTanks(); tank++) {
               FluidStack fluidInTank = fluidHandlerItem.getFluidInTank(tank);
               if (!fluidInTank.isEmpty()) {
                  if (fluidTank.insert(fluidInTank, Action.SIMULATE, AutomationType.INTERNAL).getAmount() < fluidInTank.getAmount()) {
                     return mode;
                  }

                  allEmpty = false;
               }
            }

            return allEmpty && !mode;
         }
      }, stack -> {
         LazyOptional<IFluidHandlerItem> capability = FluidUtil.getFluidHandler(stack);
         if (capability.isPresent()) {
            if (modeSupplier.getAsBoolean()) {
               IFluidHandlerItem fluidHandlerItem = (IFluidHandlerItem)capability.orElseThrow(MekanismUtils.MISSING_CAP_ERROR);

               for (int tank = 0; tank < fluidHandlerItem.getTanks(); tank++) {
                  FluidStack fluidInTank = fluidHandlerItem.getFluidInTank(tank);
                  if (!fluidInTank.isEmpty() && fluidTank.isFluidValid(fluidInTank)) {
                     return true;
                  }
               }

               return false;
            } else {
               return true;
            }
         } else {
            return false;
         }
      }, listener, x, y);
   }

   public static FluidInventorySlot fill(IExtendedFluidTank fluidTank, @Nullable IContentsListener listener, int x, int y) {
      Objects.requireNonNull(fluidTank, "Fluid tank cannot be null");
      return new FluidInventorySlot(fluidTank, alwaysFalse, stack -> {
         Optional<IFluidHandlerItem> cap = FluidUtil.getFluidHandler(stack).resolve();
         if (cap.isPresent()) {
            IFluidHandlerItem fluidHandlerItem = cap.get();

            for (int tank = 0; tank < fluidHandlerItem.getTanks(); tank++) {
               FluidStack fluidInTank = fluidHandlerItem.getFluidInTank(tank);
               if (!fluidInTank.isEmpty() && fluidTank.insert(fluidInTank, Action.SIMULATE, AutomationType.INTERNAL).getAmount() < fluidInTank.getAmount()) {
                  return true;
               }
            }
         }

         return false;
      }, stack -> FluidUtil.getFluidHandler(stack).isPresent(), listener, x, y);
   }

   public static FluidInventorySlot drain(IExtendedFluidTank fluidTank, @Nullable IContentsListener listener, int x, int y) {
      Objects.requireNonNull(fluidTank, "Fluid handler cannot be null");
      return new FluidInventorySlot(fluidTank, alwaysFalse, stack -> {
         LazyOptional<IFluidHandlerItem> cap = FluidUtil.getFluidHandler(stack.m_41613_() > 1 ? stack.m_255036_(1) : stack);
         if (cap.isPresent()) {
            FluidStack fluidInTank = fluidTank.getFluid();
            if (fluidInTank.isEmpty()) {
               return true;
            } else {
               IFluidHandlerItem itemFluidHandler = (IFluidHandlerItem)cap.orElseThrow(MekanismUtils.MISSING_CAP_ERROR);
               return itemFluidHandler.fill(fluidInTank.copy(), FluidAction.SIMULATE) > 0;
            }
         } else {
            return false;
         }
      }, stack -> isNonFullFluidContainer(FluidUtil.getFluidHandler(stack)), listener, x, y);
   }

   private static boolean isNonFullFluidContainer(LazyOptional<IFluidHandlerItem> capability) {
      Optional<IFluidHandlerItem> cap = capability.resolve();
      if (cap.isPresent()) {
         IFluidHandlerItem fluidHandler = cap.get();

         for (int tank = 0; tank < fluidHandler.getTanks(); tank++) {
            if (fluidHandler.getFluidInTank(tank).getAmount() < fluidHandler.getTankCapacity(tank)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   protected FluidInventorySlot(
      IExtendedFluidTank fluidTank,
      Predicate<ItemStack> canExtract,
      Predicate<ItemStack> canInsert,
      Predicate<ItemStack> validator,
      @Nullable IContentsListener listener,
      int x,
      int y
   ) {
      super(canExtract, canInsert, validator, listener, x, y);
      this.setSlotType(ContainerSlotType.EXTRA);
      this.fluidTank = fluidTank;
   }

   @Override
   public void setStack(ItemStack stack) {
      super.setStack(stack);
      this.isDraining = false;
      this.isFilling = false;
   }

   @Override
   public IExtendedFluidTank getFluidTank() {
      return this.fluidTank;
   }

   @Override
   public boolean isDraining() {
      return this.isDraining;
   }

   @Override
   public boolean isFilling() {
      return this.isFilling;
   }

   @Override
   public void setDraining(boolean draining) {
      this.isDraining = draining;
   }

   @Override
   public void setFilling(boolean filling) {
      this.isFilling = filling;
   }

   @Override
   public CompoundTag serializeNBT() {
      CompoundTag nbt = super.serializeNBT();
      if (this.isDraining) {
         nbt.m_128379_("draining", true);
      }

      if (this.isFilling) {
         nbt.m_128379_("filling", true);
      }

      return nbt;
   }

   @Override
   public void deserializeNBT(CompoundTag nbt) {
      this.isDraining = nbt.m_128471_("draining");
      this.isFilling = nbt.m_128471_("filling");
      super.deserializeNBT(nbt);
   }
}
