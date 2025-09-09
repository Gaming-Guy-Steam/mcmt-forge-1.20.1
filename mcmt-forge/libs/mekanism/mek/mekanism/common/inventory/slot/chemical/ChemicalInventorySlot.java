package mekanism.common.inventory.slot.chemical;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.lookup.cache.InputRecipeCache;
import mekanism.common.util.MekanismUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public abstract class ChemicalInventorySlot<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> extends BasicInventorySlot {
   protected final Supplier<Level> worldSupplier;
   protected final IChemicalTank<CHEMICAL, STACK> chemicalTank;

   @Nullable
   protected static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, STACK>> HANDLER getCapability(
      ItemStack stack, Capability<HANDLER> capability
   ) {
      return (HANDLER)(stack.m_41619_() ? null : stack.getCapability(capability).resolve().orElse(null));
   }

   protected static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, RECIPE extends ItemStackToChemicalRecipe<CHEMICAL, STACK>> STACK getPotentialConversion(
      IMekanismRecipeTypeProvider<RECIPE, InputRecipeCache.SingleItem<RECIPE>> recipeType, @Nullable Level world, ItemStack itemStack, STACK empty
   ) {
      ItemStackToChemicalRecipe<CHEMICAL, STACK> foundRecipe = recipeType.getInputCache().findTypeBasedRecipe(world, itemStack);
      return foundRecipe == null ? empty : foundRecipe.getOutput(itemStack);
   }

   protected static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> Predicate<ItemStack> getFillOrConvertExtractPredicate(
      IChemicalTank<CHEMICAL, STACK> chemicalTank,
      Function<ItemStack, IChemicalHandler<CHEMICAL, STACK>> handlerFunction,
      Function<ItemStack, STACK> potentialConversionSupplier
   ) {
      return stack -> {
         IChemicalHandler<CHEMICAL, STACK> handler = handlerFunction.apply(stack);
         if (handler != null) {
            for (int tank = 0; tank < handler.getTanks(); tank++) {
               if (chemicalTank.isValid(handler.getChemicalInTank(tank))) {
                  return false;
               }
            }
         }

         STACK conversion = potentialConversionSupplier.apply(stack);
         return conversion.isEmpty() || !chemicalTank.isValid(conversion);
      };
   }

   protected static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> Predicate<ItemStack> getFillOrConvertInsertPredicate(
      IChemicalTank<CHEMICAL, STACK> chemicalTank,
      Function<ItemStack, IChemicalHandler<CHEMICAL, STACK>> handlerFunction,
      Function<ItemStack, STACK> potentialConversionSupplier
   ) {
      return stack -> {
         if (fillInsertCheck(chemicalTank, handlerFunction.apply(stack))) {
            return true;
         } else {
            STACK conversion = potentialConversionSupplier.apply(stack);
            if (conversion.isEmpty()) {
               return false;
            } else {
               return chemicalTank.insert(conversion, Action.SIMULATE, AutomationType.INTERNAL).getAmount() < conversion.getAmount()
                  ? true
                  : chemicalTank.getNeeded() == 0L && chemicalTank.isTypeEqual(conversion) && chemicalTank.isValid(conversion);
            }
         }
      };
   }

   public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> Predicate<ItemStack> getFillExtractPredicate(
      IChemicalTank<CHEMICAL, STACK> chemicalTank, Function<ItemStack, IChemicalHandler<CHEMICAL, STACK>> handlerFunction
   ) {
      return stack -> {
         IChemicalHandler<CHEMICAL, STACK> handler = handlerFunction.apply(stack);
         if (handler != null) {
            for (int tank = 0; tank < handler.getTanks(); tank++) {
               STACK storedChemical = handler.getChemicalInTank(tank);
               if (!storedChemical.isEmpty() && chemicalTank.isValid(storedChemical)) {
                  return false;
               }
            }
         }

         return true;
      };
   }

   public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> boolean fillInsertCheck(
      IChemicalTank<CHEMICAL, STACK> chemicalTank, @Nullable IChemicalHandler<CHEMICAL, STACK> handler
   ) {
      if (handler != null) {
         for (int tank = 0; tank < handler.getTanks(); tank++) {
            STACK chemicalInTank = handler.getChemicalInTank(tank);
            if (!chemicalInTank.isEmpty()
               && chemicalTank.insert(chemicalInTank, Action.SIMULATE, AutomationType.INTERNAL).getAmount() < chemicalInTank.getAmount()) {
               return true;
            }
         }
      }

      return false;
   }

   public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> Predicate<ItemStack> getDrainInsertPredicate(
      IChemicalTank<CHEMICAL, STACK> chemicalTank, Function<ItemStack, IChemicalHandler<CHEMICAL, STACK>> handlerFunction
   ) {
      return stack -> {
         IChemicalHandler<CHEMICAL, STACK> handler = handlerFunction.apply(stack);
         if (handler != null) {
            if (chemicalTank.isEmpty()) {
               for (int tank = 0; tank < handler.getTanks(); tank++) {
                  if (handler.getChemicalInTank(tank).getAmount() < handler.getTankCapacity(tank)) {
                     return true;
                  }
               }

               return false;
            } else {
               return handler.insertChemical(chemicalTank.getStack(), Action.SIMULATE).getAmount() < chemicalTank.getStored();
            }
         } else {
            return false;
         }
      };
   }

   protected ChemicalInventorySlot(
      IChemicalTank<CHEMICAL, STACK> chemicalTank,
      Supplier<Level> worldSupplier,
      Predicate<ItemStack> canExtract,
      Predicate<ItemStack> canInsert,
      Predicate<ItemStack> validator,
      @Nullable IContentsListener listener,
      int x,
      int y
   ) {
      super(canExtract, canInsert, validator, listener, x, y);
      this.setSlotType(ContainerSlotType.EXTRA);
      this.chemicalTank = chemicalTank;
      this.worldSupplier = worldSupplier;
   }

   @Nullable
   protected abstract IChemicalHandler<CHEMICAL, STACK> getCapability();

   @Nullable
   protected ItemStackToChemicalRecipe<CHEMICAL, STACK> getConversionRecipe(@Nullable Level world, ItemStack stack) {
      return null;
   }

   public void fillTankOrConvert() {
      if (!this.isEmpty() && this.chemicalTank.getNeeded() > 0L && !this.fillTankFromItem()) {
         ItemStackToChemicalRecipe<CHEMICAL, STACK> foundRecipe = this.getConversionRecipe(this.worldSupplier.get(), this.current);
         if (foundRecipe != null) {
            ItemStack itemInput = foundRecipe.getInput().getMatchingInstance(this.current);
            if (!itemInput.m_41619_()) {
               STACK output = foundRecipe.getOutput(itemInput);
               if (!output.isEmpty() && this.chemicalTank.insert(output, Action.SIMULATE, AutomationType.MANUAL).isEmpty()) {
                  MekanismUtils.logMismatchedStackSize(this.chemicalTank.insert(output, Action.EXECUTE, AutomationType.MANUAL).getAmount(), 0L);
                  int amountUsed = itemInput.m_41613_();
                  MekanismUtils.logMismatchedStackSize(this.shrinkStack(amountUsed, Action.EXECUTE), amountUsed);
               }
            }
         }
      }
   }

   public void fillTank() {
      fillChemicalTank(this, this.chemicalTank, this.getCapability());
   }

   public boolean fillTankFromItem() {
      return fillChemicalTankFromItem(this, this.chemicalTank, this.getCapability());
   }

   public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> void fillChemicalTank(
      IInventorySlot slot, IChemicalTank<CHEMICAL, STACK> chemicalTank, @Nullable IChemicalHandler<CHEMICAL, STACK> handler
   ) {
      if (!slot.isEmpty() && chemicalTank.getNeeded() > 0L) {
         fillChemicalTankFromItem(slot, chemicalTank, handler);
      }
   }

   public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> boolean fillChemicalTankFromItem(
      IInventorySlot slot, IChemicalTank<CHEMICAL, STACK> chemicalTank, @Nullable IChemicalHandler<CHEMICAL, STACK> handler
   ) {
      if (handler != null) {
         boolean didTransfer = false;

         for (int tank = 0; tank < handler.getTanks(); tank++) {
            STACK chemicalInItem = handler.getChemicalInTank(tank);
            if (!chemicalInItem.isEmpty()) {
               STACK simulatedRemainder = chemicalTank.insert(chemicalInItem, Action.SIMULATE, AutomationType.INTERNAL);
               long chemicalInItemAmount = chemicalInItem.getAmount();
               long remainder = simulatedRemainder.getAmount();
               if (remainder < chemicalInItemAmount) {
                  STACK extractedChemical = handler.extractChemical(tank, chemicalInItemAmount - remainder, Action.EXECUTE);
                  if (!extractedChemical.isEmpty()) {
                     MekanismUtils.logMismatchedStackSize(chemicalTank.insert(extractedChemical, Action.EXECUTE, AutomationType.INTERNAL).getAmount(), 0L);
                     didTransfer = true;
                     if (chemicalTank.getNeeded() == 0L) {
                        break;
                     }
                  }
               }
            }
         }

         if (didTransfer) {
            slot.onContentsChanged();
            return true;
         }
      }

      return false;
   }

   public void drainTank() {
      drainChemicalTank(this, this.chemicalTank, this.getCapability());
   }

   public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> void drainChemicalTank(
      IInventorySlot slot, IChemicalTank<CHEMICAL, STACK> chemicalTank, @Nullable IChemicalHandler<CHEMICAL, STACK> handler
   ) {
      if (!slot.isEmpty() && !chemicalTank.isEmpty() && handler != null) {
         STACK storedChemical = chemicalTank.getStack();
         STACK simulatedRemainder = handler.insertChemical(storedChemical, Action.SIMULATE);
         long remainder = simulatedRemainder.getAmount();
         long amount = storedChemical.getAmount();
         if (remainder < amount) {
            STACK extractedChemical = chemicalTank.extract(amount - remainder, Action.EXECUTE, AutomationType.INTERNAL);
            if (!extractedChemical.isEmpty()) {
               MekanismUtils.logMismatchedStackSize(handler.insertChemical(extractedChemical, Action.EXECUTE).getAmount(), 0L);
               slot.onContentsChanged();
            }
         }
      }
   }
}
