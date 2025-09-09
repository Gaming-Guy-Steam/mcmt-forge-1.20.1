package mekanism.api.math;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import java.util.function.IntSupplier;
import mekanism.api.Action;
import mekanism.api.annotations.NothingNullByDefault;

@NothingNullByDefault
public class FloatingLongTransferUtils {
   private FloatingLongTransferUtils() {
   }

   public static FloatingLong insert(
      FloatingLong stack,
      Action action,
      IntSupplier containerCount,
      Int2ObjectFunction<FloatingLong> inContainerGetter,
      FloatingLongTransferUtils.InsertFloatingLong insert
   ) {
      int containers = containerCount.getAsInt();
      if (containers == 1) {
         return insert.insert(0, stack, action);
      } else {
         IntList matchingContainers = new IntArrayList();
         IntList emptyContainers = new IntArrayList();

         for (int container = 0; container < containers; container++) {
            FloatingLong inContainer = (FloatingLong)inContainerGetter.apply(container);
            if (inContainer.isZero()) {
               emptyContainers.add(container);
            } else {
               matchingContainers.add(container);
            }
         }

         FloatingLong toInsert = stack;
         IntListIterator var13 = matchingContainers.iterator();

         while (var13.hasNext()) {
            int containerx = (Integer)var13.next();
            FloatingLong remainder = insert.insert(containerx, toInsert, action);
            if (remainder.isZero()) {
               return FloatingLong.ZERO;
            }

            toInsert = remainder;
         }

         var13 = emptyContainers.iterator();

         while (var13.hasNext()) {
            int containerx = (Integer)var13.next();
            FloatingLong remainder = insert.insert(containerx, toInsert, action);
            if (remainder.isZero()) {
               return FloatingLong.ZERO;
            }

            toInsert = remainder;
         }

         return toInsert;
      }
   }

   public static FloatingLong extract(FloatingLong amount, Action action, IntSupplier containerCount, FloatingLongTransferUtils.ExtractFloatingLong extract) {
      int containers = containerCount.getAsInt();
      if (containers == 1) {
         return extract.extract(0, amount, action);
      } else {
         FloatingLong extracted = FloatingLong.ZERO;
         FloatingLong toExtract = amount.copy();

         for (int container = 0; container < containers; container++) {
            FloatingLong drained = extract.extract(container, toExtract, action);
            if (!drained.isZero()) {
               if (extracted.isZero()) {
                  extracted = drained;
               } else {
                  extracted = extracted.plusEqual(drained);
               }

               toExtract = toExtract.minusEqual(drained);
               if (toExtract.isZero()) {
                  break;
               }
            }
         }

         return extracted;
      }
   }

   @FunctionalInterface
   public interface ExtractFloatingLong {
      FloatingLong extract(int var1, FloatingLong var2, Action var3);
   }

   @FunctionalInterface
   public interface InsertFloatingLong {
      FloatingLong insert(int var1, FloatingLong var2, Action var3);
   }
}
