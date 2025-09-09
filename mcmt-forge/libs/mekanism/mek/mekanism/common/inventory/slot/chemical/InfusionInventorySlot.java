package mekanism.common.inventory.slot.chemical;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.ItemStackToInfuseTypeRecipe;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class InfusionInventorySlot extends ChemicalInventorySlot<InfuseType, InfusionStack> {
   @Nullable
   public static IInfusionHandler getCapability(ItemStack stack) {
      return getCapability(stack, Capabilities.INFUSION_HANDLER);
   }

   private static InfusionStack getPotentialConversion(@Nullable Level world, ItemStack itemStack) {
      return getPotentialConversion(MekanismRecipeType.INFUSION_CONVERSION, world, itemStack, InfusionStack.EMPTY);
   }

   public static InfusionInventorySlot fillOrConvert(
      IInfusionTank infusionTank, Supplier<Level> worldSupplier, @Nullable IContentsListener listener, int x, int y
   ) {
      Objects.requireNonNull(infusionTank, "Infusion tank cannot be null");
      Objects.requireNonNull(worldSupplier, "World supplier cannot be null");
      Function<ItemStack, InfusionStack> potentialConversionSupplier = stack -> getPotentialConversion(worldSupplier.get(), stack);
      return new InfusionInventorySlot(
         infusionTank,
         worldSupplier,
         getFillOrConvertExtractPredicate(infusionTank, InfusionInventorySlot::getCapability, potentialConversionSupplier),
         getFillOrConvertInsertPredicate(infusionTank, InfusionInventorySlot::getCapability, potentialConversionSupplier),
         stack -> {
            if (stack.getCapability(Capabilities.INFUSION_HANDLER).isPresent()) {
               return true;
            } else {
               InfusionStack conversion = getPotentialConversion(worldSupplier.get(), stack);
               return !conversion.isEmpty() && infusionTank.isValid(conversion);
            }
         },
         listener,
         x,
         y
      );
   }

   private InfusionInventorySlot(
      IInfusionTank infusionTank,
      Supplier<Level> worldSupplier,
      Predicate<ItemStack> canExtract,
      Predicate<ItemStack> canInsert,
      Predicate<ItemStack> validator,
      @Nullable IContentsListener listener,
      int x,
      int y
   ) {
      super(infusionTank, worldSupplier, canExtract, canInsert, validator, listener, x, y);
   }

   @Nullable
   @Override
   protected IChemicalHandler<InfuseType, InfusionStack> getCapability() {
      return getCapability(this.current);
   }

   @Nullable
   protected ItemStackToInfuseTypeRecipe getConversionRecipe(@Nullable Level world, ItemStack stack) {
      return MekanismRecipeType.INFUSION_CONVERSION.getInputCache().findFirstRecipe(world, stack);
   }
}
