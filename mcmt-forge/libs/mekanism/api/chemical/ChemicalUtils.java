package mekanism.api.chemical;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import java.util.function.Function;
import java.util.function.IntSupplier;
import mekanism.api.Action;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ChemicalUtils {
   private ChemicalUtils() {
   }

   public static void writeChemicalStack(FriendlyByteBuf buffer, ChemicalStack<?> stack) {
      if (stack.isEmpty()) {
         buffer.writeBoolean(false);
      } else {
         buffer.writeBoolean(true);
         stack.writeToPacket(buffer);
      }
   }

   public static GasStack readGasStack(FriendlyByteBuf buffer) {
      return readStack(buffer, GasStack::readFromPacket, GasStack.EMPTY);
   }

   public static InfusionStack readInfusionStack(FriendlyByteBuf buffer) {
      return readStack(buffer, InfusionStack::readFromPacket, InfusionStack.EMPTY);
   }

   public static PigmentStack readPigmentStack(FriendlyByteBuf buffer) {
      return readStack(buffer, PigmentStack::readFromPacket, PigmentStack.EMPTY);
   }

   public static SlurryStack readSlurryStack(FriendlyByteBuf buffer) {
      return readStack(buffer, SlurryStack::readFromPacket, SlurryStack.EMPTY);
   }

   private static <STACK extends ChemicalStack<?>> STACK readStack(FriendlyByteBuf buffer, Function<FriendlyByteBuf, STACK> reader, STACK empty) {
      return buffer.readBoolean() ? reader.apply(buffer) : empty;
   }

   public static <CHEMICAL extends Chemical<CHEMICAL>> CHEMICAL readChemicalFromNBT(
      @Nullable CompoundTag nbtTags, CHEMICAL empty, String nbtName, Function<ResourceLocation, CHEMICAL> registryLookup
   ) {
      return nbtTags != null && !nbtTags.m_128456_() ? registryLookup.apply(new ResourceLocation(nbtTags.m_128461_(nbtName))) : empty;
   }

   public static <CHEMICAL extends Chemical<CHEMICAL>> CHEMICAL readChemicalFromRegistry(
      @Nullable ResourceLocation name, CHEMICAL empty, IForgeRegistry<CHEMICAL> registry
   ) {
      if (name == null) {
         return empty;
      } else {
         CHEMICAL chemical = (CHEMICAL)registry.getValue(name);
         return chemical == null ? empty : chemical;
      }
   }

   public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> STACK insert(
      STACK stack,
      Action action,
      STACK empty,
      IntSupplier tankCount,
      Int2ObjectFunction<STACK> inTankGetter,
      ChemicalUtils.InsertChemical<STACK> insertChemical
   ) {
      int tanks = tankCount.getAsInt();
      if (tanks == 1) {
         return insertChemical.insert(0, stack, action);
      } else {
         IntList matchingTanks = new IntArrayList();
         IntList emptyTanks = new IntArrayList();

         for (int tank = 0; tank < tanks; tank++) {
            STACK inTank = (STACK)inTankGetter.get(tank);
            if (inTank.isEmpty()) {
               emptyTanks.add(tank);
            } else if (inTank.isTypeEqual(stack)) {
               matchingTanks.add(tank);
            }
         }

         STACK toInsert = stack;
         IntListIterator var14 = matchingTanks.iterator();

         while (var14.hasNext()) {
            int tankx = (Integer)var14.next();
            STACK remainder = insertChemical.insert(tankx, toInsert, action);
            if (remainder.isEmpty()) {
               return empty;
            }

            toInsert = remainder;
         }

         var14 = emptyTanks.iterator();

         while (var14.hasNext()) {
            int tankx = (Integer)var14.next();
            STACK remainder = insertChemical.insert(tankx, toInsert, action);
            if (remainder.isEmpty()) {
               return empty;
            }

            toInsert = remainder;
         }

         return toInsert;
      }
   }

   public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> STACK extract(
      long amount,
      Action action,
      STACK empty,
      IntSupplier tankCount,
      Int2ObjectFunction<STACK> inTankGetter,
      ChemicalUtils.ExtractChemical<STACK> extractChemical
   ) {
      int tanks = tankCount.getAsInt();
      if (tanks == 1) {
         return extractChemical.extract(0, amount, action);
      } else {
         STACK extracted = empty;
         long toDrain = amount;

         for (int tank = 0; tank < tanks; tank++) {
            if (extracted.isEmpty() || extracted.isTypeEqual((ChemicalStack<CHEMICAL>)inTankGetter.get(tank))) {
               STACK drained = extractChemical.extract(tank, toDrain, action);
               if (!drained.isEmpty()) {
                  if (extracted.isEmpty()) {
                     extracted = drained;
                  } else {
                     extracted.grow(drained.getAmount());
                  }

                  toDrain -= drained.getAmount();
                  if (toDrain == 0L) {
                     break;
                  }
               }
            }
         }

         return extracted;
      }
   }

   public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> STACK extract(
      STACK stack,
      Action action,
      STACK empty,
      IntSupplier tankCount,
      Int2ObjectFunction<STACK> inTankGetter,
      ChemicalUtils.ExtractChemical<STACK> extractChemical
   ) {
      int tanks = tankCount.getAsInt();
      if (tanks == 1) {
         STACK inTank = (STACK)inTankGetter.get(0);
         return !inTank.isEmpty() && inTank.isTypeEqual(stack) ? extractChemical.extract(0, stack.getAmount(), action) : empty;
      } else {
         STACK extracted = empty;
         long toDrain = stack.getAmount();

         for (int tank = 0; tank < tanks; tank++) {
            if (stack.isTypeEqual((ChemicalStack<CHEMICAL>)inTankGetter.get(tank))) {
               STACK drained = extractChemical.extract(tank, toDrain, action);
               if (!drained.isEmpty()) {
                  if (extracted.isEmpty()) {
                     extracted = drained;
                  } else {
                     extracted.grow(drained.getAmount());
                  }

                  toDrain -= drained.getAmount();
                  if (toDrain == 0L) {
                     break;
                  }
               }
            }
         }

         return extracted;
      }
   }

   @FunctionalInterface
   public interface ChemicalToStackCreator<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> {
      STACK createStack(CHEMICAL var1, long var2);
   }

   @FunctionalInterface
   public interface ExtractChemical<STACK extends ChemicalStack<?>> {
      STACK extract(int var1, long var2, Action var4);
   }

   @FunctionalInterface
   public interface InsertChemical<STACK extends ChemicalStack<?>> {
      STACK insert(int var1, STACK var2, Action var3);
   }

   @FunctionalInterface
   public interface StackToStackCreator<STACK extends ChemicalStack<?>> {
      STACK createStack(STACK var1, long var2);
   }
}
