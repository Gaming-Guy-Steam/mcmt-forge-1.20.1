package mekanism.common.capabilities.fluid.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.capabilities.GenericTankSpec;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.TriPredicate;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class RateLimitMultiTankFluidHandler extends ItemStackMekanismFluidHandler {
   private final List<IExtendedFluidTank> tanks;

   public static RateLimitMultiTankFluidHandler create(@NotNull Collection<RateLimitMultiTankFluidHandler.FluidTankSpec> fluidTanks) {
      return new RateLimitMultiTankFluidHandler(fluidTanks);
   }

   private RateLimitMultiTankFluidHandler(@NotNull Collection<RateLimitMultiTankFluidHandler.FluidTankSpec> fluidTanks) {
      List<IExtendedFluidTank> tankProviders = new ArrayList<>();

      for (RateLimitMultiTankFluidHandler.FluidTankSpec spec : fluidTanks) {
         tankProviders.add(
            new RateLimitFluidHandler.RateLimitFluidTank(
               spec.rate,
               spec.capacity,
               spec.canExtract,
               (fluid, automationType) -> spec.canInsert.test(fluid, automationType, this.getStack()),
               spec.isValid,
               this
            )
         );
      }

      this.tanks = Collections.unmodifiableList(tankProviders);
   }

   @Override
   protected List<IExtendedFluidTank> getInitialTanks() {
      return this.tanks;
   }

   public static class FluidTankSpec extends GenericTankSpec<FluidStack> {
      final IntSupplier rate;
      final IntSupplier capacity;

      public FluidTankSpec(
         IntSupplier rate,
         IntSupplier capacity,
         BiPredicate<FluidStack, AutomationType> canExtract,
         TriPredicate<FluidStack, AutomationType, ItemStack> canInsert,
         Predicate<FluidStack> isValid,
         Predicate<ItemStack> supportsStack
      ) {
         super(canExtract, canInsert, isValid, supportsStack);
         this.rate = rate;
         this.capacity = capacity;
      }

      public static RateLimitMultiTankFluidHandler.FluidTankSpec create(IntSupplier rate, IntSupplier capacity) {
         return new RateLimitMultiTankFluidHandler.FluidTankSpec(
            rate,
            capacity,
            ConstantPredicates.alwaysTrueBi(),
            ConstantPredicates.alwaysTrueTri(),
            ConstantPredicates.alwaysTrue(),
            ConstantPredicates.alwaysTrue()
         );
      }

      public static RateLimitMultiTankFluidHandler.FluidTankSpec createFillOnly(IntSupplier rate, IntSupplier capacity, Predicate<FluidStack> isValid) {
         return createFillOnly(rate, capacity, isValid, ConstantPredicates.alwaysTrue());
      }

      public static RateLimitMultiTankFluidHandler.FluidTankSpec createFillOnly(
         IntSupplier rate, IntSupplier capacity, Predicate<FluidStack> isValid, Predicate<ItemStack> supportsStack
      ) {
         return new RateLimitMultiTankFluidHandler.FluidTankSpec(
            rate, capacity, ConstantPredicates.notExternal(), (chemical, automation, stack) -> supportsStack.test(stack), isValid, supportsStack
         );
      }
   }
}
