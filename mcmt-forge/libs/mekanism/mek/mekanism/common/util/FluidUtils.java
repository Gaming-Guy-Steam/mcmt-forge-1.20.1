package mekanism.common.util;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.providers.IFluidProvider;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.content.network.distribution.FluidHandlerTarget;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.NotNull;

public final class FluidUtils {
   private FluidUtils() {
   }

   public static ItemStack getFilledVariant(ItemStack toFill, CachedIntValue capacity, IFluidProvider provider) {
      return getFilledVariant(toFill, capacity.getOrDefault(), provider);
   }

   public static ItemStack getFilledVariant(ItemStack toFill, int capacity, IFluidProvider provider) {
      IExtendedFluidTank dummyTank = BasicFluidTank.create(capacity, null);
      dummyTank.setStack(provider.getFluidStack(dummyTank.getCapacity()));
      ItemDataUtils.writeContainers(toFill, "FluidTanks", Collections.singletonList(dummyTank));
      return toFill;
   }

   public static OptionalInt getRGBDurabilityForDisplay(ItemStack stack) {
      return getRGBDurabilityForDisplay(StorageUtils.getStoredFluidFromNBT(stack));
   }

   public static OptionalInt getRGBDurabilityForDisplay(FluidStack stack) {
      if (!stack.isEmpty()) {
         if (stack.getFluid().m_6212_(Fluids.f_76195_)) {
            return OptionalInt.of(-2397415);
         }

         if (FMLEnvironment.dist.isClient()) {
            return OptionalInt.of(IClientFluidTypeExtensions.of(stack.getFluid()).getTintColor(stack));
         }
      }

      return OptionalInt.empty();
   }

   public static void emit(IExtendedFluidTank tank, BlockEntity from) {
      emit(EnumSet.allOf(Direction.class), tank, from);
   }

   public static void emit(Set<Direction> outputSides, IExtendedFluidTank tank, BlockEntity from) {
      emit(outputSides, tank, from, tank.getCapacity());
   }

   public static void emit(Set<Direction> outputSides, IExtendedFluidTank tank, BlockEntity from, int maxOutput) {
      if (!tank.isEmpty() && maxOutput > 0) {
         tank.extract(emit(outputSides, tank.extract(maxOutput, Action.SIMULATE, AutomationType.INTERNAL), from), Action.EXECUTE, AutomationType.INTERNAL);
      }
   }

   public static int emit(Set<Direction> sides, @NotNull FluidStack stack, BlockEntity from) {
      if (!stack.isEmpty() && !sides.isEmpty()) {
         FluidStack toSend = stack.copy();
         FluidHandlerTarget target = new FluidHandlerTarget(stack, 6);
         EmitUtils.forEachSide(
            from.m_58904_(),
            from.m_58899_(),
            sides,
            (acceptor, side) -> CapabilityUtils.getCapability(acceptor, ForgeCapabilities.FLUID_HANDLER, side.m_122424_()).ifPresent(handler -> {
               if (canFill(handler, toSend)) {
                  target.addHandler(handler);
               }
            })
         );
         return target.getHandlerCount() > 0 ? EmitUtils.sendToAcceptors(target, stack.getAmount(), toSend) : 0;
      } else {
         return 0;
      }
   }

   public static boolean canFill(IFluidHandler handler, @NotNull FluidStack stack) {
      return handler.fill(stack.copy(), FluidAction.SIMULATE) > 0;
   }

   public static boolean handleTankInteraction(Player player, InteractionHand hand, ItemStack itemStack, IExtendedFluidTank fluidTank) {
      ItemStack copyStack = itemStack.m_255036_(1);
      Optional<IFluidHandlerItem> fluidHandlerItem = FluidUtil.getFluidHandler(copyStack).resolve();
      if (fluidHandlerItem.isPresent()) {
         IFluidHandlerItem handler = fluidHandlerItem.get();
         FluidStack fluidInItem;
         if (fluidTank.isEmpty()) {
            fluidInItem = handler.drain(Integer.MAX_VALUE, FluidAction.SIMULATE);
         } else {
            fluidInItem = handler.drain(new FluidStack(fluidTank.getFluid(), Integer.MAX_VALUE), FluidAction.SIMULATE);
         }

         if (fluidInItem.isEmpty()) {
            if (!fluidTank.isEmpty()) {
               int filled = handler.fill(fluidTank.getFluid().copy(), player.m_7500_() ? FluidAction.SIMULATE : FluidAction.EXECUTE);
               ItemStack container = handler.getContainer();
               if (filled > 0) {
                  if (itemStack.m_41613_() == 1) {
                     player.m_21008_(hand, container);
                  } else if (itemStack.m_41613_() > 1 && player.m_150109_().m_36054_(container)) {
                     itemStack.m_41774_(1);
                  } else {
                     player.m_7197_(container, false, true);
                     itemStack.m_41774_(1);
                  }

                  fluidTank.extract(filled, Action.EXECUTE, AutomationType.MANUAL);
                  return true;
               }
            }
         } else {
            FluidStack simulatedRemainder = fluidTank.insert(fluidInItem, Action.SIMULATE, AutomationType.MANUAL);
            int remainder = simulatedRemainder.getAmount();
            int storedAmount = fluidInItem.getAmount();
            if (remainder < storedAmount) {
               boolean filled = false;
               FluidStack drained = handler.drain(
                  new FluidStack(fluidInItem, storedAmount - remainder), player.m_7500_() ? FluidAction.SIMULATE : FluidAction.EXECUTE
               );
               if (!drained.isEmpty()) {
                  ItemStack container = handler.getContainer();
                  if (player.m_7500_()) {
                     filled = true;
                  } else if (!container.m_41619_()) {
                     if (itemStack.m_41613_() == 1) {
                        player.m_21008_(hand, container);
                        filled = true;
                     } else if (player.m_150109_().m_36054_(container)) {
                        itemStack.m_41774_(1);
                        filled = true;
                     }
                  } else {
                     itemStack.m_41774_(1);
                     if (itemStack.m_41619_()) {
                        player.m_21008_(hand, ItemStack.f_41583_);
                     }

                     filled = true;
                  }

                  if (filled) {
                     fluidTank.insert(drained, Action.EXECUTE, AutomationType.MANUAL);
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }
}
