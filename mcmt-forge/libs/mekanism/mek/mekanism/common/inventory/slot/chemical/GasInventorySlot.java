package mekanism.common.inventory.slot.chemical;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class GasInventorySlot extends ChemicalInventorySlot<Gas, GasStack> {
   @Nullable
   public static IGasHandler getCapability(ItemStack stack) {
      return getCapability(stack, Capabilities.GAS_HANDLER);
   }

   private static GasStack getPotentialConversion(@Nullable Level world, ItemStack itemStack) {
      return getPotentialConversion(MekanismRecipeType.GAS_CONVERSION, world, itemStack, GasStack.EMPTY);
   }

   public static GasInventorySlot rotaryDrain(IGasTank gasTank, BooleanSupplier modeSupplier, @Nullable IContentsListener listener, int x, int y) {
      Objects.requireNonNull(gasTank, "Gas tank cannot be null");
      Objects.requireNonNull(modeSupplier, "Mode supplier cannot be null");
      Predicate<ItemStack> insertPredicate = getDrainInsertPredicate(gasTank, GasInventorySlot::getCapability).and(stack -> modeSupplier.getAsBoolean());
      return new GasInventorySlot(
         gasTank, insertPredicate.negate(), insertPredicate, stack -> stack.getCapability(Capabilities.GAS_HANDLER).isPresent(), listener, x, y
      );
   }

   public static GasInventorySlot rotaryFill(IGasTank gasTank, BooleanSupplier modeSupplier, @Nullable IContentsListener listener, int x, int y) {
      Objects.requireNonNull(gasTank, "Gas tank cannot be null");
      Objects.requireNonNull(modeSupplier, "Mode supplier cannot be null");
      return new GasInventorySlot(
         gasTank,
         getFillExtractPredicate(gasTank, GasInventorySlot::getCapability),
         stack -> !modeSupplier.getAsBoolean() && fillInsertCheck(gasTank, getCapability(stack)),
         stack -> stack.getCapability(Capabilities.GAS_HANDLER).isPresent(),
         listener,
         x,
         y
      );
   }

   public static GasInventorySlot fillOrConvert(IGasTank gasTank, Supplier<Level> worldSupplier, @Nullable IContentsListener listener, int x, int y) {
      Objects.requireNonNull(gasTank, "Gas tank cannot be null");
      Objects.requireNonNull(worldSupplier, "World supplier cannot be null");
      Function<ItemStack, GasStack> potentialConversionSupplier = stack -> getPotentialConversion(worldSupplier.get(), stack);
      return new GasInventorySlot(
         gasTank,
         worldSupplier,
         getFillOrConvertExtractPredicate(gasTank, GasInventorySlot::getCapability, potentialConversionSupplier),
         getFillOrConvertInsertPredicate(gasTank, GasInventorySlot::getCapability, potentialConversionSupplier),
         stack -> {
            if (stack.getCapability(Capabilities.GAS_HANDLER).isPresent()) {
               return true;
            } else {
               GasStack gasConversion = getPotentialConversion(worldSupplier.get(), stack);
               return !gasConversion.isEmpty() && gasTank.isValid(gasConversion);
            }
         },
         listener,
         x,
         y
      );
   }

   public static GasInventorySlot fill(IGasTank gasTank, @Nullable IContentsListener listener, int x, int y) {
      Objects.requireNonNull(gasTank, "Gas tank cannot be null");
      return new GasInventorySlot(
         gasTank,
         getFillExtractPredicate(gasTank, GasInventorySlot::getCapability),
         stack -> fillInsertCheck(gasTank, getCapability(stack)),
         stack -> stack.getCapability(Capabilities.GAS_HANDLER).isPresent(),
         listener,
         x,
         y
      );
   }

   public static GasInventorySlot drain(IGasTank gasTank, @Nullable IContentsListener listener, int x, int y) {
      Objects.requireNonNull(gasTank, "Gas tank cannot be null");
      Predicate<ItemStack> insertPredicate = getDrainInsertPredicate(gasTank, GasInventorySlot::getCapability);
      return new GasInventorySlot(
         gasTank, insertPredicate.negate(), insertPredicate, stack -> stack.getCapability(Capabilities.GAS_HANDLER).isPresent(), listener, x, y
      );
   }

   private GasInventorySlot(
      IGasTank gasTank,
      Predicate<ItemStack> canExtract,
      Predicate<ItemStack> canInsert,
      Predicate<ItemStack> validator,
      @Nullable IContentsListener listener,
      int x,
      int y
   ) {
      this(gasTank, () -> null, canExtract, canInsert, validator, listener, x, y);
   }

   private GasInventorySlot(
      IGasTank gasTank,
      Supplier<Level> worldSupplier,
      Predicate<ItemStack> canExtract,
      Predicate<ItemStack> canInsert,
      Predicate<ItemStack> validator,
      @Nullable IContentsListener listener,
      int x,
      int y
   ) {
      super(gasTank, worldSupplier, canExtract, canInsert, validator, listener, x, y);
   }

   @Nullable
   @Override
   protected IChemicalHandler<Gas, GasStack> getCapability() {
      return getCapability(this.current);
   }

   @Nullable
   protected ItemStackToGasRecipe getConversionRecipe(@Nullable Level world, ItemStack stack) {
      return MekanismRecipeType.GAS_CONVERSION.getInputCache().findFirstRecipe(world, stack);
   }
}
