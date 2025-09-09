package mekanism.api.fluid;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import java.util.function.IntSupplier;
import mekanism.api.Action;
import net.minecraftforge.fluids.FluidStack;

public class ExtendedFluidHandlerUtils {
   public static FluidStack insert(
      FluidStack stack, Action action, IntSupplier tankCount, Int2ObjectFunction<FluidStack> inTankGetter, ExtendedFluidHandlerUtils.InsertFluid insertFluid
   ) {
      int tanks = tankCount.getAsInt();
      if (tanks == 1) {
         return insertFluid.insert(0, stack, action);
      } else {
         IntList matchingTanks = new IntArrayList();
         IntList emptyTanks = new IntArrayList();

         for (int tank = 0; tank < tanks; tank++) {
            FluidStack inTank = (FluidStack)inTankGetter.get(tank);
            if (inTank.isEmpty()) {
               emptyTanks.add(tank);
            } else if (inTank.isFluidEqual(stack)) {
               matchingTanks.add(tank);
            }
         }

         FluidStack toInsert = stack;
         IntListIterator var13 = matchingTanks.iterator();

         while (var13.hasNext()) {
            int tankx = (Integer)var13.next();
            FluidStack remainder = insertFluid.insert(tankx, toInsert, action);
            if (remainder.isEmpty()) {
               return FluidStack.EMPTY;
            }

            toInsert = remainder;
         }

         var13 = emptyTanks.iterator();

         while (var13.hasNext()) {
            int tankx = (Integer)var13.next();
            FluidStack remainder = insertFluid.insert(tankx, toInsert, action);
            if (remainder.isEmpty()) {
               return FluidStack.EMPTY;
            }

            toInsert = remainder;
         }

         return toInsert;
      }
   }

   public static FluidStack extract(
      int amount, Action action, IntSupplier tankCount, Int2ObjectFunction<FluidStack> inTankGetter, ExtendedFluidHandlerUtils.ExtractFluid extractFluid
   ) {
      int tanks = tankCount.getAsInt();
      if (tanks == 1) {
         return extractFluid.extract(0, amount, action);
      } else {
         FluidStack extracted = FluidStack.EMPTY;
         int toDrain = amount;

         for (int tank = 0; tank < tanks; tank++) {
            if (extracted.isEmpty() || extracted.isFluidEqual((FluidStack)inTankGetter.get(tank))) {
               FluidStack drained = extractFluid.extract(tank, toDrain, action);
               if (!drained.isEmpty()) {
                  if (extracted.isEmpty()) {
                     extracted = drained;
                  } else {
                     extracted.grow(drained.getAmount());
                  }

                  toDrain -= drained.getAmount();
                  if (toDrain == 0) {
                     break;
                  }
               }
            }
         }

         return extracted;
      }
   }

   public static FluidStack extract(
      FluidStack stack, Action action, IntSupplier tankCount, Int2ObjectFunction<FluidStack> inTankGetter, ExtendedFluidHandlerUtils.ExtractFluid extractFluid
   ) {
      int tanks = tankCount.getAsInt();
      if (tanks == 1) {
         FluidStack inTank = (FluidStack)inTankGetter.get(0);
         return !inTank.isEmpty() && inTank.isFluidEqual(stack) ? extractFluid.extract(0, stack.getAmount(), action) : FluidStack.EMPTY;
      } else {
         FluidStack extracted = FluidStack.EMPTY;
         int toDrain = stack.getAmount();

         for (int tank = 0; tank < tanks; tank++) {
            if (stack.isFluidEqual((FluidStack)inTankGetter.get(tank))) {
               FluidStack drained = extractFluid.extract(tank, toDrain, action);
               if (!drained.isEmpty()) {
                  if (extracted.isEmpty()) {
                     extracted = drained;
                  } else {
                     extracted.grow(drained.getAmount());
                  }

                  toDrain -= drained.getAmount();
                  if (toDrain == 0) {
                     break;
                  }
               }
            }
         }

         return extracted;
      }
   }

   @FunctionalInterface
   public interface ExtractFluid {
      FluidStack extract(int var1, int var2, Action var3);
   }

   @FunctionalInterface
   public interface InsertFluid {
      FluidStack insert(int var1, FluidStack var2, Action var3);
   }
}
